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
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    public CreateTempDataTask(final LibraryManager libraryManager,
                              final ResourceBundle resourceBundle) {
        this.libraryManager = libraryManager;
        this.resourceBundle = resourceBundle;
        this.syncPath = null;
    }

    @Override
    public Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws IOException {
                LOG.info("Processing temporary data for the current library");

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
                Path originalGamePath;
                String desktopFileContents;
                BufferedWriter desktopFileWriter;
                int visitedNodes = 0;

                while (!nodesToVisit.isEmpty()) {
                    visitedNodes++;
                    currentNode = nodesToVisit.remove(0);
                    nodesToVisit.addAll(currentNode.getChildren());
                    currentApp = currentNode.getValue().getApplication();

                    // skip to the next one if this item isn't selected
                    if (!currentNode.getValue().isSelected()) {
                        continue;
                    }

                    updateMessage(String.format(resourceBundle.getString(IN_PROGRESS_MESSAGE_KEY),
                            currentApp.getApplicationName()));
                    updateProgress(visitedNodes, maxItems);
                    shouldCreateApp = (currentNode.getParent() != null);

                    // if it's a folder, create the actual folder for the games under it, then create
                    // the switcher application in the parent folder (if there is a parent)
                    if (shouldCreateApp) {
                        LOG.debug(String.format("Processing temp data for %s", currentApp.getApplicationName()));

                        originalGamePath = Paths.get(SharedConstants.GAMES_DIRECTORY, currentApp.getApplicationId());

                        // create the folder in .storage
                        newAppDirectoryStorage = new File(Paths.get(storageDir,
                                currentApp.getApplicationId()).toUri());
                        newAppDirectoryStorage.mkdirs();
                        // create the folder for this app in the parent folder's directory
                        newAppDirectoryTemp = new File(Paths.get(SharedConstants.TEMP_DIRECTORY,
                                currentNode.getParent().getValue().getApplication().getApplicationId(),
                                currentApp.getApplicationId()).toUri());
                        newAppDirectoryTemp.mkdirs();

                        // create the desktop file for this app in the "real" folder
                        desktopFileContents = currentApp.getDesktopFile(syncPath);
                        desktopFileWriter = new BufferedWriter(new FileWriter(Paths.get(newAppDirectoryTemp.toString(),
                                String.format("%s.desktop", currentApp.getApplicationId())).toString()));
                        desktopFileWriter.write(desktopFileContents);
                        desktopFileWriter.close();

                        LOG.debug(String.format("Created desktop file for %s", currentApp.getApplicationName()));

                        // symlink the actual contents of the app itself in the "storage" folder, minus the autoplay
                        // and pixelart folders
                        if (!(currentApp instanceof Folder)) {
                            symlinkContentsOfDirectory(originalGamePath, newAppDirectoryStorage.toPath());
                        }

                        // symlink the boxart into the "storage" folder
                        final String boxartName = currentApp.getBoxArtPath();
                        final String thumbnailName = boxartName.replace(".png", "_small.png");
                        final String desiredBoxartName = String.format("%s.png", currentApp.getApplicationId());
                        final String desiredThumbnailName = String.format("%s_small.png", currentApp.getApplicationId());
                        final File sourceBoxart = Paths.get(SharedConstants.BOXART_DIRECTORY, boxartName)
                                .toAbsolutePath().toFile();
                        final File sourceThumbnail = Paths.get(SharedConstants.BOXART_DIRECTORY, thumbnailName)
                                .toAbsolutePath().toFile();

                        // if the boxart for the game isn't set or doesn't exist, just copy the warning image in its place
                        Files.createSymbolicLink(
                                Paths.get(newAppDirectoryStorage.toString(), desiredBoxartName).toAbsolutePath(),
                                (!StringUtils.isBlank(boxartName) && sourceBoxart.exists()) ?
                                        Paths.get(SharedConstants.BOXART_DIRECTORY, boxartName).toAbsolutePath() :
                                        Paths.get(SharedConstants.BOXART_DIRECTORY, SharedConstants.WARNING_IMAGE).toAbsolutePath());
                        Files.createSymbolicLink(
                                Paths.get(newAppDirectoryStorage.toString(), desiredThumbnailName).toAbsolutePath(),
                                (!StringUtils.isBlank(thumbnailName) && sourceThumbnail.exists()) ?
                                        Paths.get(SharedConstants.BOXART_DIRECTORY, thumbnailName).toAbsolutePath() :
                                        Paths.get(SharedConstants.BOXART_DIRECTORY, SharedConstants.WARNING_IMAGE_THUMBNAIL).toAbsolutePath());

                        LOG.debug(String.format("Symlinked the boxart for %s", currentApp.getApplicationName()));

                        // if they exist in the original game, create a pixelart and autoplay
                        // folder in the temp folder for the game and symlink back to the originals
                        final File srcPixelartFolder = new File(
                                Paths.get(originalGamePath.toString(), SharedConstants.PIXELART_DIR).toUri());
                        if (srcPixelartFolder.exists() && srcPixelartFolder.isDirectory()) {
                            final File dstPixelArtFolder = new File(
                                    Paths.get(newAppDirectoryTemp.toString(), SharedConstants.PIXELART_DIR).toUri());
                            dstPixelArtFolder.mkdirs();
                            symlinkContentsOfDirectory(srcPixelartFolder.toPath(), dstPixelArtFolder.toPath());
                            LOG.debug(String.format("Symlinked the pixelart data for %s", currentApp.getApplicationName()));
                        }

                        final File srcAutoplayFolder = new File(
                                Paths.get(originalGamePath.toString(), SharedConstants.AUTOPLAY_DIR).toUri());
                        if (srcAutoplayFolder.exists() && srcAutoplayFolder.isDirectory()) {
                            final File dstAutoplayFolder = new File(
                                    Paths.get(newAppDirectoryTemp.toString(), SharedConstants.AUTOPLAY_DIR).toUri());
                            dstAutoplayFolder.mkdirs();
                            symlinkContentsOfDirectory(srcAutoplayFolder.toPath(), dstAutoplayFolder.toPath());
                            LOG.debug(String.format("Symlinked the autoplay data for %s", currentApp.getApplicationName()));
                        }
                    }
                }

                updateMessage(resourceBundle.getString(COMPLETE_MESSAGE_KEY));
                updateProgress(maxItems, maxItems);

                return null;
            }
        };
    }

    /**
     * Creates symlinks of all the contents in the src folder inside the dst folder (minus
     * the autoplay and pixelart folders)
     * @param src
     * @param dst
     * @throws IOException
     */
    private void symlinkContentsOfDirectory(final Path src, final Path dst) throws IOException {
        final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(src);

        for (final Path path : directoryStream) {
            final File dstFile = new File(Paths.get(dst.toString(), path.getFileName().toString()).toUri());

            if (path.toFile().isDirectory()) {
                if (!path.toFile().getName().equals(SharedConstants.AUTOPLAY_DIR) &&
                        !path.toFile().getName().equals(SharedConstants.PIXELART_DIR)) {
                    // if it's a folder, create the folder and symlink the contents
                    dstFile.mkdirs();
                    symlinkContentsOfDirectory(path, dstFile.toPath());
                }
            } else {
                // if it's not a folder, just symlink it (relative link)
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
}
