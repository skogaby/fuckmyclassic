package com.fuckmyclassic.hibernate;

import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Folder;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.shared.SharedConstants;
import javafx.scene.control.CheckBoxTreeItem;
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
    public Application loadApplicationByAppId(String applicationId) {
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
     * Loads the LibraryItem corresponding to the given application and library.
     * @param application The Application that corresponds to the item
     * @param library The Library that corresponds to the item
     * @return The LibraryItem corresponding to the given data
     */
    public LibraryItem loadLibraryItemByApplication(Application application, Library library) {
        final Query<LibraryItem> query = session.createQuery(
                "from LibraryItem l where l.application = :application and l.library = :library");
        query.setParameter("application", application);
        query.setParameter("library", library);

        final List<LibraryItem> results = query.getResultList();
        LibraryItem item = null;

        if (!results.isEmpty()) {
            item = results.get(0);
        }

        return item;
    }

    /**
     * Loads all the applications that are in a given folder (in the given library), inserting
     * them into the given Tree structure represented by parentFolder.
     * @param parentFolder The folder to load the applications from and insert the nodes under
     * @param library The metadata for the library that needs to be loaded.
     */
    @Override
    public void loadApplicationsForFolder(CheckBoxTreeItem<LibraryItem> parentFolder, Library library) {
        // get all the applications in the top-level folder
        final Query<LibraryItem> query = session.createQuery(
                "from LibraryItem l where l.library = :library and l.folder = :folder");
        query.setParameter("library", library);
        query.setParameter("folder", parentFolder.getValue().getApplication());

        final List<CheckBoxTreeItem<LibraryItem>> itemResults = query.getResultStream()
                .map(r -> {
                    final CheckBoxTreeItem<LibraryItem> item = new CheckBoxTreeItem<>(r, null, r.isSelected(), true);

                    item.selectedProperty().addListener(((observable, oldValue, newValue) -> {
                        final LibraryItem libraryItem = item.getValue();
                        boolean old = libraryItem.isSelected();

                        if (newValue != old) {
                            libraryItem.setSelected(newValue);
                            this.hibernateManager.updateEntity(libraryItem);
                        }
                    }));

                    return item;
                })
                .collect(Collectors.toList());
        parentFolder.getChildren().addAll(itemResults);

        // iterate through the results and recurse down for any folders that are inside this one
        for (CheckBoxTreeItem<LibraryItem> itemResult : itemResults) {
            if (itemResult.getValue().getApplication() instanceof Folder) {
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
    public CheckBoxTreeItem<LibraryItem> loadApplicationTreeForLibrary(Library library) {
        // check if the HOME folder exists, create one if it doesn't
        Application homeFolder = loadApplicationByAppId(SharedConstants.HOME_FOLDER_ID);

        if (homeFolder == null) {
            homeFolder = new Folder();
            homeFolder.setApplicationName(SharedConstants.HOME_FOLDER_NAME);
            homeFolder.setApplicationId(SharedConstants.HOME_FOLDER_ID);
            hibernateManager.saveEntity(homeFolder);
        }

        // create a LibraryItem for the home folder if one doesn't exist
        LibraryItem homeFolderItem = loadLibraryItemByApplication(homeFolder, library);

        if (homeFolderItem == null) {
            homeFolderItem = new LibraryItem()
                    .setFolder(null)
                    .setApplication(homeFolder)
                    .setLibrary(library)
                    .setSelected(true);
            hibernateManager.saveEntity(homeFolderItem);
        }

        // now select all items in the home folder and recurse down for any folders
        final CheckBoxTreeItem<LibraryItem> homeItem = new CheckBoxTreeItem<>(homeFolderItem, null, true, true);
        homeItem.setExpanded(true);
        loadApplicationsForFolder(homeItem, library);

        return homeItem;
    }
}
