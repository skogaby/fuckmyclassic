package com.fuckmyclassic.hibernate.dao.impl;

import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.dao.ApplicationDAO;
import com.fuckmyclassic.model.Application;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implementation of the ApplicationDAO interface.
 */
@Repository
public class ApplicationDAOImpl implements ApplicationDAO {

    /** Hibernate manager for higher level interactions. */
    private final HibernateManager hibernateManager;
    /** Hibernate session for database interaction at a low level. */
    private final Session session;

    @Autowired
    public ApplicationDAOImpl(final HibernateManager hibernateManager, final Session session) {
        this.hibernateManager = hibernateManager;
        this.session = session;
    }

    /**
     * Loads a specific application by its string ID.
     * @param applicationId The ID string of the application (ex. CLV-S-00000)
     * @return The Application corresponding to the ID
     */
    @Override
    public Application loadApplicationByAppId(String applicationId) {
        final Query<Application> query = session.createQuery("from Application where application_id = :id");
        query.setParameter("id", applicationId);
        final List<Application> results = query.getResultList();
        Application app = null;

        if (!results.isEmpty()) {
            app = results.get(0);
        }

        return app;
    }
}
