package com.fuckmyclassic.task.impl;

import com.fuckmyclassic.management.LibraryManager;
import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Folder;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.task.AbstractTaskCreator;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.util.ResourceBundle;

/**
 * Task that handles creating the temporary data in preparation for syncing games.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class CreateTempDataTask extends AbstractTaskCreator<Void> {

    static Logger LOG = LogManager.getLogger(CreateTempDataTask.class.getName());

    private final String IN_PROGRESS_MESSAGE_KEY = "CreateTempDataTask.inProgressMessage";
    private final String COMPLETE_MESSAGE_KEY = "CreateTempDataTask.completeMessage";

    /** Library manager so we can get the current library data. */
    private final LibraryManager libraryManager;
    /** Bundle for getting localized strings. */
    private final ResourceBundle resourceBundle;
    /** The path to replace the default directory with in the desktop files (for linked sync/export) */
    private String syncPath;
    /** The path to replace the default directory with in the desktop files (for linked sync/export) */
    private String systemType;

    @Autowired
    public CreateTempDataTask(final LibraryManager libraryManager,
                              final ResourceBundle resourceBundle) {
        this.libraryManager = libraryManager;
        this.resourceBundle = resourceBundle;
        this.syncPath = null;
        this.systemType = null;
    }

    @Override
    public Task<Void> createTask() {
        if (systemType == null) {
            LOG.error("Tried to process temp data before setting the system type");
            throw new RuntimeException();
        }

        return new Task<Void>() {
            @Override
            protected Void call() throws IOException {
                int maxItems = libraryManager.getCurrentLibraryTree().getValue().getNumNodes();
                updateProgress(0, maxItems);

                // first, create the temp directory or empty it if it exists
                final String storageDir = Paths.get(SharedConstants.TEMP_DIRECTORY, SharedConstants.CONSOLE_STORAGE_DIR).toString();
                cleanDirectory(SharedConstants.TEMP_DIRECTORY);
                cleanDirectory(storageDir);

                // traverse the current library tree and create the temp data appropriately
                final List<TreeItem<LibraryItem>> nodesToVisit = new ArrayList<>();
                nodesToVisit.add(libraryManager.getCurrentLibraryTree());

                // temp variables we need while traversing and creating the data
                TreeItem<LibraryItem> currentNode;
                Application currentApp;
                boolean shouldCreateApp;
                File newAppDirectoryTemp, newAppDirectoryStorage;
                String desktopFileContents;
                BufferedWriter desktopFileWriter;
                int visitedNodes = 0;

                while (!nodesToVisit.isEmpty()) {
                    visitedNodes++;
                    currentNode = nodesToVisit.remove(0);
                    nodesToVisit.addAll(currentNode.getChildren());

                    // if it's a folder, create the actual folder for the games under it, then create
                    // the switcher application in the parent folder (if there is a parent)
                    currentApp = currentNode.getValue().getApplication();

                    updateMessage(String.format(resourceBundle.getString(IN_PROGRESS_MESSAGE_KEY),
                            currentApp.getApplicationName()));
                    updateProgress(visitedNodes, maxItems);
                    shouldCreateApp = (currentNode.getParent() != null);

                    if (shouldCreateApp) {
                        // create the folder in .storage
                        newAppDirectoryStorage = new File(Paths.get(storageDir,
                                currentApp.getApplicationId()).toUri());
                        newAppDirectoryStorage.mkdirs();
                        // create the folder for this app in the parent folder's directory
                        newAppDirectoryTemp = new File(Paths.get(SharedConstants.TEMP_DIRECTORY,
                                systemType,
                                currentNode.getParent().getValue().getApplication().getApplicationId(),
                                currentApp.getApplicationId()).toUri());
                        newAppDirectoryTemp.mkdirs();

                        // create the desktop file for this app
                        desktopFileContents = currentApp.getDesktopFile(syncPath);
                        desktopFileWriter = new BufferedWriter(new FileWriter(Paths.get(newAppDirectoryTemp.toString(),
                                String.format("%s.desktop", currentApp.getApplicationId())).toString()));
                        desktopFileWriter.write(desktopFileContents);
                        desktopFileWriter.close();

                        // symlink the actual contents of the app itself
                        if (!(currentApp instanceof Folder)) {
                            symlinkContentsOfDirectory(
                                    Paths.get(SharedConstants.GAMES_DIRECTORY, currentApp.getApplicationId()),
                                    newAppDirectoryStorage.toPath());
                        }

                        // symlink the boxart
                        final String boxart = currentApp.getBoxArtPath();
                        final String thumbnail = boxart.replace(".png", "_small.png");

                        Files.createSymbolicLink(
                                Paths.get(newAppDirectoryStorage.toString(), boxart).toAbsolutePath(),
                                Paths.get(SharedConstants.BOXART_DIRECTORY, boxart).toAbsolutePath());
                        Files.createSymbolicLink(
                                Paths.get(newAppDirectoryStorage.toString(), thumbnail).toAbsolutePath(),
                                Paths.get(SharedConstants.BOXART_DIRECTORY, thumbnail).toAbsolutePath());
                    }
                }

                updateMessage(resourceBundle.getString(COMPLETE_MESSAGE_KEY));
                updateProgress(maxItems, maxItems);

                return null;
            }
        };
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

    /**
     * Either clears out the given directory, or creates it if it doesn't exist.
     * @param dir
     */
    private void cleanDirectory(final String dir) throws IOException {
        // create the directory or empty it if it exists
        final File directory = new File(dir);

        if (directory.exists() && directory.isDirectory()) {
            FileUtils.cleanDirectory(directory);
        } else {
            directory.mkdirs();
        }
    }

    public CreateTempDataTask setSyncPath(String syncPath) {
        this.syncPath = syncPath;
        return this;
    }

    public CreateTempDataTask setSystemType(String systemType) {
        this.systemType = systemType;
        return this;
    }
}
