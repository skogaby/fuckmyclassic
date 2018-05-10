package com.fuckmyclassic.hibernate.dao;

import com.fuckmyclassic.model.Console;

/**
 * DAO interface for interacting with known consoles in the database.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public interface ConsoleDAO {

    /**
     * Fetch a console from the database based on its SID.
     * @param consoleSid The SID of the console to fetch
     * @return The Console corresponding to the given SID
     */
    Console getOrCreateConsoleForSid(String consoleSid);

    /**
     * Fetch a console from the database based on its last known IP address,
     * or null if no such console exists.
     * @param lastKnownAddress The last known IP address of the console
     * @return The requested Console, or null if it doesn't exist
     */
    Console getConsoleForLastKnownAddress(String lastKnownAddress);
}