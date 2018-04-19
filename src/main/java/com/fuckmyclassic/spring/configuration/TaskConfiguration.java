package com.fuckmyclassic.spring.configuration;

import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.LibraryDAO;
import com.fuckmyclassic.network.NetworkConnection;
import com.fuckmyclassic.task.GetConsoleSidTask;
import com.fuckmyclassic.task.UpdateUnknownLibrariesTask;
import com.fuckmyclassic.userconfig.UserConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.ResourceBundle;

/**
 * Spring Bean configuration to setup all the Tasks.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Configuration
@ComponentScan({"com.fuckmyclassic.spring.configuration"})
public class TaskConfiguration {

    @Bean
    public GetConsoleSidTask getConsoleSidService(ResourceBundle resourceBundle, NetworkConnection networkConnection) {
        return new GetConsoleSidTask(resourceBundle, networkConnection);
    }

    @Bean
    public UpdateUnknownLibrariesTask updateUnknownLibrariesTask(UserConfiguration userConfiguration,
            NetworkConnection networkConnection, HibernateManager hibernateManager, LibraryDAO libraryDAO, ResourceBundle resourceBundle) {
        return new UpdateUnknownLibrariesTask(userConfiguration, networkConnection, hibernateManager, libraryDAO, resourceBundle);
    }
}
