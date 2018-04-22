package com.fuckmyclassic.network;

import com.fuckmyclassic.task.impl.GetConsoleSidTask;
import com.fuckmyclassic.task.impl.LoadLibrariesTask;
import com.fuckmyclassic.task.impl.UpdateUnknownLibrariesTask;
import com.fuckmyclassic.ui.controller.SequentialTaskRunnerDialog;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Default listener for new console connections to the app that gets
 * registered on app start.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class SshConnectionListenerImpl implements SshConnectionListener {

    static Logger LOG = LogManager.getLogger(SshConnectionListenerImpl.class.getName());

    private static final String ON_CONNECT_TASK_MESSAGE_KEY = "NewConnectionTaskLabel";

    /** Resource bundle for internationalized task strings. */
    private final ResourceBundle tasksResourceBundle;
    /** The dialog to run sequential tasks. */
    private final SequentialTaskRunnerDialog sequentialTaskRunnerDialog;

    // Tasks that we run on detection of a new console connection
    private final GetConsoleSidTask getConsoleSidTask;
    private final UpdateUnknownLibrariesTask updateUnknownLibrariesTask;
    private final LoadLibrariesTask loadLibrariesTask;

    @Autowired
    public SshConnectionListenerImpl(final ResourceBundle resourceBundle,
                                     final SequentialTaskRunnerDialog sequentialTaskRunnerDialog,
                                     final GetConsoleSidTask getConsoleSidTask,
                                     final UpdateUnknownLibrariesTask updateUnknownLibrariesTask,
                                     final LoadLibrariesTask loadLibrariesTask) {
        this.tasksResourceBundle = resourceBundle;
        this.sequentialTaskRunnerDialog = sequentialTaskRunnerDialog;
        this.getConsoleSidTask = getConsoleSidTask;
        this.updateUnknownLibrariesTask = updateUnknownLibrariesTask;
        this.loadLibrariesTask = loadLibrariesTask;
    }

    /**
     * Handles all the tasks that need to be performed when a console first connects.
     */
    @Override
    public void onSshConnected() {
        LOG.info("New console connected");

        Platform.runLater(() -> {
            try {
                sequentialTaskRunnerDialog.setMainTaskMessage(this.tasksResourceBundle.getString(ON_CONNECT_TASK_MESSAGE_KEY));
                sequentialTaskRunnerDialog.setTaskCreators(getConsoleSidTask, updateUnknownLibrariesTask, loadLibrariesTask);
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
