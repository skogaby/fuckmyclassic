package com.fuckmyclassic.hibernate.dao.impl;

import com.fuckmyclassic.hibernate.dao.AbstractHibernateDAO;
import com.fuckmyclassic.model.LibraryItem;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

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
}
