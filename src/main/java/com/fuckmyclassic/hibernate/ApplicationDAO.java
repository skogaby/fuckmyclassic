package com.fuckmyclassic.hibernate;

import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.model.LibraryItem;
import javafx.scene.control.CheckBoxTreeItem;

/**
 * DAO interface for interacting with applications in the database.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public interface ApplicationDAO {

    /**
     * Loads a specific application by its string ID.
     * @param applicationId The ID string of the application (ex. CLV-S-00000)
     * @return The Application corresponding to the ID
     */
    Application loadApplicationByAppId(String applicationId);

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
     */
    void loadApplicationsForFolder(CheckBoxTreeItem<LibraryItem> parentFolder, Library library);
    
    /**
     * Loads a library from the database, given the library's metadata.
     * @param library The metadata for the library that needs to be loaded.
     * @return A tree representing the requested library.
     */
    CheckBoxTreeItem<LibraryItem> loadApplicationTreeForLibrary(Library library);
}
