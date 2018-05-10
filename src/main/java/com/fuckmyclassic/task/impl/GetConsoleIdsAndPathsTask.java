package com.fuckmyclassic.task.impl;

import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.dao.ConsoleDAO;
import com.fuckmyclassic.model.Console;
import com.fuckmyclassic.model.ConsoleType;
import com.fuckmyclassic.network.NetworkManager;
import com.fuckmyclassic.task.AbstractTaskCreator;
import com.fuckmyclassic.userconfig.UserConfiguration;
import com.jcraft.jsch.JSchException;
import javafx.concurrent.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Simple Service to update the current console SID.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class GetConsoleIdsAndPathsTask extends AbstractTaskCreator<String> {

    static Logger LOG = LogManager.getLogger(GetConsoleIdsAndPathsTask.class.getName());

    private final String IN_PROGRESS_MESSAGE_KEY = "GetConsoleIdsAndPathsTask.inProgressMessage";
    private final String COMPLETE_MESSAGE_KEY = "GetConsoleIdsAndPathsTask.completeMessage";

    /** The connection used for SSH commands. */
    private final NetworkManager networkManager;
    /** The configuration about the current session */
    private final UserConfiguration userConfiguration;
    /** Bundle for getting localized strings. */
    private final ResourceBundle resourceBundle;
    /** Manager to save Console updates */
    private final HibernateManager hibernateManager;
    /** DAO for interacting with the Consoles table */
    private final ConsoleDAO consoleDAO;
    /** The address to send commands to */
    private String dstAddress;

    @Autowired
    public GetConsoleIdsAndPathsTask(final ResourceBundle resourceBundle,
                                     final NetworkManager networkManager,
                                     final UserConfiguration userConfiguration,
                                     final ConsoleDAO consoleDAO,
                                     final HibernateManager hibernateManager) {
        this.resourceBundle = resourceBundle;
        this.networkManager = networkManager;
        this.userConfiguration = userConfiguration;
        this.consoleDAO = consoleDAO;
        this.hibernateManager = hibernateManager;
    }

    @Override
    public Task<String> createTask() {
        return new Task<String>() {
            @Override
            protected String call() throws IOException, JSchException {
                if (!StringUtils.isBlank(dstAddress)) {
                    updateMessage(resourceBundle.getString(IN_PROGRESS_MESSAGE_KEY));
                    updateProgress(0, 1);

                    final String consoleSid = networkManager.runCommand(dstAddress,
                            "echo \"`devmem 0x01C23800``devmem 0x01C23804``devmem 0x01C23808``devmem 0x01C2380C`\"")
                            .trim().replace("0x", "");
                    final String consoleType = networkManager.runCommand(dstAddress,
                            "hakchi eval 'echo \"$sftype-$sfregion\"'").trim();
                    final String syncPath = networkManager.runCommand(dstAddress,
                            "hakchi findGameSyncStorage").trim();

                    LOG.info(String.format("Detected console SID: %s", consoleSid));
                    LOG.info(String.format("Detected console type: %s", consoleType));
                    LOG.info(String.format("Detected console sync path: %s", syncPath));

                    final Console console = consoleDAO.getOrCreateConsoleForSid(consoleSid);
                    console.setConsoleSyncPath(syncPath);
                    console.setConsoleType(ConsoleType.fromCode(consoleType));
                    console.setLastKnownAddress(dstAddress);
                    hibernateManager.updateEntity(console);
                    userConfiguration.setSelectedConsole(console);
                    userConfiguration.addConnectedConsole(console);

                    updateMessage(resourceBundle.getString(COMPLETE_MESSAGE_KEY));
                    updateProgress(1, 1);

                    // null it out to enforce that all callers set this explicitly
                    dstAddress = null;
                    return consoleSid;
                } else {
                    throw new RuntimeException("Destination address wasn't set for GetConsoleIdsAndPathsTask");
                }
            }
        };
    }

    public GetConsoleIdsAndPathsTask setDstAddress(String dstAddress) {
        this.dstAddress = dstAddress;
        return this;
    }
}
