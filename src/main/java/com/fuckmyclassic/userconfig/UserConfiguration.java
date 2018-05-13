package com.fuckmyclassic.userconfig;

import com.fuckmyclassic.hibernate.dao.ConsoleDAO;
import com.fuckmyclassic.model.Console;
import com.fuckmyclassic.shared.SharedConstants;
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to contain the user configurations.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class UserConfiguration {

    /** The name of the configuration file on-disk. */
    private static final String CONFIG_FILE = "config.toml";
    /** Config key for the selected console SID */
    private static final String SELECTED_CONSOLE_KEY = "selectedConsole";
    /** Condif key for the selected library ID */
    private static final String SELECTED_LIBRARY_KEY = "selectedLibraryID";

    /** Console DAO for loading up the saved console */
    private final ConsoleDAO consoleDAO;
    /** The currently selected Console to manage */
    private Console selectedConsole;
    /** The set of currently connected Consoles */
    private Set<Console> connectedConsoles;
    /** The last console SID that was connected, so we can know if the connection is from a new console */
    private String lastConsoleSID;
    /** The ID of the last library that was selected for the console. */
    private long selectedLibraryID;

    @Autowired
    public UserConfiguration(final ConsoleDAO consoleDAO) {
        this.consoleDAO = consoleDAO;
        this.selectedConsole = null;
        this.connectedConsoles = Collections.newSetFromMap(new ConcurrentHashMap<Console, Boolean>());
        this.lastConsoleSID = SharedConstants.DEFAULT_CONSOLE_SID;
        this.selectedLibraryID = -1L;
    }

    /**
     * Creates a UserConfiguration file from the standard config file path.
     * @return A new UserConfiguration file from the file path, or a default one if the file doesn't exist
     */
    public void initFromTomlFile() {
        final File tomlFile = new File(CONFIG_FILE);

        if (tomlFile.exists()) {
            final Toml toml = new Toml().read(tomlFile);
            this.selectedConsole = consoleDAO.getOrCreateConsoleForSid(toml.getString(SELECTED_CONSOLE_KEY));
            this.selectedLibraryID = toml.getLong(SELECTED_LIBRARY_KEY);
            this.lastConsoleSID = this.selectedConsole.getConsoleSid();
        }
    }

    /**
     * Saves the current configuration values to the disk.
     */
    public void saveTomlFile() throws IOException {
        final TomlWriter tomlWriter = new TomlWriter();
        final File tomlFile = new File(CONFIG_FILE);

        if (!tomlFile.exists()) {
            tomlFile.createNewFile();
        }

        final Map<String, Object> fileAttribs = new HashMap<>();
        fileAttribs.put(SELECTED_CONSOLE_KEY, this.selectedConsole == null ?
                SharedConstants.DEFAULT_CONSOLE_SID : this.selectedConsole.getConsoleSid());
        fileAttribs.put(SELECTED_LIBRARY_KEY, this.selectedLibraryID);
        tomlWriter.write(fileAttribs, tomlFile);
    }

    /**
     * Add a new Console to the connected consoles set.
     * @param console The newly connected console.
     */
    public void addConnectedConsole(final Console console) {
        this.connectedConsoles.add(console);
    }

    /**
     * Removes a Console from the connected consoles set.
     * @param console The newly disconnected console.
     */
    public void removeConnectedConsole(final Console console) {
        this.connectedConsoles.removeIf(x -> x.getConsoleSid().equals(console.getConsoleSid()));
    }

    /**
     * Says whether or not any of the logically connected consoles has the given IP address.
     * @param address The address to check for
     * @return Whether or not the given address is connected
     */
    public boolean isAddressConnected(final String address) {
        for (Console console : connectedConsoles) {
            if (console.getLastKnownAddress().equals(address)) {
                return true;
            }
        }

        return false;
    }

    public Console getSelectedConsole() {
        return selectedConsole;
    }

    public UserConfiguration setSelectedConsole(Console selectedConsole) {
        this.selectedConsole = selectedConsole;
        return this;
    }

    public long getSelectedLibraryID() {
        return selectedLibraryID;
    }

    public UserConfiguration setSelectedLibraryID(long selectedLibraryID) {
        this.selectedLibraryID = selectedLibraryID;
        return this;
    }

    public Set<Console> getConnectedConsoles() {
        return connectedConsoles;
    }

    public UserConfiguration setConnectedConsoles(Set<Console> connectedConsoles) {
        this.connectedConsoles = connectedConsoles;
        return this;
    }

    public String getLastConsoleSID() {
        return lastConsoleSID;
    }

    public UserConfiguration setLastConsoleSID(String lastConsoleSID) {
        this.lastConsoleSID = lastConsoleSID;
        return this;
    }
}
