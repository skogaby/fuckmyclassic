package com.fuckmyclassic.spring.configuration;

import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.dao.LibraryDAO;
import com.fuckmyclassic.management.LibraryManager;
import com.fuckmyclassic.network.NetworkConnection;
import com.fuckmyclassic.task.TaskProvider;
import com.fuckmyclassic.task.impl.CreateTempDataTask;
import com.fuckmyclassic.task.impl.GetConsoleIdsAndPathsTask;
import com.fuckmyclassic.task.impl.LoadLibrariesTask;
import com.fuckmyclassic.task.impl.MountGamesAndStartUiTask;
import com.fuckmyclassic.task.impl.RsyncDataTask;
import com.fuckmyclassic.task.impl.ShowSplashScreenAndStopUiTask;
import com.fuckmyclassic.task.impl.UnmountGamesTask;
import com.fuckmyclassic.task.impl.UpdateUnknownLibrariesTask;
import com.fuckmyclassic.userconfig.ConsoleConfiguration;
import com.fuckmyclassic.userconfig.PathConfiguration;
import com.fuckmyclassic.userconfig.UserConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;
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
                                     LoadLibrariesTask loadLibrariesTask, CreateTempDataTask createTempDataTask, RsyncDataTask rsyncDataTask,
                                     ShowSplashScreenAndStopUiTask showSplashScreenAndStopUiTask, UnmountGamesTask unmountGamesTask,
                                     MountGamesAndStartUiTask mountGamesAndStartUiTask) {
        return new TaskProvider(createTempDataTask, getConsoleIdsAndPathsTask, loadLibrariesTask,
                updateUnknownLibrariesTask, rsyncDataTask, showSplashScreenAndStopUiTask, unmountGamesTask,
                mountGamesAndStartUiTask);
    }

    @Bean
    public GetConsoleIdsAndPathsTask getConsoleIdsAndPathsTask(ResourceBundle resourceBundle, NetworkConnection networkConnection,
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
    public CreateTempDataTask createTempDataTask(LibraryManager libraryManager, ResourceBundle resourceBundle, PathConfiguration pathConfiguration) {
        return new CreateTempDataTask(libraryManager, resourceBundle, pathConfiguration);
    }

    @Bean
    public RsyncDataTask rsyncDataTask(ResourceBundle resourceBundle) {
        return new RsyncDataTask(resourceBundle);
    }

    @Bean
    public ShowSplashScreenAndStopUiTask showSplashScreenTask(NetworkConnection networkConnection, ResourceBundle resourceBundle)
            throws URISyntaxException {
        return new ShowSplashScreenAndStopUiTask(networkConnection, resourceBundle);
    }

    @Bean
    public UnmountGamesTask unmountGamesTask(NetworkConnection networkConnection, ResourceBundle resourceBundle) {
        return new UnmountGamesTask(networkConnection, resourceBundle);
    }

    @Bean
    public MountGamesAndStartUiTask mountGamesAndStartUiTask(NetworkConnection networkConnection, ResourceBundle resourceBundle) {
        return new MountGamesAndStartUiTask(networkConnection, resourceBundle);
    }
}
