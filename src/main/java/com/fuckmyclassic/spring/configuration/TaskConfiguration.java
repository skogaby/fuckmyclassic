package com.fuckmyclassic.spring.configuration;

import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.dao.LibraryDAO;
import com.fuckmyclassic.management.LibraryManager;
import com.fuckmyclassic.network.NetworkConnection;
import com.fuckmyclassic.task.TaskProvider;
import com.fuckmyclassic.task.impl.CreateTempDataTask;
import com.fuckmyclassic.task.impl.GetConsoleIdsAndPathsTask;
import com.fuckmyclassic.task.impl.LoadLibrariesTask;
import com.fuckmyclassic.task.impl.UpdateUnknownLibrariesTask;
import com.fuckmyclassic.userconfig.ConsoleConfiguration;
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
    public TaskProvider taskProvider(GetConsoleIdsAndPathsTask getConsoleIdsAndPathsTask, UpdateUnknownLibrariesTask updateUnknownLibrariesTask,
                                     LoadLibrariesTask loadLibrariesTask, CreateTempDataTask createTempDataTask) {
        return new TaskProvider(createTempDataTask, getConsoleIdsAndPathsTask, loadLibrariesTask, updateUnknownLibrariesTask);
    }

    @Bean
    public GetConsoleIdsAndPathsTask getConsoleSidService(ResourceBundle resourceBundle, NetworkConnection networkConnection,
                                                          ConsoleConfiguration consoleConfiguration) {
        return new GetConsoleIdsAndPathsTask(resourceBundle, networkConnection, consoleConfiguration);
    }

    @Bean
    public UpdateUnknownLibrariesTask updateUnknownLibrariesTask(UserConfiguration userConfiguration,
                                                                 ConsoleConfiguration consoleConfiguration,
                                                                 HibernateManager hibernateManager,
                                                                 LibraryDAO libraryDAO,
                                                                 ResourceBundle resourceBundle) {
        return new UpdateUnknownLibrariesTask(userConfiguration, consoleConfiguration, hibernateManager, libraryDAO, resourceBundle);
    }

    @Bean
    public LoadLibrariesTask loadLibrariesTask(UserConfiguration userConfiguration, ConsoleConfiguration consoleConfiguration,
                                               LibraryDAO libraryDAO, ResourceBundle resourceBundle) {
        return new LoadLibrariesTask(userConfiguration, consoleConfiguration, libraryDAO, resourceBundle);
    }

    @Bean
    public CreateTempDataTask createTempDataTask(LibraryManager libraryManager, ResourceBundle resourceBundle) {
        return new CreateTempDataTask(libraryManager, resourceBundle);
    }
}
