package com.fuckmyclassic.userconfig;

import com.fuckmyclassic.shared.SharedConstants;
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Class to contain the user configurations.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class UserConfiguration {

    /** The name of the configuration file on-disk. */
    public static final String CONFIG_FILE = "config.toml";
    /** The SID of the last console that was connected to. */
    private String lastConsoleSID;
    /** The ID of the last library that was loaded for the console. */
    private long lastLibraryID;

    public UserConfiguration() {
        this.lastConsoleSID = SharedConstants.DEFAULT_CONSOLE_SID;
        this.lastLibraryID = -1L;
    }

    /**
     * Creates a UserConfiguration file from the standard config file path.
     * @return A new UserConfiguration file from the file path, or a default one if the file doesn't exist
     */
    public static UserConfiguration loadFromTomlFile() {
        final File tomlFile = new File(CONFIG_FILE);
        final UserConfiguration configuration;

        if (!tomlFile.exists()) {
            configuration = new UserConfiguration();
        } else {
            configuration = new Toml().read(tomlFile).to(UserConfiguration.class);
        }

        return configuration;
    }

    /**
     * Saves the current configuration values to the disk.
     */
    public static void saveTomlFile(final UserConfiguration userConfiguration) throws IOException {
        final TomlWriter tomlWriter = new TomlWriter();
        final File tomlFile = new File(CONFIG_FILE);

        if (!tomlFile.exists()) {
            tomlFile.createNewFile();
        }

        tomlWriter.write(userConfiguration, tomlFile);
    }

    public String getLastConsoleSID() {
        return lastConsoleSID;
    }

    public UserConfiguration setLastConsoleSID(String lastConsoleSID) {
        this.lastConsoleSID = lastConsoleSID;
        return this;
    }

    public long getLastLibraryID() {
        return lastLibraryID;
    }

    public UserConfiguration setLastLibraryID(long lastLibraryID) {
        this.lastLibraryID = lastLibraryID;
        return this;
    }
}
