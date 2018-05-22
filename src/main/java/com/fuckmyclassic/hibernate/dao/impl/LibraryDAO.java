package com.fuckmyclassic.hibernate.dao.impl;

import com.fuckmyclassic.hibernate.dao.AbstractHibernateDAO;
import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Folder;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.ui.component.UiPropertyContainer;
import com.fuckmyclassic.ui.util.CheckBoxTreeItemUtils;
import com.fuckmyclassic.userconfig.PathConfiguration;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import org.apache.commons.io.FileUtils;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DAO for accessing library data.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Repository
public class LibraryDAO extends AbstractHibernateDAO<Library> {

    /** DAO to load application data */
    private final ApplicationDAO applicationDAO;
    /** DAO to load library item data */
    private final LibraryItemDAO libraryItemDAO;
    /** Container for UI properties we need to update */
    private final UiPropertyContainer uiPropertyContainer;
    /** Configuration object for local paths */
    private final PathConfiguration pathConfiguration;

    @Autowired
    public LibraryDAO(final SessionFactory sessionFactory,
                      final ApplicationDAO applicationDAO,
                      final LibraryItemDAO libraryItemDAO,
                      final UiPropertyContainer uiPropertyContainer,
                      final PathConfiguration pathConfiguration) {
        super(sessionFactory);
        setClazz(Library.class);

        this.applicationDAO = applicationDAO;
        this.libraryItemDAO = libraryItemDAO;
        this.uiPropertyContainer = uiPropertyContainer;
        this.pathConfiguration = pathConfiguration;
    }

    /**
     * Fetches the metadata for all of the libraries for a given console,
     * or creates a default library if none exists for this console.
     * @param consoleSid The console SID to fetch the libraries for
     * @return The list of Library metadata items for the given console
     */
    public List<Library> getOrCreateLibrariesForConsole(final String consoleSid) {
        this.openCurrentSessionWithTransaction();
        List<Library> results = getLibrariesForConsole(consoleSid);

        // create a default library if none exists
        if (results.isEmpty()) {
            final Library defaultLibrary = new Library(consoleSid, SharedConstants.DEFAULT_LIBRARY_NAME);
            super.create(defaultLibrary);
            results.add(defaultLibrary);
        }

        this.closeCurrentSessionwithTransaction();
        return results;
    }

    /**
     * Fetches the metadata for all of the libraries for a given console.
     * @param consoleSid The console SID to fetch the libraries for
     * @return The list of Library metadata items for the given console
     */
    public List<Library> getLibrariesForConsole(final String consoleSid) {
        this.openCurrentSession();

        final Query<Library> query = this.currentSession.createQuery("from Library where console_sid = :console_sid");
        query.setParameter("console_sid", consoleSid);
        final List<Library> libraries = query.getResultList();

        this.closeCurrentSession();
        return libraries;
    }

    /**
     * Loads the LibraryItem corresponding to the given application and library.
     * @param application The Application that corresponds to the item
     * @param library The Library that corresponds to the item
     * @return The LibraryItem corresponding to the given data
     */
    public LibraryItem loadLibraryItemByApplication(final Application application, final Library library) {
        this.openCurrentSession();

        final Query<LibraryItem> query = this.currentSession.createQuery(
                "from LibraryItem l where l.application = :application and l.library = :library");
        query.setParameter("application", application);
        query.setParameter("library", library);

        final List<LibraryItem> results = query.getResultList();
        LibraryItem item = null;

        if (!results.isEmpty()) {
            item = results.get(0);
        }

        this.closeCurrentSession();

        return item;
    }

