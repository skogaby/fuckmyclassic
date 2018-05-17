package com.fuckmyclassic.spring.configuration;

import com.fuckmyclassic.hibernate.dao.impl.ApplicationDAO;
import com.fuckmyclassic.hibernate.dao.impl.ConsoleDAO;
import com.fuckmyclassic.hibernate.dao.impl.LibraryDAO;
import com.fuckmyclassic.hibernate.dao.impl.LibraryItemDAO;
import com.fuckmyclassic.ui.component.UiPropertyContainer;
import com.fuckmyclassic.userconfig.PathConfiguration;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.nio.file.Paths;
import java.util.Properties;

/**
 * Spring configuration for the Hibernate beans.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Configuration
@ComponentScan({"com.fuckmyclassic.spring.configuration"})
@PropertySource("classpath:hibernate.properties")
@EnableTransactionManagement
public class HibernateConfiguration {

    /** The name of the sqlite database file */
    private static final String SQLITE_DATABASE = "fuckmyclassic.sqlite";

    @Value("${jdbc.driverClassName}")
    private String driverClassName;

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
    public DriverManagerDataSource dataSource(PathConfiguration pathConfiguration) {
        final DriverManagerDataSource source = new DriverManagerDataSource();
        source.setDriverClassName(this.driverClassName);
        source.setUsername(this.username);
        source.setPassword(this.password);

        // locate the database in the user directory, and replace backslashes with forward slashes so it works on
        // Windows correctly, per sqlite-jdbc's spec
        source.setUrl(String.format("jdbc:sqlite:%s",
                Paths.get(pathConfiguration.externalDirectory, SQLITE_DATABASE).toString().replace('\\', '/')));

        return source;
    }

    @Bean
    public ApplicationDAO applicationDAO(SessionFactory sessionFactory) {
        return new ApplicationDAO(sessionFactory);
    }

    @Bean
    public LibraryDAO libraryDAO(SessionFactory sessionFactory,
                                 ApplicationDAO applicationDAO,
                                 LibraryItemDAO libraryItemDAO,
                                 UiPropertyContainer uiPropertyContainer) {
        return new LibraryDAO(sessionFactory, applicationDAO, libraryItemDAO, uiPropertyContainer);
    }

    @Bean
    public ConsoleDAO consoleDAO(SessionFactory sessionFactory) {
        return new ConsoleDAO(sessionFactory);
    }

    @Bean
    public LibraryItemDAO libraryItemDAO(SessionFactory sessionFactory) {
        return new LibraryItemDAO(sessionFactory);
    }
}
