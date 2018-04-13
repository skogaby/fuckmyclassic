package com.fuckmyclassic.management;

import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Folder;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.shared.SharedConstants;
import javafx.scene.control.TreeItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Imports an application given a file path and a folder to insert it to.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class AppImporter {

    static Logger LOG = LogManager.getLogger(AppImporter.class.getName());

    /**
     * Instance to help with persisting new apps and library items to the database.
     */
    private final HibernateManager hibernateManager;

    /**
     * Instance to help with creating new library items.
     */
    private final LibraryManager libraryManager;

    public AppImporter(final HibernateManager hibernateManager, final LibraryManager libraryManager) {
        this.hibernateManager = hibernateManager;
        this.libraryManager = libraryManager;

        // ensure that the games and boxart directories exists
        final File gamesDir = new File(SharedConstants.GAMES_DIRECTORY);
        final File boxartDir = new File(SharedConstants.BOXART_DIRECTORY);
        gamesDir.mkdirs();
        boxartDir.mkdirs();
    }

    /**
     * Creates an application and copies the data locally, given a filepath, a folder, and a TreeCell
     * @param files The files that are being imported or drag 'n' dropped
     * @param importTarget The TreeItem we're attempting to import the items to (the item that was a drag 'n'
     *                     drop target, or the folder that we're importing to via the import games button)
     */
    public void handleFileImportAttempt(final List<File> files, final TreeItem<Application> importTarget) throws IOException {
        if (importTarget == null) {
            return;
        }

        TreeItem<Application> targetFolder = importTarget;

        if (!(targetFolder.getValue() instanceof Folder)) {
            // make the target the parent folder if the current target is a game
            targetFolder = targetFolder.getParent();
        }

        LOG.info(String.format("Attempting to import new files to '%s'. Number of files: %d",
                targetFolder.getValue().getApplicationName(), files.size()));

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

    public void importFilesAsNewApp(final List<File> files, final TreeItem<Application> importFolder) throws IOException {
        // first, generate a new ID for the app
        final String newAppId = generateRandomAppId();
        LOG.info(String.format("Creating new app with ID '%s'", newAppId));

        // then, create the directory if it doesn't exist and copy the files
        final File targetGameDirectory = new File(Paths.get(
                SharedConstants.GAMES_DIRECTORY, newAppId).toUri());
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
        this.hibernateManager.saveEntity(newApp);

        final LibraryItem newLibraryItem = new LibraryItem()
                .setLibrary(this.libraryManager.getCurrentLibrary())
                .setApplication(newApp)
                .setFolder((Folder) importFolder.getValue())
                .setSelected(true);
        this.hibernateManager.saveEntity(newLibraryItem);

        // create the new TreeItem and insert it
        final TreeItem<Application> newItem = new TreeItem<>(newApp);
        importFolder.getChildren().add(newItem);
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