    /**
     * Loads all the applications that are in a given folder (in the given library), inserting
     * them into the given Tree structure represented by parentFolder.
     * @param parentFolder The folder to load the applications from and insert the nodes under
     * @param library The metadata for the library that needs to be loaded.
     * @param useCheckboxes Whether or not to make the tree items CheckBoxTreeItems
     * @param onlySelected Whether or not to load only the selected library items
     */
    public int loadApplicationsForFolder(TreeItem<LibraryItem> parentFolder, Library library, boolean useCheckboxes, boolean onlySelected) {
        // get all the applications in the top-level folder
        String queryString = "from LibraryItem l where l.library = :library and l.folder = :folder";

        if (onlySelected) {
            queryString += " and l.selected = true";
        }

        this.openCurrentSession();
        final Query<LibraryItem> query = this.currentSession.createQuery(queryString);
        query.setParameter("library", library);
        query.setParameter("folder", parentFolder.getValue().getApplication());

        final List<TreeItem<LibraryItem>> itemResults = query.getResultStream()
                .map(r -> {
                    final TreeItem<LibraryItem> item;

                    if (useCheckboxes) {
                        item = new CheckBoxTreeItem<>(r, null, r.isSelected(), false);
                        CheckBoxTreeItemUtils.setCheckListenerOnTreeItem((CheckBoxTreeItem) item, this.libraryItemDAO, this.uiPropertyContainer);
                    } else {
                        item = new TreeItem<>(r);
                    }

                    final Application application =  item.getValue().getApplication();

                    // calculate the current app size before we return it; it's calculated on
                    // each start instead of saved to the database so that we can detect
                    // manually added files
                    if (!(application instanceof Folder)) {
                        long appSize = FileUtils.sizeOfDirectory(new File(Paths.get(this.pathConfiguration.gamesDirectory,
                                application.getApplicationId()).toString()));

                        // if there's boxart, add the size for that too
                        if (Files.exists(Paths.get(this.pathConfiguration.boxartDirectory, application.getBoxArtPath()))) {
                            appSize += FileUtils.sizeOf(new File(Paths.get(this.pathConfiguration.boxartDirectory,
                                    application.getBoxArtPath()).toString()));
                            appSize += FileUtils.sizeOf(new File(Paths.get(this.pathConfiguration.boxartDirectory,
                                    application.getBoxArtPath().replace(".png", "_small.png")).toString()));
                        }

                        application.setApplicationSize(appSize);
                    }

                    return item;
                })
                .collect(Collectors.toList());
        parentFolder.getChildren().addAll(itemResults);

        // iterate through the results and recurse down for any folders that are inside this one
        int numNodes = 0;
        int numSelected = 0;
        long totalSize = 0;

        for (TreeItem<LibraryItem> itemResult : itemResults) {
            if (itemResult.getValue().getApplication() instanceof Folder) {
                itemResult.setExpanded(true);
                numNodes += 1 + loadApplicationsForFolder(itemResult, library, useCheckboxes, onlySelected);

                if (!useCheckboxes || (((CheckBoxTreeItem) itemResult).isSelected() ||
                        ((CheckBoxTreeItem) itemResult).isIndeterminate())) {
                    totalSize += itemResult.getValue().getTreeFilesize();
                }
            } else {
                numNodes++;

                if (!useCheckboxes || (((CheckBoxTreeItem) itemResult).isSelected() ||
                        ((CheckBoxTreeItem) itemResult).isIndeterminate())) {
                    totalSize += itemResult.getValue().getApplication().getApplicationSize();
                }
            }

            // set indeterminate values for checkboxes properly
            if (useCheckboxes &&
                    ((CheckBoxTreeItem) itemResult).isSelected()) {
                numSelected++;
            }
        }

        parentFolder.getValue().setTreeFilesize(totalSize);

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

        this.closeCurrentSession();
        return numNodes;
    }

    /**
     * Loads a library from the database, given a console SID and a library ID.
     * @param library The metadata for the library that needs to be loaded.
     * @param useCheckboxes Whether or not to make the tree items CheckBoxTreeItems
     * @param onlySelected Whether or not to load only the selected library items
     * @return A tree representing the requested library.
     */
    public TreeItem<LibraryItem> loadApplicationTreeForLibrary(Library library, boolean useCheckboxes, boolean onlySelected) {
        this.openCurrentSessionWithTransaction();

        // check if the HOME folder exists, create one if it doesn't
        Application homeFolder = this.applicationDAO.loadApplicationByAppId(SharedConstants.HOME_FOLDER_ID);

        if (homeFolder == null) {
            homeFolder = new Folder();
            homeFolder.setApplicationName(SharedConstants.HOME_FOLDER_NAME);
            homeFolder.setApplicationId(SharedConstants.HOME_FOLDER_ID);
            this.applicationDAO.create(homeFolder);
        }

        // create a LibraryItem for the home folder if one doesn't exist
        LibraryItem homeFolderItem = loadLibraryItemByApplication(homeFolder, library);

        if (homeFolderItem == null) {
            homeFolderItem = new LibraryItem()
                    .setFolder(null)
                    .setApplication(homeFolder)
                    .setLibrary(library)
                    .setSelected(true);
            this.libraryItemDAO.create(homeFolderItem);
        }

        // now select all items in the home folder and recurse down for any folders
        final TreeItem<LibraryItem> homeItem;

        if (useCheckboxes) {
            homeItem = new CheckBoxTreeItem<>(homeFolderItem, null,
                    homeFolderItem.isSelected(), false);
            CheckBoxTreeItemUtils.setCheckListenerOnTreeItem((CheckBoxTreeItem) homeItem, this.libraryItemDAO, this.uiPropertyContainer);
        } else {
            homeItem = new TreeItem<>(homeFolderItem, null);
        }

        homeItem.setExpanded(true);
        homeItem.getValue().setNumNodes(loadApplicationsForFolder(homeItem, library, useCheckboxes, onlySelected) + 1);
        this.uiPropertyContainer.numSelected.set(getNumSelectedForLibrary(library));

        this.closeCurrentSessionwithTransaction();
        return homeItem;
    }

    /**
     * Gets the number of selected items for a given library (excluding folders).
     * @param library The library to query for.
     * @return The number of selected items in the library (excluding folders).
     */
    public long getNumSelectedForLibrary(Library library) {
        this.openCurrentSession();

        final Query query = this.currentSession.createQuery(
                "select count(*) from LibraryItem l where l.library = :library and l.selected = true and l.application.class = Application");
        query.setParameter("library", library);
        final long result = (Long) query.uniqueResult();

        this.closeCurrentSession();
        return result;
    }

    /**
     * Returns a list of all LibraryItems belonging to the given library.
     * @param library The library to query
     * @param onlySelected Whether or not to include only selected items
     * @return A list of all LibraryItems belonging to the given Library
     */
    public List<LibraryItem> getApplicationsForLibrary(Library library, boolean onlySelected) {
        this.openCurrentSession();

        String queryString = "from LibraryItem l where l.library = :library";

        if (onlySelected) {
            queryString += " and l.selected = true";
        }

        final Query<LibraryItem> query = this.currentSession.createQuery(queryString);
        query.setParameter("library", library);
        final List<LibraryItem> results = query.getResultList();

        this.closeCurrentSession();
        return results;
    }
}