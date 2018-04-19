package com.fuckmyclassic.task;

import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.LibraryDAO;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.network.NetworkConnection;
import com.fuckmyclassic.shared.SharedConstants;
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
 */
@Component
public class UpdateUnknownLibrariesTask extends AbstractTaskCreator<Void> {

    static Logger LOG = LogManager.getLogger(UpdateUnknownLibrariesTask.class.getName());

    private final String IN_PROGRESS_MESSAGE_KEY = "UpdateUnknownLibrariesTask.inProgressMessage";
    private final String COMPLETE_MESSAGE_KEY = "UpdateUnknownLibrariesTask.completeMessage";

    /**
     * Current user configuration
     */
    private final UserConfiguration userConfiguration;

    /**
     * Current handle to the console's network
     */
    private final NetworkConnection networkConnection;

    /**
     * Hibernate manager to update libraries
     */
    private final HibernateManager hibernateManager;

    /**
     * DAO for querying for libraries
     */
    private final LibraryDAO libraryDAO;

    /**
     * Bundle for getting localized strings.
     */
    private final ResourceBundle resourceBundle;

    @Autowired
    public UpdateUnknownLibrariesTask(final UserConfiguration userConfiguration,
                                      final NetworkConnection networkConnection,
                                      final HibernateManager hibernateManager,
                                      final LibraryDAO libraryDAO,
                                      final ResourceBundle resourceBundle) {
        this.userConfiguration = userConfiguration;
        this.networkConnection = networkConnection;
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
                        networkConnection.getConnectedConsoleSid() != null) {
                    final List<Library> libraries = libraryDAO.getLibrariesForConsole(SharedConstants.DEFAULT_CONSOLE_SID);
                    LOG.info(String.format("Updating %d libraries to belong to the connected console", libraries.size()));

                    libraries.forEach(l -> {
                        l.setConsoleSid(networkConnection.getConnectedConsoleSid());
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
