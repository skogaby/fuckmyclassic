package com.fuckmyclassic.hibernate.dao.impl;

import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.dao.ApplicationDAO;
import com.fuckmyclassic.hibernate.dao.LibraryDAO;
import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Folder;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.ui.component.UiPropertyContainer;
import com.fuckmyclassic.ui.util.CheckBoxTreeItemUtils;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the LibraryDAO interface using MySQL and Hibernate.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Repository
public class LibraryDAOImpl implements LibraryDAO {

    /** Hibernate manager for higher level interactions. */
    private final HibernateManager hibernateManager;
    /** Hibernate session for database interaction at a low level. */
    private final Session session;
    /** DAO to load application data. */
    private final ApplicationDAO applicationDAO;
    /** Container for UI properties we need to update */
    private final UiPropertyContainer uiPropertyContainer;

    @Autowired
    public LibraryDAOImpl(final HibernateManager hibernateManager, final Session session,
                          final ApplicationDAO applicationDAO, final UiPropertyContainer uiPropertyContainer) {
        this.hibernateManager = hibernateManager;
        this.session = session;
        this.applicationDAO = applicationDAO;
        this.uiPropertyContainer = uiPropertyContainer;
    }

    /**
     * Fetches the metadata for all of the libraries for a given console,
     * or creates a default library if none exists for this console.
     * @param consoleSid The console SID to fetch the libraries for
     * @return The list of Library metadata items for the given console
     */
    @Override
    public List<Library> getOrCreateLibrariesForConsole(String consoleSid) {
        List<Library> results = getLibrariesForConsole(consoleSid);

        // create a default library if none exists
        if (results.isEmpty()) {
            final Library defaultLibrary = new Library(consoleSid, 0, SharedConstants.DEFAULT_LIBRARY_NAME);
            hibernateManager.saveEntity(defaultLibrary);
            results.add(defaultLibrary);
        }

        return results;
    }

    /**
     * Fetches the metadata for all of the libraries for a given console.
     * @param consoleSid The console SID to fetch the libraries for
     * @return The list of Library metadata items for the given console
     */
    @Override
    public List<Library> getLibrariesForConsole(String consoleSid) {
        final Query<Library> query = session.createQuery("from Library where console_sid = :console_sid");
        query.setParameter("console_sid", consoleSid);
        return query.getResultList();
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
     * @param useCheckboxes Whether or not to make the tree items CheckBoxTreeItems
     */
    @Override
    public int loadApplicationsForFolder(TreeItem<LibraryItem> parentFolder, Library library, boolean useCheckboxes) {
        // get all the applications in the top-level folder
        final Query<LibraryItem> query = session.createQuery(
                "from LibraryItem l where l.library = :library and l.folder = :folder");
        query.setParameter("library", library);
        query.setParameter("folder", parentFolder.getValue().getApplication());

        final List<TreeItem<LibraryItem>> itemResults = query.getResultStream()
                .map(r -> {
                    final TreeItem<LibraryItem> item;

                    if (useCheckboxes) {
                        item = new CheckBoxTreeItem<>(r, null, r.isSelected(), false);
                        CheckBoxTreeItemUtils.setCheckListenerOnTreeItem((CheckBoxTreeItem) item, this.hibernateManager, this.uiPropertyContainer);
                    } else {
                        item = new TreeItem<>(r);
                    }

                    return item;
                })
                .collect(Collectors.toList());
        parentFolder.getChildren().addAll(itemResults);

        // iterate through the results and recurse down for any folders that are inside this one
        int numNodes = 0;
        int numSelected = 0;

        for (TreeItem<LibraryItem> itemResult : itemResults) {
            if (itemResult.getValue().getApplication() instanceof Folder) {
                itemResult.setExpanded(true);
                numNodes += 1 + loadApplicationsForFolder(itemResult, library, useCheckboxes);
            } else {
                numNodes++;
            }

            // set indeterminate values for checkboxes properly
            if (useCheckboxes &&
                    ((CheckBoxTreeItem) itemResult).isSelected()) {
                numSelected++;
            }
        }

        // if less than all the items were selected, the parent is indeterminant
        if (useCheckboxes){
            if (numSelected > 0 &&
                    numSelected < itemResults.size()) {
                // small hack to change the value of the parent without altering
                // the values of what's below it
                ((CheckBoxTreeItem) parentFolder).setIndependent(true);
                ((CheckBoxTreeItem) parentFolder).setSelected(false);
                ((CheckBoxTreeItem) parentFolder).setIndeterminate(true);
                ((CheckBoxTreeItem) parentFolder).setIndependent(false);
            }
        }

        return numNodes;
    }

    /**
     * Loads a library from the database, given a console SID and a library ID.
     * @param library The metadata for the library that needs to be loaded.
     * @param useCheckboxes Whether or not to make the tree items CheckBoxTreeItems
     * @return A tree representing the requested library.
     */
    @Override
    public TreeItem<LibraryItem> loadApplicationTreeForLibrary(Library library, boolean useCheckboxes) {
        // check if the HOME folder exists, create one if it doesn't
        Application homeFolder = this.applicationDAO.loadApplicationByAppId(SharedConstants.HOME_FOLDER_ID);

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
        final TreeItem<LibraryItem> homeItem;

        if (useCheckboxes) {
            homeItem = new CheckBoxTreeItem<>(homeFolderItem, null,
                    homeFolderItem.isSelected(), false);
            CheckBoxTreeItemUtils.setCheckListenerOnTreeItem((CheckBoxTreeItem) homeItem, this.hibernateManager, this.uiPropertyContainer);
        } else {
            homeItem = new TreeItem<>(homeFolderItem, null);
        }

        homeItem.setExpanded(true);
        homeItem.getValue().setNumNodes(loadApplicationsForFolder(homeItem, library, useCheckboxes) + 1);
        this.uiPropertyContainer.numSelected.set(getNumSelectedForLibrary(library));

        return homeItem;
    }

    /**
     * Gets the number of selected items for a given library (excluding folders).
     * @param library The library to query for.
     * @return The number of selected items in the library (excluding folders).
     */
    public long getNumSelectedForLibrary(Library library) {
        final Query query = session.createQuery(
                "select count(*) from LibraryItem l where l.library = :library and l.selected = true and l.application.class = Application");
        query.setParameter("library", library);
        return (Long) query.uniqueResult();
    }
}
