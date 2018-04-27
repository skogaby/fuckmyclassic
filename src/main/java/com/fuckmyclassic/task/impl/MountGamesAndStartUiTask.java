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
 * Task to overmount the games directory and start the UI back up.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class MountGamesAndStartUiTask extends AbstractTaskCreator<Void> {

    static Logger LOG = LogManager.getLogger(MountGamesAndStartUiTask.class.getName());

    private final String IN_PROGRESS_MESSAGE_KEY = "MountGamesAndStartUiTask.inProgressMessage";
    private final String COMPLETE_MESSAGE_KEY = "MountGamesAndStartUiTask.completeMessage";

    /** Network connection to the console */
    private final NetworkConnection networkConnection;
    /** Bundle for getting localized strings. */
    private final ResourceBundle resourceBundle;

    @Autowired
    public MountGamesAndStartUiTask(final NetworkConnection networkConnection,
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

                LOG.info("Overmounting the games directory and starting the UI");
                networkConnection.runCommand("hakchi overmount_games; uistart");
                LOG.info("Done unmounting the games directory");

                updateMessage(resourceBundle.getString(COMPLETE_MESSAGE_KEY));
                updateProgress(1, 1);

                return null;
            }
        };
    }
}
