package com.fuckmyclassic.management;

import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Folder;
import javafx.scene.control.TreeItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * Imports an application given a file path and a folder to insert it to.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class AppImporter {

    static Logger LOG = LogManager.getLogger(AppImporter.class.getName());

    @Autowired
    public AppImporter() {

    }

    /**
     * Creates an application and copies the data locally, given a filepath, a folder, and a TreeCell
     * @param files The files that are being imported or drag 'n' dropped
     * @param importTarget The TreeItem we're attempting to import the items to (the item that was a drag 'n'
     *                     drop target, or the folder that we're importing to via the import games button)
     */
    public void handleFileImportAttempt(final List<File> files, final TreeItem<Application> importTarget) {
        TreeItem<Application> targetApp = importTarget;

        if (!(targetApp.getValue() instanceof Folder)) {
            // make the target the parent folder if the current target is a game
            targetApp = targetApp.getParent();
        }

        LOG.info(String.format("Importing new app to '%s'. Number of files: %d",
                targetApp.getValue().getApplicationName(), files.size()));

        // first, generate a new ID for the app

        // then, create the directory if it doesn't exist

        // create the Application and persist it to the database

        // create the new TreeItem and insert it

        // finally, refresh the View
    }
}
