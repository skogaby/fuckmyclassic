package com.fuckmyclassic.ui.util;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;

/**
 * Class to help with JavaFX Platform related functions.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class PlatformUtils {

    static Logger LOG = LogManager.getLogger(PlatformUtils.class.getName());

    /**
     * Runs the specified {@link Runnable} on the
     * JavaFX application thread and waits for completion.
     *
     * @param action the {@link Runnable} to run
     * @throws NullPointerException if {@code action} is {@code null}
     */
    public static void runAndWait(Runnable action) {
        if (action == null) {
            throw new NullPointerException();
        }

        // run synchronously on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        // queue on JavaFX thread and wait for completion
        final CountDownLatch doneLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                doneLatch.countDown();
            }
        });

        try {
            doneLatch.await();
        } catch (InterruptedException e) {
            // ignore exception
            LOG.error(e);
        }
    }
}
