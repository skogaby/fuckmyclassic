package com.fuckmyclassic.task.impl;

import com.fuckmyclassic.model.Console;
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
 * Task to overmount the games directory and start the UI back up.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class MountGamesAndStartUiTask extends AbstractTaskCreator<Void> {

    static Logger LOG = LogManager.getLogger(MountGamesAndStartUiTask.class.getName());

    private final String IN_PROGRESS_MESSAGE_KEY = "MountGamesAndStartUiTask.inProgressMessage";
    private final String COMPLETE_MESSAGE_KEY = "MountGamesAndStartUiTask.completeMessage";

    /** Network manager, to send commands to consoles */
    private final NetworkManager networkManager;
    /** User configuration for the current session */
    private final UserConfiguration userConfiguration;
    /** Bundle for getting localized strings. */
    private final ResourceBundle resourceBundle;

    @Autowired
    public MountGamesAndStartUiTask(final NetworkManager networkManager,
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

                final Console console = userConfiguration.getSelectedConsole();
                LOG.info(String.format("Overmounting the games directory and starting the UI on \"%s\"",
                        console.getNickname()));
                networkManager.runCommand(userConfiguration.getSelectedConsole().getLastKnownAddress(),
                        "hakchi overmount_games; uistart");
                LOG.info("Done unmounting the games directory");

                updateMessage(resourceBundle.getString(COMPLETE_MESSAGE_KEY));
                updateProgress(1, 1);

                return null;
            }
        };
    }
}
