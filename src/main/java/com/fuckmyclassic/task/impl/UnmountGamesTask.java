package com.fuckmyclassic.task.impl;

import com.fuckmyclassic.network.NetworkManager;
import com.fuckmyclassic.task.AbstractTaskCreator;
import com.fuckmyclassic.userconfig.UserConfiguration;
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

    /** Network manager, to send commands to consoles */
    private final NetworkManager networkManager;
    /** User configuration for the current session */
    private final UserConfiguration userConfiguration;
    /** Bundle for getting localized strings. */
    private final ResourceBundle resourceBundle;

    @Autowired
    public UnmountGamesTask(final NetworkManager networkManager,
                            final UserConfiguration userConfiguration,
                            final ResourceBundle resourceBundle) {
        this.networkManager = networkManager;
        this.userConfiguration = userConfiguration;
        this.resourceBundle = resourceBundle;
    }

    @Override
    public Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws IOException, JSchException {
                updateMessage(resourceBundle.getString(IN_PROGRESS_MESSAGE_KEY));
                updateProgress(0, 1);

                LOG.info(String.format("Unmounting the games directory on \"%s\"", userConfiguration.getSelectedConsole().getNickname()));
                networkManager.runCommand("hakchi eval 'umount \"$gamepath\"'");
                LOG.info("Done unmounting the games directory");

                updateMessage(resourceBundle.getString(COMPLETE_MESSAGE_KEY));
                updateProgress(1, 1);

                return null;
            }
        };
    }
}
