package com.fuckmyclassic.task.impl;

import com.fuckmyclassic.network.NetworkConnection;
import com.fuckmyclassic.task.AbstractTaskCreator;
import com.fuckmyclassic.userconfig.ConsoleConfiguration;
import com.jcraft.jsch.JSchException;
import javafx.concurrent.Task;
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
    private final NetworkConnection networkConnection;
    /** The configuration about the currently connected console */
    private final ConsoleConfiguration consoleConfiguration;
    /** Bundle for getting localized strings. */
    private final ResourceBundle resourceBundle;

    @Autowired
    public GetConsoleIdsAndPathsTask(final ResourceBundle resourceBundle,
                                     final NetworkConnection networkConnection,
                                     final ConsoleConfiguration consoleConfiguration) {
        this.resourceBundle = resourceBundle;
        this.networkConnection = networkConnection;
        this.consoleConfiguration = consoleConfiguration;
    }

    @Override
    public Task<String> createTask() {
        return new Task<String>() {
            @Override
            protected String call() throws IOException, JSchException {
                updateMessage(resourceBundle.getString(IN_PROGRESS_MESSAGE_KEY));
                updateProgress(0, 1);

                final String consoleSid = networkConnection.runCommand(
                        "echo \"`devmem 0x01C23800``devmem 0x01C23804``devmem 0x01C23808``devmem 0x01C2380C`\"")
                        .trim().replace("0x", "");
                final String consoleType = networkConnection.runCommand("hakchi eval 'echo \"$sftype-$sfregion\"'").trim();
                final String syncPath = networkConnection.runCommand("hakchi findGameSyncStorage").trim();

                LOG.info(String.format("Detected console SID: %s", consoleSid));
                LOG.info(String.format("Detected console type: %s", consoleType));
                LOG.info(String.format("Detected console sync path: %s", syncPath));

                consoleConfiguration.setConnectedConsoleSid(consoleSid);
                consoleConfiguration.setSystemType(consoleType);
                consoleConfiguration.setSystemSyncPath(syncPath);

                updateMessage(resourceBundle.getString(COMPLETE_MESSAGE_KEY));
                updateProgress(1, 1);

                return consoleSid;
            }
        };
    }
}
