package com.fuckmyclassic.controller.util;

import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Folder;
import javafx.scene.control.TreeCell;
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
     * @param files The paths of the files to add
     * @param parentItem The TreeItem of the folder to add the app to
     * @param parentCell The TreeCell of the folder to add the app to
     * @return The new Application object that was created
     */
    public Application createAndImportApplicationFromFiles(final List<File> files, final TreeItem<Application> parentItem,
                                                           final TreeCell<Application> parentCell) {
        LOG.info(String.format("Importing new app to '%s'", parentItem.getValue().getApplicationName()));

        // first, generate a new ID for the app

        // then, create the directory if it doesn't exist

        // create the Application and persist it to the database

        // create the new TreeItem and insert it

        // finally, refresh the View

        return null;
    }
}
