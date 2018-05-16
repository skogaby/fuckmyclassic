package com.fuckmyclassic.hibernate.dao;

import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.model.LibraryItem;
import javafx.scene.control.TreeItem;

import java.util.List;

/**
 * DAO interface for interacting with library metadata in the database.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public interface LibraryDAO {

    /**
     * Fetches the metadata for all of the libraries for a given console.
     * @param consoleSid The console SID to fetch the libraries for
     * @return The list of Library metadata items for the given console
     */
    List<Library> getOrCreateLibrariesForConsole(String consoleSid);

    /**
     * Fetches the metadata for all of the libraries for a given console.
     * @param consoleSid The console SID to fetch the libraries for
     * @return The list of Library metadata items for the given console
     */
    List<Library> getLibrariesForConsole(String consoleSid);

    /**
     * Loads the LibraryItem corresponding to the given application and library.
     * @param application The Application that corresponds to the item
     * @param library The Library that corresponds to the item
     * @return The LibraryItem corresponding to the given data
     */
    LibraryItem loadLibraryItemByApplication(Application application, Library library);

    /**
     * Loads all the applications that are in a given folder (in the given library)
     * @param parentFolder The folder to load the applications from
     * @param library The metadata for the library that needs to be loaded.
     * @param useCheckboxes Whether or not to make the tree items CheckBoxTreeItems
     */
    int loadApplicationsForFolder(TreeItem<LibraryItem> parentFolder, Library library, boolean useCheckboxes);

    /**
     * Loads a library from the database, given the library's metadata.
     * @param library The metadata for the library that needs to be loaded.
     * @param useCheckboxes Whether or not to make the tree items CheckBoxTreeItems
     * @return A tree representing the requested library.
     */
    TreeItem<LibraryItem> loadApplicationTreeForLibrary(Library library, boolean useCheckboxes);

    /**
     * Gets the number of selected items for a given library.
     * @param library The library to query for.
     * @return The number of selected items in the library.
     */
    long getNumSelectedForLibrary(Library library);
}
