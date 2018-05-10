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
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ResourceBundle;

/**
 * Task to show the splash screen on the console while we perform operations on it.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class ShowSplashScreenAndStopUiTask extends AbstractTaskCreator<Void> {

    static Logger LOG = LogManager.getLogger(ShowSplashScreenAndStopUiTask.class.getName());

    private final String IN_PROGRESS_MESSAGE_KEY = "ShowSplashScreenTask.inProgressMessage";
    private final String COMPLETE_MESSAGE_KEY = "ShowSplashScreenTask.completeMessage";

    /** The name of the splash screen file */
    public static String SPLASH_SCREEN = "images/splash.gz";
    /** The actual path of the splash screen file */
    private final String SPLASH_SCREEN_PATH = Paths.get(ClassLoader.getSystemResource(SPLASH_SCREEN).toURI()).toString();

    /** Network manager, to send commands to consoles */
    private final NetworkManager networkManager;
    /** User configuration for the current session */
    private final UserConfiguration userConfiguration;
    /** Bundle for getting localized strings. */
    private final ResourceBundle resourceBundle;

    @Autowired
    public ShowSplashScreenAndStopUiTask(final NetworkManager networkManager,
                                         final UserConfiguration userConfiguration,
                                         final ResourceBundle resourceBundle) throws URISyntaxException {
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

                LOG.info(String.format("Showing splash screen on \"%s\"", console.getNickname()));

                networkManager.runCommand(console.getLastKnownAddress(), "uistop; usleep 500000");
                int result = networkManager.runCommandWithStreams(console.getLastKnownAddress(),
                        "gunzip -c - > /dev/fb0",
                        new FileInputStream(SPLASH_SCREEN_PATH), null, null);

                if (result != 0) {
                    throw new RuntimeException("Couldn't show splash screen on console");
                }

                LOG.info("Done showing splash screen");

                updateMessage(resourceBundle.getString(COMPLETE_MESSAGE_KEY));
                updateProgress(1, 1);

                return null;
            }
        };
    }
}
