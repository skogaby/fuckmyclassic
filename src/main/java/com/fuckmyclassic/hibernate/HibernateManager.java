package com.fuckmyclassic.hibernate;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Helper class to manage Hibernate entities. Handles things
 * such as wrapping commits in transactions, things like that.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class HibernateManager {

    private final int MINIMUM_RETRY_SLEEP = 300;
    private final int MAXIMUM_RETRY_SLEEP = 500;

    /** Random number generator for simple retry jitter on concurrent modifications */
    private final Random random;
    /** The actual session handle to the database. */
    private final Session hibernateSession;

    @Autowired
    public HibernateManager(final Session hibernateSession) {
        this.random = new Random(System.currentTimeMillis());
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

                if (e instanceof ExecutionException) {
                    // retry if this was due to multiple mutations happening at once
                    try {
                        Thread.sleep(this.random.nextInt(MAXIMUM_RETRY_SLEEP - MINIMUM_RETRY_SLEEP) +
                                MINIMUM_RETRY_SLEEP);
                        performMutation(entity, operation);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
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
