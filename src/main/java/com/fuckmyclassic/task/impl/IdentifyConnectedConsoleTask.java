package com.fuckmyclassic.task.impl;

import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.dao.ConsoleDAO;
import com.fuckmyclassic.model.Console;
import com.fuckmyclassic.model.ConsoleType;
import com.fuckmyclassic.network.NetworkManager;
import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.task.AbstractTaskCreator;
import com.fuckmyclassic.ui.util.PlatformUtils;
import com.fuckmyclassic.userconfig.UserConfiguration;
import com.jcraft.jsch.JSchException;
import javafx.concurrent.Task;
import javafx.scene.control.TextInputDialog;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Simple Service to update the current console SID.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class IdentifyConnectedConsoleTask extends AbstractTaskCreator<String> {

    static Logger LOG = LogManager.getLogger(IdentifyConnectedConsoleTask.class.getName());

    // Resource keys for the dialog that ran this task
    private final String IN_PROGRESS_MESSAGE_KEY = "IdentifyConnectedConsoleTask.inProgressMessage";
    private final String COMPLETE_MESSAGE_KEY = "IdentifyConnectedConsoleTask.completeMessage";

    // Resource keys for the dialog to let the user name a new console
    private final String NEW_CONSOLE_DIALOG_TITLE_KEY = "IdentifyConnectedConsoleTask.newConsoleDialog.title";
    private final String NEW_CONSOLE_DIALOG_HEADER_KEY = "IdentifyConnectedConsoleTask.newConsoleDialog.header";
    private final String NEW_CONSOLE_DIALOG_CONTENT_KEY = "IdentifyConnectedConsoleTask.newConsoleDialog.content";

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
    public IdentifyConnectedConsoleTask(final ResourceBundle resourceBundle,
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

                    final Console console;

                    // we've never seen this console before, ask the user for a name for it
                    if (consoleDAO.getConsoleForSid(consoleSid) == null) {
                        console = consoleDAO.createConsoleForSid(consoleSid);

                        PlatformUtils.runAndWait(() -> {
                            final TextInputDialog nameDialog = new TextInputDialog(SharedConstants.DEFAULT_CONSOLE_NICKNAME);
                            nameDialog.setTitle(resourceBundle.getString(NEW_CONSOLE_DIALOG_TITLE_KEY));
                            nameDialog.setHeaderText(resourceBundle.getString(NEW_CONSOLE_DIALOG_HEADER_KEY));
                            nameDialog.setContentText(resourceBundle.getString(NEW_CONSOLE_DIALOG_CONTENT_KEY));
                            nameDialog.setGraphic(null);
                            final Optional<String> result = nameDialog.showAndWait();

                            if (result.isPresent()) {
                                console.setNickname(result.get().trim());
                            } else {
                                console.setNickname(SharedConstants.DEFAULT_CONSOLE_NICKNAME);
                            }
                        });
                    } else {
                        console = consoleDAO.getConsoleForSid(consoleSid);
                    }

                    console.setConsoleSyncPath(syncPath);
                    console.setConsoleType(ConsoleType.fromCode(consoleType));
                    console.setLastKnownAddress(dstAddress);
                    hibernateManager.updateEntity(console);
                    userConfiguration.setSelectedConsole(console);
                    userConfiguration.addConnectedConsole(console);

                    // if there is a saved console for UNKNOWN, go ahead and delete it also
                    final Console defaultConsole = consoleDAO.getConsoleForSid(SharedConstants.DEFAULT_CONSOLE_SID);

                    if (defaultConsole != null) {
                        hibernateManager.deleteEntity(defaultConsole);
                    }

                    updateMessage(resourceBundle.getString(COMPLETE_MESSAGE_KEY));
                    updateProgress(1, 1);

                    // null it out to enforce that all callers set this explicitly
                    dstAddress = null;
                    return consoleSid;
                } else {
                    throw new RuntimeException("Destination address wasn't set for IdentifyConnectedConsoleTask");
                }
            }
        };
    }

    public IdentifyConnectedConsoleTask setDstAddress(String dstAddress) {
        this.dstAddress = dstAddress;
        return this;
    }
}
