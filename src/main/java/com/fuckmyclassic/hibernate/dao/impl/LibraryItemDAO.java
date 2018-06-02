package com.fuckmyclassic.hibernate.dao.impl;

import com.fuckmyclassic.hibernate.dao.AbstractHibernateDAO;
import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.LibraryItem;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Default DAO for library items, so we get CRUD methods.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Repository
public class LibraryItemDAO extends AbstractHibernateDAO<LibraryItem> {

    @Autowired
    public LibraryItemDAO(final SessionFactory sessionFactory) {
        super(sessionFactory);
        setClazz(LibraryItem.class);
    }

    /**
     * Return all
     * @param application
     * @return
     */
    public List<LibraryItem> getLibraryItemsForApplication(final Application application) {
        this.openCurrentSession();
        final Query<LibraryItem> query = this.currentSession.createQuery("from LibraryItem l where l.application = :application");
        query.setParameter("application", application);
        final List<LibraryItem> results = query.getResultList();
        this.closeCurrentSession();

        return results;
    }
}
