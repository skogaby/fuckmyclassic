package com.fuckmyclassic.hibernate;

import com.fuckmyclassic.model.Application;

/**
 * DAO interface for interacting with applications in the database.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public interface ApplicationDAO {

    /**
     * Loads a specific application by its string ID.
     * @param applicationId The ID string of the application (ex. CLV-S-00000)
     * @return The Application corresponding to the ID
     */
    Application loadApplicationByAppId(String applicationId);
}
