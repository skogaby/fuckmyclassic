package com.fuckmyclassic.task.impl;

import com.fuckmyclassic.network.NetworkManager;
import com.fuckmyclassic.task.AbstractTaskCreator;
import com.fuckmyclassic.userconfig.UserConfiguration;
import com.jcraft.jsch.JSchException;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Task to take a screenshot of the actively selected console.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class TakeScreenshotTask extends AbstractTaskCreator<Void> {

    static Logger LOG = LogManager.getLogger(TakeScreenshotTask.class.getName());

    private final String IN_PROGRESS_MESSAGE_KEY = "TakeScreenshotTask.inProgressMessage";
    private final String COMPLETE_MESSAGE_KEY = "TakeScreenshotTask.completeMessage";

    /** Object used to issue SSH commands to the console */
    private final NetworkManager networkManager;
    /** Bundle for getting localized strings. */
    private final ResourceBundle resourceBundle;
    /** User configuration so we can pass the screenshot back to the main thread to save it */
    private final UserConfiguration userConfiguration;

    @Autowired
    public TakeScreenshotTask(final NetworkManager networkManager,
                              final ResourceBundle resourceBundle,
                              final UserConfiguration userConfiguration) {
        this.networkManager = networkManager;
        this.resourceBundle = resourceBundle;
        this.userConfiguration = userConfiguration;
    }

    @Override
    public Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws IOException, JSchException {
                updateMessage(resourceBundle.getString(IN_PROGRESS_MESSAGE_KEY));
                updateProgress(3, 5);

                LOG.debug("Stopping the UI and fetching framebuffer");

                final ByteArrayOutputStream fbData = new ByteArrayOutputStream();
                networkManager.runCommand("hakchi uipause");
                networkManager.runCommandWithStreams("cat /dev/fb0", null, fbData, null);
                updateProgress(4, 5);
                networkManager.runCommand("hakchi uiresume");

                // fetch information about the framebuffer and decode it to a bitmap
                final int stride = Integer.parseInt(networkManager.runCommand("cat /sys/class/graphics/fb0/stride"));
                final String[] virtualSize = networkManager.runCommand("cat /sys/class/graphics/fb0/virtual_size").split(",");
                final int width = Integer.parseInt(virtualSize[0]);
                final int height = Integer.parseInt(virtualSize[1]) / 2;
                final byte[] rawData = fbData.toByteArray();
                final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

                int rawOffset, red, green, blue;

                for (int y = 0; y < height; y++) {
                    rawOffset = y * stride;

                    for (int x = 0; x < width; x++) {
                        blue = rawData[rawOffset] & 0xFF;
                        green = rawData[rawOffset + 1] & 0xFF;
                        red = rawData[rawOffset + 2] & 0xFF;
                        rawOffset += 4;

                        image.setRGB(x, y, new Color(red, green, blue).getRGB());
                    }
                }

                userConfiguration.setLastScreenshot(image);
                LOG.debug("Finished getting the screenshot data from the console");

                updateMessage(resourceBundle.getString(COMPLETE_MESSAGE_KEY));
                updateProgress(5, 5);

                return null;
            }
        };
    }
}
