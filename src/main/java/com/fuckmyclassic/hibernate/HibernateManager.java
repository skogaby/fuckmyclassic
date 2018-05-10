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

    /** The actual session handle to the database. */
    private final Session hibernateSession;

    @Autowired
    public HibernateManager(final Session hibernateSession) {
        this.hibernateSession = hibernateSession;
    }

    /**
     * Performs the given mutation on the given entity in the database.
     * @param entity Entity to operate on
     * @param operation The operation to perform
     */
    public void performMutation(final Object entity, PersistenceOperation operation) {
        if (entity != null) {
            Transaction tx = null;

            try {
                tx = this.hibernateSession.beginTransaction();
                operation.call(entity);
                this.hibernateSession.flush();
                this.hibernateSession.clear();
                tx.commit();
            } catch (Exception e) {
                if (tx != null) {
                    tx.rollback();
                }

                throw e;
            }
        }
    }

    /**
     * Saves a new entity.
     * @param entity
     */
    public void saveEntity(final Object entity) {
        performMutation(entity, x -> this.hibernateSession.saveOrUpdate(x));
    }

    /**
     * Updates an existing entity.
     * @param entity
     */
    public void updateEntity(final Object entity) {
        performMutation(entity, x -> this.hibernateSession.saveOrUpdate(x));
    }

    /**
     * Deletes an existing entity.
     * @param entity
     */
    public void deleteEntity(final Object entity) {
        performMutation(entity, x -> this.hibernateSession.delete(x));
    }
}
