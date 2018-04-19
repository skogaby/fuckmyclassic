package com.fuckmyclassic.network;

import com.fuckmyclassic.task.GetConsoleSidTask;
import com.fuckmyclassic.task.UpdateUnknownLibrariesTask;
import com.fuckmyclassic.ui.controller.SequentialTaskRunnerDialog;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Default listener for new console connections to the app that gets
 * registered on app start.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class SshConnectionListenerImpl implements SshConnectionListener {

    static Logger LOG = LogManager.getLogger(SshConnectionListenerImpl.class.getName());

    /**
     * The dialog to run sequential tasks.
     */
    private final SequentialTaskRunnerDialog sequentialTaskRunnerDialog;

    /**
     * Task to get the console's SID.
     */
    private final GetConsoleSidTask getConsoleSidTask;

    /**
     * Task to update unowned libraries to be owned by the newly connected console.
     */
    private final UpdateUnknownLibrariesTask updateUnknownLibrariesTask;

    @Autowired
    public SshConnectionListenerImpl(final SequentialTaskRunnerDialog sequentialTaskRunnerDialog,
                                     final GetConsoleSidTask getConsoleSidTask,
                                     final UpdateUnknownLibrariesTask updateUnknownLibrariesTask) {
        this.sequentialTaskRunnerDialog = sequentialTaskRunnerDialog;
        this.getConsoleSidTask = getConsoleSidTask;
        this.updateUnknownLibrariesTask = updateUnknownLibrariesTask;
    }

    /**
     * Handles all the tasks that need to be performed when a console first connects.
     */
    @Override
    public void onSshConnected() {
        LOG.info("New console connected");

        // TESTING, REMOVE LATER
        Platform.runLater(() -> {
            try {
                sequentialTaskRunnerDialog.setMainTaskMessage("TESTING");
                sequentialTaskRunnerDialog.setTaskCreators(getConsoleSidTask, updateUnknownLibrariesTask);
                sequentialTaskRunnerDialog.showDialog();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Handler for when the console disconnects.
     */
    @Override
    public void onSshDisconnected() {
        LOG.info("Console disconnected");
    }
}
