package com.fuckmyclassic.shared;

import java.nio.file.Paths;

/**
 * Shared class to contain constant values.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class SharedConstants {

    /** Name of the app. */
    public static final String APP_NAME = "fuckmyclassic";

    /** Current version of the app. */
    public static final String APP_VERSION = "0.1.0.0";

    /** Application name for the home folder. */
    public static final String HOME_FOLDER_NAME = "Home Folder";

    /** Application ID for the home folder. */
    public static final String HOME_FOLDER_ID = "000";

    /** The default library name when none exists for a console. */
    public static final String DEFAULT_LIBRARY_NAME = "Default library";

    /** Default console SID when none have been connected, yet. */
    public static final String DEFAULT_CONSOLE_SID = "UNKNOWN";

    /** Default nickname for the default console */
    public static final String DEFAULT_CONSOLE_NICKNAME = "Default console";

    /** Resource path for the warning icon. */
    public static final String WARNING_IMAGE = "warning.png";

    /** Resource path for the thumbnail version of the warning icon */
    public static final String WARNING_IMAGE_THUMBNAIL = "warning_small.png";

    /** The size of each dimension for the game boxarts */
    public static final int BOXART_SIZE = 228;

    /** The size of each dimension for the game thumbnails */
    public static final int THUMBNAIL_SIZE = 40;

    /** Suffixes for showing sizes in human readable format */
    public static final String[] SIZE_DICT = { "bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
}
