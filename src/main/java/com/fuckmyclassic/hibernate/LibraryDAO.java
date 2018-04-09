package com.fuckmyclassic.hibernate;

import com.fuckmyclassic.model.Library;

import java.util.List;

/**
 * DAO interface for interacting with library metadata in the database.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public interface LibraryDAO {

    /**
     * Fetches the metadata for all of the libraries for a given console.
     * @param consoleSid The console SID to fetch the libraries for
     * @return The list of Library metadata items for the given console
     */
    List<Library> getLibrariesForConsole(String consoleSid);
}
