package com.fuckmyclassic.userconfig;

import com.fuckmyclassic.Main;
import com.fuckmyclassic.shared.SharedConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.filechooser.FileSystemView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Class to abstract away local paths on the computer. Abstracts the OS-specific pathing
 * semantics, as well as handling portable vs. non-portable mode for deciding where to
 * store userdata.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class PathConfiguration {

    //////////////////////////////////////////////////////////////////////////
    // Static members
    //////////////////////////////////////////////////////////////////////////

    /** The path to use in the desktop files for the games directory */
    public static final String CONSOLE_GAMES_DIR = "/var/games";

    /** The path to use in the desktop files for the saves directory */
    public static final String CONSOLE_SAVES_DIR = "/var/saves";

    /** The name of the folder within CONSOLE_GAMES_DIR where the real game data is stored */
    public static final String CONSOLE_STORAGE_DIR = "storage";

    /** The name of the autoplay folder for a game */
    public static final String AUTOPLAY_DIR = "autoplay";

    /** The name of the pixelart folder for a game */
    public static final String PIXELART_DIR = "pixelart";

    /** Directory where actual game data is stored */
    public static final String GAMES_DIRECTORY = "games";

    /** Directory where the untouched data for the original games is stored */
    public static final String ORIGINAL_GAMES_DIRECTORY = "original_games";

    /** Directory where resource images are stored. */
    public static final String IMAGES_DIRECTORY = "images";

    /** Name of the flag file to designate the program is in nonportable mode */
    public static final String NONPORTABLE_FLAG = "nonportable.flag";

    /** Name of the flag file to designate the temp folder, if the user wants the location specified */
    public static final String TEMP_FOLDER_FLAG = "tempfolder.flag";

    /** Squashfs games path for NES-based consoles */
    public static final String NES_SQUASHFS_GAMES_PATH = "/var/squashfs/usr/share/games/nes/kachikachi";

    /** Squashfs games path for SNES-based consoles */
    public static final String SNES_SQUASHFS_GAMES_PATH = "/var/squashfs/usr/share/games";

    //////////////////////////////////////////////////////////////////////////
    // Non-static members
    //////////////////////////////////////////////////////////////////////////

    /** Whether or not the program is running in portable mode */
    private final boolean portable;

    /** The directory of the running program itself */
    public final String programDirectory;

    /** The directory where we store userdata -- for portable mode, this is the same as internalDirectory */
    public final String externalDirectory;

    /** Directory where the local games storage is */
    public final String gamesDirectory;

    /** The directory to use for temporary data */
    public final String tempDirectory;

    /** The directory for the original games cache */
    public final String originalGamesDirectory;

    @Autowired
    public PathConfiguration() throws IOException {
        // really ugly hack to make sure we get the path of the program itself, and not the path
        // of wherever the process was invoked from. we want it to always go to the installation
        // directory
        // TODO: Test this on Windows. Test from a JAR, rather than from IDE testing. This should be more
        // robust in the future
        this.programDirectory = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                .getParentFile().getParentFile().getParentFile().getParentFile().toPath().toAbsolutePath().toString();

        // portable mode is enabled if there is a portable.flag file in the main program directory
        final File nonportableFlag = new File(Paths.get(programDirectory, NONPORTABLE_FLAG).toString());
        this.portable = nonportableFlag.exists() ? false : true;

        // the external directory is either the user's home/documents directory (nonportable mode) or
        // the installation directory (portable mode)
        this.externalDirectory = portable ? programDirectory :
                Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getPath(),
                        SharedConstants.APP_NAME).toString();

        // setup the portable/non-portable dependent paths
        this.gamesDirectory = Paths.get(this.externalDirectory, GAMES_DIRECTORY).toString();
        this.originalGamesDirectory = Paths.get(this.externalDirectory, ORIGINAL_GAMES_DIRECTORY).toString();

        final File tempFolderFlag = new File(Paths.get(programDirectory, TEMP_FOLDER_FLAG).toString());
        this.tempDirectory = tempFolderFlag.exists() ?
                new BufferedReader(new FileReader(tempFolderFlag)).readLine().trim() :
                Paths.get(System.getProperty("java.io.tmpdir"), SharedConstants.APP_NAME).toString();
    }

    public boolean isPortable() {
        return portable;
    }
}
