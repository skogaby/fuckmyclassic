package com.fuckmyclassic.hibernate;

import com.fuckmyclassic.model.Application;
import javafx.scene.control.TreeItem;

import java.util.List;

/**
 * DAO interface for interacting with applications in the database.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public interface ApplicationDAO {

    /**
     * Loads a specific application by its string ID.
     * @param applicationId
     * @return
     */
    Application loadApplicationById(final String applicationId);

    /**
     * Loads the data for the given applications.
     * @param applicationIds
     * @return
     */
    List<TreeItem<Application>> loadApplications(final List<String> applicationIds);

    /**
     * Loads all the applications that are in a given folder (in the given library)
     * @param parentFolder The folder to load the applications from
     * @param consoleSid The console the library belongs to
     * @param libraryId The ID of the library to load from
     * @return
     */
    public void loadApplicationsForFolder(TreeItem<Application> parentFolder, String consoleSid, int libraryId);


    /**
     * Loads a library from the database, given a console SID and a library ID.
     * @param consoleSid The console the library belongs to
     * @param libraryId The ID of the library to load
     * @return A tree representing the requested library.
     */
    TreeItem<Application> loadLibraryForConsole(final String consoleSid, final int libraryId);
}
