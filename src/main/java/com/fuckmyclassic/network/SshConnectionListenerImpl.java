package com.fuckmyclassic.network;

import com.fuckmyclassic.hibernate.dao.ConsoleDAO;
import com.fuckmyclassic.model.Console;
import com.fuckmyclassic.task.TaskProvider;
import com.fuckmyclassic.ui.controller.SequentialTaskRunnerDialog;
import com.fuckmyclassic.ui.util.PlatformUtils;
import com.fuckmyclassic.userconfig.UserConfiguration;
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
    /** Provider for the Tasks we need on new connection */
    private final TaskProvider taskProvider;
    /** DAO to get consoles based on their IPs */
    private final ConsoleDAO consoleDAO;
    /** User's configuration of their session */
    private final UserConfiguration userConfiguration;
    /** Manager of network operations */
    private final NetworkManager networkManager;

    @Autowired
    public SshConnectionListenerImpl(final ResourceBundle resourceBundle,
                                     final SequentialTaskRunnerDialog sequentialTaskRunnerDialog,
                                     final TaskProvider taskProvider,
                                     final ConsoleDAO consoleDAO,
                                     final UserConfiguration userConfiguration,
                                     final NetworkManager networkManager) {
        this.tasksResourceBundle = resourceBundle;
        this.sequentialTaskRunnerDialog = sequentialTaskRunnerDialog;
        this.taskProvider = taskProvider;
        this.consoleDAO = consoleDAO;
        this.userConfiguration = userConfiguration;
        this.networkManager = networkManager;
    }

    /**
     * Handles all the tasks that need to be performed when a console first connects.
     */
    @Override
    public void onSshConnected(final String address) {
        LOG.info(String.format("New console connected. Address: %s", address));

        // identify the console and load its libraries
        PlatformUtils.runAndWait(() -> {
            try {
                taskProvider.identifyConnectedConsoleTask.setDstAddress(address);
                taskProvider.loadLibrariesTask.setShouldRefreshConsoles(true);

                sequentialTaskRunnerDialog.setMainTaskMessage(this.tasksResourceBundle.getString(ON_CONNECT_TASK_MESSAGE_KEY));
                sequentialTaskRunnerDialog.setTaskCreators(taskProvider.identifyConnectedConsoleTask,
                        taskProvider.updateUnknownLibrariesTask, taskProvider.loadLibrariesTask);
                sequentialTaskRunnerDialog.showDialog();
            } catch (IOException e) {
                LOG.error(e);
            }
        });
    }

    /**
     * Handler for when the console disconnects.
     */
    @Override
    public void onSshDisconnected(final String address) {
        LOG.info(String.format("Console disconnected: %s", address));

        // remove the connected console from the UserConfiguration's collection
        // of connected consoles, as well as the NetworkManager's collection
        final Console console = this.consoleDAO.getConsoleForLastKnownAddress(address);

        if (console != null) {
            this.userConfiguration.removeConnectedConsole(console);
            this.networkManager.disconnect(console.getLastKnownAddress());
        }
    }
}
