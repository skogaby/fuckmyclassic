package com.fuckmyclassic.shared;

import java.nio.file.Paths;

/**
 * Shared class to contain constant values.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class SharedConstants {

    /** Name of the app. */
    public static String APP_NAME = "fuckmyclassic";
    /** Current version of the app. */
    public static String APP_VERSION = "0.1.0.0";
    /** Application name for the home folder. */
    public static String HOME_FOLDER_NAME = "Home Folder";
    /** Application ID for the home folder. */
    public static String HOME_FOLDER_ID = "000";
    /** The default library name when none exists for a console. */
    public static String DEFAULT_LIBRARY_NAME = "Default library";
    /** Default console SID when none have been connected, yet. */
    public static String DEFAULT_CONSOLE_SID = "UNKNOWN";
    /** Directory where to store actual game data */
    public static String GAMES_DIRECTORY = "games";
    /** Directory where boxart is stored */
    public static String BOXART_DIRECTORY = "boxart";
    /** Temp directory where we create the game structure and symlink the data to. */
    public static String TEMP_DIRECTORY = "temp";
    // Uncomment the below line to use the system temp directory instead of a local one. I'm using a local
    // one right now for debugging, but it should use the system path for release
    // public static String TEMP_DIRECTORY = Paths.get(System.getProperty("java.io.tmpdir"), APP_NAME).toString();
    /** Directory where resource images are stored. */
    public static String IMAGES_DIRECTORY = "images";
    /** Resource path for the warning icon. */
    public static String WARNING_IMAGE = "warning.png";
    /** Resource path for the thumbnail version of the warning icon */
    public static String WARNING_IMAGE_THUMBNAIL = "warning_small.png";
    /** The size of each dimension for the game boxarts */
    public static int BOXART_SIZE = 228;
    /** The size of each dimension for the game thumbnails */
    public static int THUMBNAIL_SIZE = 40;
    /** The path to use in the desktop files for the games directory */
    public static String CONSOLE_GAMES_DIR = "/var/games";
    /** The path to use in the desktop files for the saves directory */
    public static String CONSOLE_SAVES_DIR = "/var/saves";
    /** The name of the folder within CONSOLE_GAMES_DIR where the real game data is stored */
    public static String CONSOLE_STORAGE_DIR = "storage";
    /** The name of the autoplay folder for a game */
    public static String AUTOPLAY_DIR = "autoplay";
    /** The name of the pixelart folder for a game */
    public static String PIXELART_DIR = "pixelart";
    /** The name of the splash screen file */
    public static String SPLASH_SCREEN = "images/splash.gz";
}
