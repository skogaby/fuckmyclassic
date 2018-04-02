package com.fuckmyclassic.management;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Class to represent a folder on the console's home menu. This is
 * a subclass of Application so we can make <code>.desktop</code>
 * files for the folders.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class Folder extends Application {

    private StringProperty destinationFolder;

    public Folder() {

    }

    public Folder(final String destinationFolder) {
        super();
        this.destinationFolder = new SimpleStringProperty(destinationFolder);
    }

    public String getDestinationFolder() {
        return destinationFolder.get();
    }

    public Folder setDestinationFolder(String destinationFolder) {
        this.destinationFolder = new SimpleStringProperty(destinationFolder);
        return this;
    }
}
