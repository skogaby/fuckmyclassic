package com.fuckmyclassic.hibernate.dao.impl;

import com.fuckmyclassic.hibernate.dao.AbstractHibernateDAO;
import com.fuckmyclassic.model.Console;
import com.fuckmyclassic.shared.SharedConstants;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DAO for accessing Console data.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Repository
public class ConsoleDAO extends AbstractHibernateDAO<Console> {

    @Autowired
    public ConsoleDAO(final SessionFactory sessionFactory) {
        super(sessionFactory);
        setClazz(Console.class);
    }

    /**
     * Get a list of all known consoles (or the default console if none exist)
     */
    public List<Console> getAllConsoles() {
        this.openCurrentSession();
        final List<Console> consoles = this.currentSession.createQuery("from Console").getResultList();
        this.closeCurrentSession();

        if (consoles.isEmpty()) {
            final Console console = new Console();
            console.setNickname(SharedConstants.DEFAULT_CONSOLE_NICKNAME);
            console.setConsoleSid(SharedConstants.DEFAULT_CONSOLE_SID);
            consoles.add(console);
            super.create(console);
        }

        return consoles;
    }

    /**
     * Fetch a console from the database based on its SID.
     * @param consoleSid The SID of the console to fetch
     * @return The Console corresponding to the given SID
     */
    public Console getConsoleForSid(String consoleSid) {
        this.openCurrentSession();
        final Query<Console> query = this.currentSession.createQuery("from Console where console_sid = :sid");
        query.setParameter("sid", consoleSid);
        final List<Console> results = query.getResultList();
        this.closeCurrentSession();

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
    public Console getOrCreateConsoleForSid(String consoleSid) {
        this.openCurrentSession();
        final Query<Console> query = this.currentSession.createQuery("from Console where console_sid = :sid");
        query.setParameter("sid", consoleSid);
        final List<Console> results = query.getResultList();
        this.closeCurrentSession();

        Console console = null;

        if (!results.isEmpty()) {
            console = results.get(0);
        }

        // if this is the first load for the console, just go ahead and create it for the caller
        if (console == null) {
            console = new Console();
            console.setNickname(SharedConstants.DEFAULT_CONSOLE_NICKNAME);
            console.setConsoleSid(consoleSid);
            super.create(console);
        }

        return console;
    }

    /**
     * Creates a new Console in the database for the given console SID
     * @param consoleSid The SID of the new console
     * @return The newly created Console
     */
    public Console createConsoleForSid(String consoleSid) {
        final Console console = new Console();
        console.setNickname(SharedConstants.DEFAULT_CONSOLE_NICKNAME);
        console.setConsoleSid(consoleSid);
        super.create(console);

        return console;
    }

    /**
     * Fetch a console from the database based on its last known IP address,
     * or null if no such console exists.
     * @param lastKnownAddress The last known IP address of the console
     * @return The requested Console, or null if it doesn't exist
     */
    public Console getConsoleForLastKnownAddress(String lastKnownAddress) {
        this.openCurrentSession();

        final Query<Console> query = this.currentSession.createQuery("from Console where last_known_address = :address");
        query.setParameter("address", lastKnownAddress);
        final List<Console> results = query.getResultList();
        Console console = null;

        this.closeCurrentSession();

        if (!results.isEmpty()) {
            console = results.get(0);
        }

        return console;
    }
}
