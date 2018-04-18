package com.fuckmyclassic.task;

import com.fuckmyclassic.network.NetworkConnection;
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
public class GetConsoleSidTask extends AbstractTaskCreator<String> {

    static Logger LOG = LogManager.getLogger(GetConsoleSidTask.class.getName());

    private final String IN_PROGRESS_MESSAGE_KEY = "GetConsoleSidTask.inProgressMessage";
    private final String COMPLETE_MESSAGE_KEY = "GetConsoleSidTask.completeMessage";

    /**
     * The connection used for SSH commands.
     */
    private final NetworkConnection networkConnection;

    /**
     * Bundle for getting localized strings.
     */
    private final ResourceBundle resourceBundle;

    @Autowired
    public GetConsoleSidTask(final ResourceBundle resourceBundle, final NetworkConnection networkConnection) {
        this.resourceBundle = resourceBundle;
        this.networkConnection = networkConnection;
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
                LOG.info(String.format("Detected console SID: %s", consoleSid));

                updateMessage(resourceBundle.getString(COMPLETE_MESSAGE_KEY));
                updateProgress(1, 1);

                return consoleSid;
            }
        };
    }
}
