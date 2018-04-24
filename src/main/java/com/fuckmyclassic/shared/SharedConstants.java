package com.fuckmyclassic.shared;

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
    /** Resource path for the warning icon. */
    public static String WARNING_IMAGE = "images/warning.png";
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
}
