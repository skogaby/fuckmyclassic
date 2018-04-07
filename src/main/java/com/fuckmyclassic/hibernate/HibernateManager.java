package com.fuckmyclassic.hibernate;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper class to manage Hibernate entities. Handles things
 * such as wrapping commits in transactions, things like that.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class HibernateManager {

    /**
     * The actual session handle to the database.
     */
    private final Session hibernateSession;

    @Autowired
    public HibernateManager(final Session hibernateSession) {
        this.hibernateSession = hibernateSession;
    }

    /**
     * Persists an entity to the database, wrapping it in
     * a transaction.
     * @param entity The entity to persist.
     */
    public void persistEntity(final Object entity) {
        if (entity != null) {
            Transaction tx = null;

            try {
                tx = this.hibernateSession.beginTransaction();
                this.hibernateSession.persist(entity);
                tx.commit();
            } catch (Exception e) {
                if (tx != null) {
                    tx.rollback();
                }

                throw e;
            }
        }
    }
}
