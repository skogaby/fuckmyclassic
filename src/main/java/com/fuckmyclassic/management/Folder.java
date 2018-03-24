package com.fuckmyclassic.management;

/**
 * Class to represent a folder on the console's home menu. This is
 * a subclass of Application so we can make <code>.desktop</code>
 * files for the folders.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class Folder extends Application {

    /**
     * The ID of the destination folder that this folder points to.
     */
    private String destinationFolder;

    public Folder() {

    }

    public Folder(final String destinationFolder) {
        super();
        this.destinationFolder = destinationFolder;
    }

    public String getDestinationFolder() {
        return destinationFolder;
    }

    public Folder setDestinationFolder(String destinationFolder) {
        this.destinationFolder = destinationFolder;
        return this;
    }
}
