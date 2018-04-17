package com.fuckmyclassic.spring.configuration;

import com.fuckmyclassic.hibernate.ApplicationDAO;
import com.fuckmyclassic.hibernate.ApplicationDAOImpl;
import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.LibraryDAO;
import com.fuckmyclassic.hibernate.LibraryDAOImpl;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;

/**
 * Spring configuration for the Hibernate beans.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Configuration
@PropertySource("classpath:hibernate.properties")
@EnableTransactionManagement
public class HibernateConfiguration {

    @Value("${jdbc.driverClassName}")
    private String driverClassName;

    @Value("${jdbc.url}")
    private String url;

    @Value("${jdbc.username}")
    private String username;

    @Value("${jdbc.password}")
    private String password;

    @Value("${hibernate.hbm2ddl.auto}")
    private String hbm2ddl;

    @Value("${hibernate.dialect}")
    private String dialect;

    @Value("${hibernate.show_sql}")
    private String showSql;

    @Bean
    public LocalSessionFactoryBean sessionFactory(DriverManagerDataSource dataSource) {
        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", this.hbm2ddl);
        hibernateProperties.setProperty("hibernate.dialect", this.dialect);
        hibernateProperties.setProperty("hibernate.show_sql", this.showSql);

        final LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan("com.fuckmyclassic.model");
        sessionFactory.setHibernateProperties(hibernateProperties);

        return sessionFactory;
    }

    @Bean
    public Session getSession(LocalSessionFactoryBean sessionFactory) {
        return sessionFactory.getObject().openSession();
    }

    @Bean
    public DriverManagerDataSource dataSource() {
        final DriverManagerDataSource source = new DriverManagerDataSource();
        source.setDriverClassName(this.driverClassName);
        source.setUrl(this.url);
        source.setUsername(this.username);
        source.setPassword(this.password);

        return source;
    }

    @Bean
    public HibernateManager hibernateManager(Session session) {
        return new HibernateManager(session);
    }

    @Bean
    public ApplicationDAO applicationDAO(HibernateManager hibernateManager, Session session) {
        return new ApplicationDAOImpl(hibernateManager, session);
    }

    @Bean
    public LibraryDAO libraryDAO(HibernateManager hibernateManager, Session session, ApplicationDAO applicationDAO) {
        return new LibraryDAOImpl(hibernateManager, session, applicationDAO);
    }
}
