package com.fuckmyclassic.task.impl;

import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.dao.LibraryDAO;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.task.AbstractTaskCreator;
import com.fuckmyclassic.userconfig.ConsoleConfiguration;
import com.fuckmyclassic.userconfig.UserConfiguration;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ResourceBundle;

/**
 * Task to update any libraries in the database whose console SID is "UNKNOWN"
 * to belong to the currently connected console.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class UpdateUnknownLibrariesTask extends AbstractTaskCreator<Void> {

    static Logger LOG = LogManager.getLogger(UpdateUnknownLibrariesTask.class.getName());

    private final String IN_PROGRESS_MESSAGE_KEY = "UpdateUnknownLibrariesTask.inProgressMessage";
    private final String COMPLETE_MESSAGE_KEY = "UpdateUnknownLibrariesTask.completeMessage";

    /** Current user configuration */
    private final UserConfiguration userConfiguration;
    /** The configuration about the currently connected console */
    private final ConsoleConfiguration consoleConfiguration;
    /** Hibernate manager to update libraries */
    private final HibernateManager hibernateManager;
    /** DAO for querying for libraries */
    private final LibraryDAO libraryDAO;
    /** Bundle for getting localized strings. */
    private final ResourceBundle resourceBundle;

    @Autowired
    public UpdateUnknownLibrariesTask(final UserConfiguration userConfiguration,
                                      final ConsoleConfiguration consoleConfiguration,
                                      final HibernateManager hibernateManager,
                                      final LibraryDAO libraryDAO,
                                      final ResourceBundle resourceBundle) {
        this.userConfiguration = userConfiguration;
        this.consoleConfiguration = consoleConfiguration;
        this.hibernateManager = hibernateManager;
        this.libraryDAO = libraryDAO;
        this.resourceBundle = resourceBundle;
    }

    @Override
    public Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() {
                updateMessage(resourceBundle.getString(IN_PROGRESS_MESSAGE_KEY));
                updateProgress(0, 1);

                if (userConfiguration.getLastConsoleSID().equals(SharedConstants.DEFAULT_CONSOLE_SID) &&
                        consoleConfiguration.getConnectedConsoleSid() != null) {
                    final List<Library> libraries = libraryDAO.getLibrariesForConsole(SharedConstants.DEFAULT_CONSOLE_SID);
                    LOG.info(String.format("Updating %d libraries to belong to the connected console", libraries.size()));

                    libraries.forEach(l -> {
                        l.setConsoleSid(consoleConfiguration.getConnectedConsoleSid());
                        hibernateManager.updateEntity(l);
                    });
                }

                updateMessage(resourceBundle.getString(COMPLETE_MESSAGE_KEY));
                updateProgress(1, 1);

                return null;
            }
        };
    }
}
