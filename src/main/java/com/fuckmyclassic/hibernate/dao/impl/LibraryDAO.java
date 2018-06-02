package com.fuckmyclassic.hibernate.dao.impl;

import com.fuckmyclassic.hibernate.dao.AbstractHibernateDAO;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.shared.SharedConstants;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DAO for accessing library data.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Repository
public class LibraryDAO extends AbstractHibernateDAO<Library> {

    @Autowired
    public LibraryDAO(final SessionFactory sessionFactory) {
        super(sessionFactory);
        setClazz(Library.class);
    }

    /**
     * Fetches the metadata for all of the libraries for a given console,
     * or creates a default library if none exists for this console.
     * @param consoleSid The console SID to fetch the libraries for
     * @return The list of Library metadata items for the given console
     */
    public List<Library> getOrCreateLibrariesForConsole(final String consoleSid) {
        this.openCurrentSession();
        List<Library> results = getLibrariesForConsole(consoleSid);
        this.closeCurrentSession();

        // create a default library if none exists
        if (results.isEmpty()) {
            final Library defaultLibrary = new Library(consoleSid, SharedConstants.DEFAULT_LIBRARY_NAME);
            super.create(defaultLibrary);
            results.add(defaultLibrary);
        }

        return results;
    }

    /**
     * Fetches the metadata for all of the libraries for a given console.
     * @param consoleSid The console SID to fetch the libraries for
     * @return The list of Library metadata items for the given console
     */
    public List<Library> getLibrariesForConsole(final String consoleSid) {
        this.openCurrentSession();

        final Query<Library> query = this.currentSession.createQuery("from Library where console_sid = :console_sid");
        query.setParameter("console_sid", consoleSid);
        final List<Library> libraries = query.getResultList();

        this.closeCurrentSession();
        return libraries;
    }

    /**
     * Gets the number of selected items for a given library (excluding folders).
     * @param library The library to query for.
     * @return The number of selected items in the library (excluding folders).
     */
    public long getNumSelectedForLibrary(Library library) {
        this.openCurrentSession();

        final Query query = this.currentSession.createQuery(
                "select count(*) from LibraryItem l where l.library = :library and l.selected = true and l.application.class = Application");
        query.setParameter("library", library);
        final long result = (Long) query.uniqueResult();

        this.closeCurrentSession();
        return result;
    }
}
