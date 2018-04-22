package com.fuckmyclassic.management;

import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Folder;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.shared.SharedConstants;
import javafx.scene.control.TreeItem;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * The class that handles operations related to exporting and syncing
 * games to either USB drives or a console itself.
 */
@Component
public class AppExporter {

    /** Library manager so we can get the current library data. */
    private final LibraryManager libraryManager;

    @Autowired
    public AppExporter(final LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
    }

    /**
     * Prepares the temporary data on disk before exporting it to the console.
     * @throws IOException
     */
    public void prepareTempData() throws IOException {
        // first, create the temp directory or empty it if it exists
        final File tempDirectory = new File(SharedConstants.TEMP_DIRECTORY);

        if (tempDirectory.exists() && tempDirectory.isDirectory()) {
            FileUtils.cleanDirectory(tempDirectory);
        } else {
            tempDirectory.mkdirs();
        }

        // traverse the current library tree and create the temp data appropriately
        final List<TreeItem<LibraryItem>> nodesToVisit = new ArrayList<>();
        nodesToVisit.add(this.libraryManager.getCurrentLibraryTree());

        // temp variables we need while traversing and creating the data
        TreeItem<LibraryItem> currentNode;
        Application currentApp;
        boolean shouldCreateApp;
        File newAppDirectory;
        String desktopFileContents;
        BufferedWriter desktopFileWriter;

        while (!nodesToVisit.isEmpty()) {
            shouldCreateApp = true;
            currentNode = nodesToVisit.remove(0);
            nodesToVisit.addAll(currentNode.getChildren());

            // if it's a folder, create the actual folder for the games under it, then create
            // the switcher application in the parent folder (if there is a parent)
            currentApp = currentNode.getValue().getApplication();

            if (currentApp instanceof Folder) {
                new File(Paths.get(SharedConstants.TEMP_DIRECTORY, currentApp.getApplicationId()).toUri()).mkdirs();
                shouldCreateApp = (currentNode.getParent() != null);
            }

            if (shouldCreateApp) {
                // create the folder for this app in the parent folder's directory
                newAppDirectory = new File(Paths.get(SharedConstants.TEMP_DIRECTORY,
                        currentNode.getParent().getValue().getApplication().getApplicationId(),
                        currentApp.getApplicationId()).toUri());
                newAppDirectory.mkdirs();

                // create the desktop file for this app
                desktopFileContents = currentApp.getDesktopFile();
                desktopFileWriter = new BufferedWriter(new FileWriter(Paths.get(newAppDirectory.toString(),
                        String.format("%s.desktop", currentApp.getApplicationId())).toString()));
                desktopFileWriter.write(desktopFileContents);
                desktopFileWriter.close();

                // symlink the actual contents of the app itself
                if (!(currentApp instanceof Folder)) {
                    this.symlinkContentsOfDirectory(
                            Paths.get(SharedConstants.GAMES_DIRECTORY, currentApp.getApplicationId()),
                            newAppDirectory.toPath());
                }

                // symlink the boxart
                final String boxart = currentApp.getBoxArtPath();
                final String thumbnail = boxart.replace(".png", "_small.png");

                Files.createSymbolicLink(
                        Paths.get(newAppDirectory.toString(), boxart).toAbsolutePath(),
                        Paths.get(SharedConstants.BOXART_DIRECTORY, boxart).toAbsolutePath());
                Files.createSymbolicLink(
                        Paths.get(newAppDirectory.toString(), thumbnail).toAbsolutePath(),
                        Paths.get(SharedConstants.BOXART_DIRECTORY, thumbnail).toAbsolutePath());
            }
        }
    }

    /**
     * Creates symlinks of all the contents in the src folder inside the dst folder.
     * @param src
     * @param dst
     * @throws IOException
     */
    private void symlinkContentsOfDirectory(final Path src, final Path dst) throws IOException {
        final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(src);

        for (final Path path : directoryStream) {
            final File dstFile = new File(Paths.get(dst.toString(), path.getFileName().toString()).toUri());

            if (path.toFile().isDirectory()) {
                // if it's a folder, create the folder and symlink the contents
                dstFile.mkdirs();
                symlinkContentsOfDirectory(path, dstFile.toPath());
            } else {
                // if it's not a folder, just symlink it
                Files.createSymbolicLink(dstFile.toPath().toAbsolutePath(), path.toAbsolutePath());
            }
        }
    }
}
