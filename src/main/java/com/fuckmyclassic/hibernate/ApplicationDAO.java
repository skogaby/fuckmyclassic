package com.fuckmyclassic.hibernate;

import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Library;
import javafx.scene.control.TreeItem;

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
    Application loadApplicationByAppId(final String applicationId);

    /**
     * Loads all the applications that are in a given folder (in the given library)
     * @param parentFolder The folder to load the applications from
     * @param library The metadata for the library that needs to be loaded.
     */
    void loadApplicationsForFolder(TreeItem<Application> parentFolder, Library library);
    
    /**
     * Loads a library from the database, given the library's metadata.
     * @param library The metadata for the library that needs to be loaded.
     * @return A tree representing the requested library.
     */
    TreeItem<Application> loadApplicationTreeForLibrary(Library library);
}
