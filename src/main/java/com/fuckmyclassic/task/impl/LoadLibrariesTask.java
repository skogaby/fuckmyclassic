package com.fuckmyclassic.task.impl;

import com.fuckmyclassic.hibernate.dao.impl.ConsoleDAO;
import com.fuckmyclassic.hibernate.dao.impl.LibraryDAO;
import com.fuckmyclassic.model.Console;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.task.AbstractTaskCreator;
import com.fuckmyclassic.ui.controller.MainWindow;
import com.fuckmyclassic.ui.util.PlatformUtils;
import com.fuckmyclassic.userconfig.UserConfiguration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Task to load the libraries and initialize the game view for the newly
 * connected console if it's different than the last console that was connected.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class LoadLibrariesTask extends AbstractTaskCreator<Void> {

    static Logger LOG = LogManager.getLogger(LoadLibrariesTask.class.getName());

    private final String IN_PROGRESS_MESSAGE_KEY = "LoadLibrariesTask.inProgressMessage";
    private final String COMPLETE_MESSAGE_KEY = "LoadLibrariesTask.completeMessage";

    /** Current user configuration */
    private final UserConfiguration userConfiguration;
    /** DAO for querying for libraries */
    private final LibraryDAO libraryDAO;
    /** The DAO for accessing known consoles */
    private final ConsoleDAO consoleDAO;
    /** Bundle for getting localized strings. */
    private final ResourceBundle resourceBundle;
    /** The main window, so we can refresh the views */
    private MainWindow mainWindow;
    /** Says whether the console combobox should refresh */
    private boolean shouldRefreshConsoles;

    @Autowired
    public LoadLibrariesTask(final UserConfiguration userConfiguration,
                             final LibraryDAO libraryDAO,
                             final ConsoleDAO consoleDAO,
                             final ResourceBundle resourceBundle) {
        this.userConfiguration = userConfiguration;
        this.libraryDAO = libraryDAO;
        this.consoleDAO = consoleDAO;
        this.resourceBundle = resourceBundle;
        this.shouldRefreshConsoles = false;
    }

    @Override
    public Task<Void> createTask() {
        if (this.mainWindow == null) {
            LOG.error("Called the LoadLibrariesTask before assigning the MainWindow");
            throw new RuntimeException();
        }

        return new Task<Void>() {
            @Override
            protected Void call() {
                updateMessage(resourceBundle.getString(IN_PROGRESS_MESSAGE_KEY));
                updateProgress(0, 1);

                // first, refresh the consoles view, in case we deleted the UNKNOWN console
                // or created a new console
                LOG.info("Refreshing the consoles list");
                final List<Console> consoles = consoleDAO.getAllConsoles();
                final ObservableList<Console> consoleItems = FXCollections.observableArrayList(consoles);

                final String connectedSid = userConfiguration.getSelectedConsole().getConsoleSid();
                LOG.info(String.format("Loading libraries for console %s", connectedSid));

                // next, load the libraries and setup the combobox for library selection
                final List<Library> libraries = libraryDAO.getOrCreateLibrariesForConsole(connectedSid);
                final ObservableList<Library> libraryItems = FXCollections.observableArrayList(libraries);
                final Library library;

                // load the last used library, or the first one if there's no config value yet or the new console
                // is different from the last one
                if (userConfiguration.getSelectedLibraryID() == -1L ||
                        (!userConfiguration.getLastConsoleSID().equals(SharedConstants.DEFAULT_CONSOLE_SID) &&
                        !userConfiguration.getLastConsoleSID().equals(connectedSid))) {
                    library = libraryItems.get(0);
                } else {
                    library = libraryItems.stream().filter(l -> l.getId() == userConfiguration.getSelectedLibraryID())
                            .collect(Collectors.toList()).get(0);
                }

                userConfiguration.setSelectedLibraryID(library.getId());
                userConfiguration.setLastConsoleSID(connectedSid);

                PlatformUtils.runAndWait(() -> {
                    if (shouldRefreshConsoles) {
                        mainWindow.cmbCurrentConsole.getItems().clear();
                        mainWindow.cmbCurrentConsole.getItems().addAll(consoleItems);

                        mainWindow.shouldConsoleListenerRespond = false;
                        mainWindow.cmbCurrentConsole.setValue(userConfiguration.getSelectedConsole());
                        mainWindow.shouldConsoleListenerRespond = true;

                        shouldRefreshConsoles = false;
                    }

                    mainWindow.cmbCurrentLibrary.getItems().clear();
                    mainWindow.cmbCurrentLibrary.getItems().addAll(libraryItems);
                    mainWindow.cmbCurrentLibrary.setValue(library);
                });

                updateMessage(resourceBundle.getString(COMPLETE_MESSAGE_KEY));
                updateProgress(1, 1);

                return null;
            }
        };
    }

    public LoadLibrariesTask setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        return this;
    }

    public LoadLibrariesTask setShouldRefreshConsoles(boolean shouldRefreshConsoles) {
        this.shouldRefreshConsoles = shouldRefreshConsoles;
        return this;
    }
}
