package com.fuckmyclassic.management;

import com.fuckmyclassic.hibernate.dao.impl.ApplicationDAO;
import com.fuckmyclassic.hibernate.dao.impl.LibraryItemDAO;
import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Folder;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.ui.component.UiPropertyContainer;
import com.fuckmyclassic.ui.util.CheckBoxTreeItemUtils;
import com.fuckmyclassic.userconfig.PathConfiguration;
import com.fuckmyclassic.userconfig.UserConfiguration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Imports an application given a file path and a folder to insert it to.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class AppImporter {

    static Logger LOG = LogManager.getLogger(AppImporter.class.getName());

    /** DAO for creating new Applications */
    private final ApplicationDAO applicationDAO;
    /** DAO for creating new LibraryItems */
    private final LibraryItemDAO libraryItemDAO;
    /** Instance to help with creating new library items. */
    private final LibraryManager libraryManager;
    /** Container for UI properties we need to update */
    private final UiPropertyContainer uiPropertyContainer;
    /** Paths for runtime operations */
    private final PathConfiguration pathConfiguration;
    /** Configuration for the session */
    private final UserConfiguration userConfiguration;

    public AppImporter(final ApplicationDAO applicationDAO,
                       final LibraryItemDAO libraryItemDAO,
                       final LibraryManager libraryManager,
                       final UiPropertyContainer uiPropertyContainer,
                       final PathConfiguration pathConfiguration,
                       final UserConfiguration userConfiguration) {
        this.applicationDAO = applicationDAO;
        this.libraryItemDAO = libraryItemDAO;
        this.libraryManager = libraryManager;
        this.uiPropertyContainer = uiPropertyContainer;
        this.pathConfiguration = pathConfiguration;
        this.userConfiguration = userConfiguration;
    }

    /**
     * Creates an application and copies the data locally, given a filepath, a folder, and a TreeCell
     * @param files The files that are being imported or drag 'n' dropped
     * @param importTarget The CheckBoxTreeItem we're attempting to import the items to (the item that was a drag 'n'
     *                     drop target, or the folder that we're importing to via the import games button)
     */
    public void handleFileImportAttempt(final List<File> files, final CheckBoxTreeItem<LibraryItem> importTarget) throws IOException {
        if (importTarget == null) {
            return;
        }

        CheckBoxTreeItem<LibraryItem> targetFolder = importTarget;

        if (!(targetFolder.getValue().getApplication() instanceof Folder)) {
            // make the target the parent folder if the current target is a game
            targetFolder = (CheckBoxTreeItem) targetFolder.getParent();
        }

        LOG.info(String.format("Attempting to import new files to '%s'. Number of files: %d",
                targetFolder.getValue().getApplication().getApplicationName(), files.size()));

        // testing
        this.importFilesAsNewApp(files, targetFolder);

        /*
            Drag and drop behavior. For all imports, below is the logic for which folder to insert into:
            * If dragged onto a folder:
                * Create a new game in that folder with the file(s) as the data
            * If dragged onto a game:
                * Create a new game in that game’s parent folder with the file(s) as the data

            If a single file is dragged into the app, use the below logic:
            * If it’s a SNES game, convert to SFROM then import in the appropriate folder
            * If it’s a non-SNES game, import to the appropriate folder
            * If it’s an HMOD file, import to the mods directory
            * If it’s a folder:
                * If the folder ends in .hmod, treat it as a HMOD file
                * If the folder is not a mod, treat it as a zip file
            * If it’s a zip file, check the contents:
                * If there’s a .desktop file, extract the zip and import it as a new game/app in the appropriate folder
                * If there is no desktop file, prompt the user for action:
                    * Extract the zip file and import each individual file as a new game in the appropriate folder
                    * Import the zip file itself as a new game in the appropriate folder

            If there are multiple files dragged into the app:
            * Prompt the user for action:
                * Import the multiple files as a single, new app that needs multiple files
                * Iterate through and import each file individually using the above logic
         */
    }

    public void importFilesAsNewApp(final List<File> files, final CheckBoxTreeItem<LibraryItem> importFolder) throws IOException {
        // first, generate a new ID for the app
        final String newAppId = generateRandomAppId();
        LOG.info(String.format("Creating new app with ID '%s'", newAppId));

        // then, create the directory if it doesn't exist and copy the files
        final File targetGameDirectory = new File(Paths.get(
                this.pathConfiguration.gamesDirectory, newAppId).toUri());
        targetGameDirectory.mkdirs();
        long applicationSize = 0;

        for (File file : files) {
            Files.copy(file.toPath(), Paths.get(targetGameDirectory.toString(), file.getName()), new CopyOption[] {
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES
            });

            applicationSize += file.length();
        }

        // create the Application and LibraryItem and persist them to the database
        final Application newApp = new Application()
                .setApplicationId(newAppId)
                .setApplicationName(files.get(0).getName())
                .setSortName(files.get(0).getName().toLowerCase(Locale.getDefault()))
                .setSinglePlayer(true)
                .setApplicationSize(applicationSize)
                .setCompressed(false);
        this.applicationDAO.create(newApp);

        final LibraryItem newLibraryItem = new LibraryItem()
                .setLibrary(this.libraryManager.getCurrentLibrary())
                .setApplication(newApp)
                .setFolder((Folder) importFolder.getValue().getApplication())
                .setSelected(true);
        this.libraryItemDAO.create(newLibraryItem);

        // create the new CheckBoxTreeItem and insert it
        final CheckBoxTreeItem<LibraryItem> newItem = new CheckBoxTreeItem<>(newLibraryItem, null, true, false);
        CheckBoxTreeItemUtils.setCheckListenerOnTreeItem(newItem, this.libraryItemDAO,
                this.uiPropertyContainer, this.userConfiguration);

        importFolder.getChildren().add(newItem);
        FXCollections.sort(importFolder.getChildren(), Comparator.comparing(TreeItem::getValue));

        // update the parent file tree sizes
        TreeItem<LibraryItem> parent = newItem;

        while (parent.getParent() != null) {
            parent = parent.getParent();
            parent.getValue().setTreeFilesize(parent.getValue().getTreeFilesize() + applicationSize);
        }

        // update the space usage
        this.uiPropertyContainer.numSelected.set(this.uiPropertyContainer.numSelected.get() + 1L);
        if (this.userConfiguration.getSelectedConsole().getSpaceForGames() != 0) {
            this.uiPropertyContainer.gameSpaceUsed.setValue(
                    (double) parent.getValue().getTreeFilesize() /
                            (double) userConfiguration.getSelectedConsole().getSpaceForGames());
        }
    }

    /**
     * Generates a random string to use as an application ID for new games.
     * @return A randomly generated application ID.
     */
    private String generateRandomAppId() {
        final Random rand = new Random();
        final char prefix = 'F';

        return String.format("CLV-%c-%c%c%c%c%c%c%c%c%c%c", prefix,
                (char) ('A' + rand.nextInt(26)),
                (char) ('A' + rand.nextInt(26)),
                (char) ('A' + rand.nextInt(26)),
                (char) ('A' + rand.nextInt(26)),
                (char) ('A' + rand.nextInt(26)),
                (char) ('A' + rand.nextInt(26)),
                (char) ('A' + rand.nextInt(26)),
                (char) ('A' + rand.nextInt(26)),
                (char) ('A' + rand.nextInt(26)),
                (char) ('A' + rand.nextInt(26)));
    }
}
