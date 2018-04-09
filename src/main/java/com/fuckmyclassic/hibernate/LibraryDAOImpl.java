package com.fuckmyclassic.hibernate;

import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.shared.SharedConstants;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LibraryDAOImpl implements LibraryDAO {

    /**
     * Hibernate manager for higher level interactions.
     */
    private final HibernateManager hibernateManager;

    /**
     * Hibernate session for database interaction at a low level.
     */
    private final Session session;

    @Autowired
    public LibraryDAOImpl(final HibernateManager hibernateManager, final Session session) {
        this.hibernateManager = hibernateManager;
        this.session = session;
    }

    /**
     * Fetches the metadata for all of the libraries for a given console.
     * @param consoleSid The console SID to fetch the libraries for
     * @return The list of Library metadata items for the given console
     */
    @Override
    public List<Library> getLibrariesForConsole(String consoleSid) {
        final Query<Library > query = session.createQuery("from Library where console_sid = :console_sid");
        query.setParameter("console_sid", consoleSid);
        List<Library> results = query.getResultList();

        // create a default library if none exists
        if (results.isEmpty()) {
            final Library defaultLibrary = new Library(consoleSid, 0, SharedConstants.DEFAULT_LIBRARY_NAME);
            hibernateManager.saveEntity(defaultLibrary);
            results.add(defaultLibrary);
        }

        return results;
    }
}
