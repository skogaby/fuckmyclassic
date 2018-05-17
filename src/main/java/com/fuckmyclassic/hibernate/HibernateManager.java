package com.fuckmyclassic.hibernate;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Helper class to manage Hibernate entities. Handles things
 * such as wrapping commits in transactions, things like that.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class HibernateManager {

    private final int MINIMUM_RETRY_SLEEP = 50;
    private final int MAXIMUM_RETRY_SLEEP = 500;
    private final int MAXIMUM_RETRIES = 4;
    private final int BATCH_SIZE = 20;

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
     * Perform a persistence operation on one or more given entities.
     * @param operation The operation to perform
     * @param tries
     * @param entities The entities to act upon
     */
    public void performMutation(final PersistenceOperation operation, int tries, final Object... entities) {
        if (entities != null) {
            Transaction tx = null;

            try {
                tx = this.hibernateSession.beginTransaction();

                for (int i = 0; i < entities.length; i++) {
                    operation.call(entities[i]);

                    if (i % BATCH_SIZE == 0) {
                        this.hibernateSession.flush();
                        this.hibernateSession.clear();
                    }
                }

                tx.commit();
            } catch (Exception e) {
                if (tx != null) {
                    tx.rollback();
                }

                if (tries < MAXIMUM_RETRIES) {
                    try {
                        Thread.sleep(this.random.nextInt(MAXIMUM_RETRY_SLEEP - MINIMUM_RETRY_SLEEP) +
                                MINIMUM_RETRY_SLEEP);
                        performMutation(operation, tries + 1, entities);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * Saves a new entity or entities.
     * @param entities
     */
    public void saveEntities(final Object... entities) {
        performMutation(x -> this.hibernateSession.saveOrUpdate(x), 0, entities);
    }

    /**
     * Updates an existing entity or entities.
     * @param entities
     */
    public void updateEntities(final Object... entities) {
        performMutation(x -> this.hibernateSession.saveOrUpdate(x), 0, entities);
    }

    /**
     * Deletes an existing entity or entities.
     * @param entities
     */
    public void deleteEntities(final Object... entities) {
        performMutation(x -> this.hibernateSession.delete(x), 0, entities);
    }
}
