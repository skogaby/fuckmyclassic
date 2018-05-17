package com.fuckmyclassic.hibernate.dao.impl;

import com.fuckmyclassic.hibernate.dao.AbstractHibernateDAO;
import com.fuckmyclassic.model.Application;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DAO for accessing Application data.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Repository
public class ApplicationDAO extends AbstractHibernateDAO<Application> {

    @Autowired
    public ApplicationDAO(final SessionFactory sessionFactory) {
        super(sessionFactory);
        setClazz(Application.class);
    }

    /**
     * Loads a specific application by its string ID.
     * @param applicationId The ID string of the application (ex. CLV-S-00000)
     * @return The Application corresponding to the ID
     */
    public Application loadApplicationByAppId(final String applicationId) {
        this.openCurrentSession();
        final Query<Application> query = this.currentSession.createQuery("from Application where application_id = :id");
        query.setParameter("id", applicationId);
        final List<Application> results = query.getResultList();
        this.closeCurrentSession();

        Application app = null;

        if (!results.isEmpty()) {
            app = results.get(0);
        }

        return app;
    }
}
