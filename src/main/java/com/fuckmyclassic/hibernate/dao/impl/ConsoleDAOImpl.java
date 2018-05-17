package com.fuckmyclassic.hibernate.dao.impl;

import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.dao.ConsoleDAO;
import com.fuckmyclassic.model.Console;
import com.fuckmyclassic.shared.SharedConstants;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implementation of the ConsoleDAO interface using MySQL and Hibernate.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Repository
public class ConsoleDAOImpl implements ConsoleDAO {

    /** Hibernate manager, for making new entities */
    private final HibernateManager hibernateManager;
    /** Hibernate session for database interaction at a low level. */
    private final Session session;

    @Autowired
    public ConsoleDAOImpl(final HibernateManager hibernateManager,
                              final Session session) {
        this.hibernateManager = hibernateManager;
        this.session = session;
    }

    /**
     * Get a list of all known consoles (or the default console if none exist)
     */
    @Override
    public List<Console> getAllConsoles() {
        final List<Console> consoles = session.createQuery("from Console").getResultList();

        if (consoles.isEmpty()) {
            final Console console = new Console();
            console.setNickname(SharedConstants.DEFAULT_CONSOLE_NICKNAME);
            console.setConsoleSid(SharedConstants.DEFAULT_CONSOLE_SID);
            consoles.add(console);
            this.hibernateManager.saveEntities(console);
        }

        return consoles;
    }

    /**
     * Fetch a console from the database based on its SID.
     * @param consoleSid The SID of the console to fetch
     * @return The Console corresponding to the given SID
     */
    @Override
    public Console getConsoleForSid(String consoleSid) {
        final Query<Console> query = session.createQuery("from Console where console_sid = :sid");
        query.setParameter("sid", consoleSid);
        final List<Console> results = query.getResultList();
        Console console = null;

        if (!results.isEmpty()) {
            console = results.get(0);
        }

        return console;
    }

    /**
     * Fetch a console from the database based on its SID, or create it if it doesn't exist.
     * @param consoleSid The SID of the console to fetch
     * @return The Console corresponding to the given SID
     */
    @Override
    public Console getOrCreateConsoleForSid(String consoleSid) {
        final Query<Console> query = session.createQuery("from Console where console_sid = :sid");
        query.setParameter("sid", consoleSid);
        final List<Console> results = query.getResultList();
        Console console = null;

        if (!results.isEmpty()) {
            console = results.get(0);
        }

        // if this is the first load for the console, just go ahead and create it for the caller
        if (console == null) {
            console = new Console();
            console.setNickname(SharedConstants.DEFAULT_CONSOLE_NICKNAME);
            console.setConsoleSid(consoleSid);
            this.hibernateManager.saveEntities(console);
        }

        return console;
    }

    /**
     * Creates a new Console in the database for the given console SID
     * @param consoleSid The SID of the new console
     * @return The newly created Console
     */
    @Override
    public Console createConsoleForSid(String consoleSid) {
        final Console console = new Console();
        console.setNickname(SharedConstants.DEFAULT_CONSOLE_NICKNAME);
        console.setConsoleSid(consoleSid);
        this.hibernateManager.saveEntities(console);
        return console;
    }

    /**
     * Fetch a console from the database based on its last known IP address,
     * or null if no such console exists.
     * @param lastKnownAddress The last known IP address of the console
     * @return The requested Console, or null if it doesn't exist
     */
    @Override
    public Console getConsoleForLastKnownAddress(String lastKnownAddress) {
        final Query<Console> query = session.createQuery("from Console where last_known_address = :address");
        query.setParameter("address", lastKnownAddress);
        final List<Console> results = query.getResultList();
        Console console = null;

        if (!results.isEmpty()) {
            console = results.get(0);
        }

        return console;
    }
}
