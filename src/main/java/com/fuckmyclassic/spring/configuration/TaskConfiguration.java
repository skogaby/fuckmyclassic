package com.fuckmyclassic.spring.configuration;

import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.dao.LibraryDAO;
import com.fuckmyclassic.management.LibraryManager;
import com.fuckmyclassic.network.NetworkConnection;
import com.fuckmyclassic.task.impl.GetConsoleSidTask;
import com.fuckmyclassic.task.impl.LoadLibrariesTask;
import com.fuckmyclassic.task.impl.UpdateUnknownLibrariesTask;
import com.fuckmyclassic.ui.controller.MainWindow;
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

    @Bean
    public LoadLibrariesTask loadLibrariesTask(UserConfiguration userConfiguration, NetworkConnection networkConnection,
                                               LibraryDAO libraryDAO, ResourceBundle resourceBundle, MainWindow mainWindow,
                                               LibraryManager libraryManager) {
        return new LoadLibrariesTask(userConfiguration, networkConnection, libraryDAO, resourceBundle, mainWindow, libraryManager);
    }
}
