package com.fuckmyclassic.hibernate;

import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Folder;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.shared.SharedConstants;
import javafx.scene.control.TreeItem;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the ApplicationDAO interface.
 */
@Repository
public class ApplicationDAOImpl implements ApplicationDAO {

    /**
     * Hibernate manager for higher level interactions.
     */
    private final HibernateManager hibernateManager;

    /**
     * Hibernate session for database interaction at a low level.
     */
    private final Session session;

    @Autowired
    public ApplicationDAOImpl(final HibernateManager hibernateManager, final Session session) {
        this.hibernateManager = hibernateManager;
        this.session = session;
    }

    /**
     * Loads a specific application by its string ID.
     * @param applicationId The ID string of the application (ex. CLV-S-00000)
     * @return The Application corresponding to the ID
     */
    @Override
    public Application loadApplicationByAppId(final String applicationId) {
        final Query<Application> query = session.createQuery("from Application where application_id = :id");
        query.setParameter("id", applicationId);
        final List<Application> results = query.getResultList();
        Application app = null;

        if (!results.isEmpty()) {
            app = results.get(0);
        }

        return app;
    }

    /**
     * Loads all the applications that are in a given folder (in the given library)
     * @param parentFolder The folder to load the applications from
     * @param library The metadata for the library that needs to be loaded.
     */
    @Override
    public void loadApplicationsForFolder(TreeItem<Application> parentFolder, Library library) {
        // get all the applications in the top-level folder
        final Query<LibraryItem> query = session.createQuery(
                "from LibraryItem l where l.library = :library and l.folder = :folder");
        query.setParameter("library", library);
        query.setParameter("folder", parentFolder.getValue());

        final List<TreeItem<Application>> itemResults = query.getResultStream()
                .map(r -> new TreeItem<>(r.getApplication()))
                .collect(Collectors.toList());
        parentFolder.getChildren().addAll(itemResults);

        // iterate through the results and recurse down for any folders that are inside this one
        for (TreeItem<Application> itemResult : itemResults) {
            if (itemResult.getValue() instanceof Folder) {
                itemResult.setExpanded(true);
                loadApplicationsForFolder(itemResult, library);
            }
        }
    }

    /**
     * Loads a library from the database, given a console SID and a library ID.
     * @param library The metadata for the library that needs to be loaded.
     * @return A tree representing the requested library.
     */
    @Override
    public TreeItem<Application> loadLibraryForConsole(Library library) {
        // check if the HOME folder exists, create one if it doesn't
        Application homeFolder = loadApplicationByAppId(SharedConstants.HOME_FOLDER_ID);

        if (homeFolder == null) {
            homeFolder = new Folder();
            homeFolder.setApplicationName(SharedConstants.HOME_FOLDER_NAME);
            homeFolder.setApplicationId(SharedConstants.HOME_FOLDER_ID);
            hibernateManager.saveEntity(homeFolder);
        }

        // now select all items in the home folder and recurse down for any folders
        final TreeItem<Application> homeItem = new TreeItem<>(homeFolder);
        homeItem.setExpanded(true);
        loadApplicationsForFolder(homeItem, library);

        return homeItem;
    }
}
