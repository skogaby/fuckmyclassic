package com.fuckmyclassic.task.impl;

import com.fuckmyclassic.network.NetworkConnection;
import com.fuckmyclassic.task.AbstractTaskCreator;
import com.jcraft.jsch.JSchException;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Task to unmount the games directory so we can sync games to it.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class UnmountGamesTask extends AbstractTaskCreator<Void> {

    static Logger LOG = LogManager.getLogger(UnmountGamesTask.class.getName());

    private final String IN_PROGRESS_MESSAGE_KEY = "UnmountGamesTask.inProgressMessage";
    private final String COMPLETE_MESSAGE_KEY = "UnmountGamesTask.completeMessage";

    /** Network connection to the console */
    private final NetworkConnection networkConnection;
    /** Bundle for getting localized strings. */
    private final ResourceBundle resourceBundle;

    @Autowired
    public UnmountGamesTask(final NetworkConnection networkConnection,
                            final ResourceBundle resourceBundle) {
        this.networkConnection = networkConnection;
        this.resourceBundle = resourceBundle;
    }

    @Override
    public Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws IOException, JSchException {
                updateMessage(resourceBundle.getString(IN_PROGRESS_MESSAGE_KEY));
                updateProgress(0, 1);

                LOG.info("Unmounting the games directory");
                networkConnection.runCommand("hakchi eval 'umount \"$gamepath\"'");
                LOG.info("Done unmounting the games directory");

                updateMessage(resourceBundle.getString(COMPLETE_MESSAGE_KEY));
                updateProgress(1, 1);

                return null;
            }
        };
    }
}
