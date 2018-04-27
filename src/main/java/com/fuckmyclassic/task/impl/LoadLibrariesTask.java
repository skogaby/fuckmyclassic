package com.fuckmyclassic.task.impl;

import com.fuckmyclassic.hibernate.dao.LibraryDAO;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.task.AbstractTaskCreator;
import com.fuckmyclassic.ui.controller.MainWindow;
import com.fuckmyclassic.userconfig.ConsoleConfiguration;
import com.fuckmyclassic.userconfig.UserConfiguration;
import javafx.application.Platform;
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

    /** Current user configuration*/
    private final UserConfiguration userConfiguration;
    /** Configuration about the current console  */
    private final ConsoleConfiguration consoleConfiguration;
    /** DAO for querying for libraries */
    private final LibraryDAO libraryDAO;
    /** Bundle for getting localized strings. */
    private final ResourceBundle resourceBundle;
    /** The main window, so we can refresh the views */
    private MainWindow mainWindow;

    @Autowired
    public LoadLibrariesTask(final UserConfiguration userConfiguration, final ConsoleConfiguration consoleConfiguration,
                             final LibraryDAO libraryDAO, final ResourceBundle resourceBundle) {
        this.userConfiguration = userConfiguration;
        this.consoleConfiguration = consoleConfiguration;
        this.libraryDAO = libraryDAO;
        this.resourceBundle = resourceBundle;
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

                final String connectedSid = consoleConfiguration.getConnectedConsoleSid();
                LOG.info(String.format("Loading libraries for console %s", connectedSid));

                // first load the libraries and setup the combobox for library selection
                final List<Library> libraries = libraryDAO.getLibrariesForConsole(connectedSid);
                final ObservableList<Library> items = FXCollections.observableArrayList(libraries);
                final Library library;

                // load the last used library, or the first one if there's no config value yet or the new console
                // is different from the last one
                if (userConfiguration.getLastLibraryID() == -1L ||
                        (!userConfiguration.getLastConsoleSID().equals(SharedConstants.DEFAULT_CONSOLE_SID) &&
                        !userConfiguration.getLastConsoleSID().equals(connectedSid))) {
                    library = items.get(0);
                } else {
                    library = items.stream().filter(l -> l.getId() == userConfiguration.getLastLibraryID())
                            .collect(Collectors.toList()).get(0);
                }

                userConfiguration.setLastLibraryID(library.getId());
                userConfiguration.setLastConsoleSID(connectedSid);

                Platform.runLater(() -> {
                    mainWindow.cmbCurrentCollection.setItems(items);
                    mainWindow.cmbCurrentCollection.getSelectionModel().select(library);
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
}
