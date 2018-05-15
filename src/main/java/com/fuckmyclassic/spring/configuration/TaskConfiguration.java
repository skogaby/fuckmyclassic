package com.fuckmyclassic.spring.configuration;

import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.dao.ConsoleDAO;
import com.fuckmyclassic.hibernate.dao.LibraryDAO;
import com.fuckmyclassic.management.LibraryManager;
import com.fuckmyclassic.network.NetworkManager;
import com.fuckmyclassic.task.TaskProvider;
import com.fuckmyclassic.task.impl.CreateTempDataTask;
import com.fuckmyclassic.task.impl.DumpOriginalGamesTask;
import com.fuckmyclassic.task.impl.IdentifyConnectedConsoleTask;
import com.fuckmyclassic.task.impl.LoadLibrariesTask;
import com.fuckmyclassic.task.impl.MountGamesAndStartUiTask;
import com.fuckmyclassic.task.impl.RsyncDataTask;
import com.fuckmyclassic.task.impl.ShowSplashScreenAndStopUiTask;
import com.fuckmyclassic.task.impl.UnmountGamesTask;
import com.fuckmyclassic.task.impl.UpdateUnknownLibrariesTask;
import com.fuckmyclassic.ui.controller.RsyncRunnerDialog;
import com.fuckmyclassic.userconfig.PathConfiguration;
import com.fuckmyclassic.userconfig.UserConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
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
    public TaskProvider taskProvider(IdentifyConnectedConsoleTask identifyConnectedConsoleTask, UpdateUnknownLibrariesTask updateUnknownLibrariesTask,
                                     LoadLibrariesTask loadLibrariesTask, CreateTempDataTask createTempDataTask, RsyncDataTask rsyncDataTask,
                                     ShowSplashScreenAndStopUiTask showSplashScreenAndStopUiTask, UnmountGamesTask unmountGamesTask,
                                     MountGamesAndStartUiTask mountGamesAndStartUiTask, DumpOriginalGamesTask dumpOriginalGamesTask) {
        return new TaskProvider(createTempDataTask, identifyConnectedConsoleTask, loadLibrariesTask,
                updateUnknownLibrariesTask, rsyncDataTask, showSplashScreenAndStopUiTask, unmountGamesTask,
                mountGamesAndStartUiTask, dumpOriginalGamesTask);
    }

    @Bean
    public IdentifyConnectedConsoleTask getConsoleIdsAndPathsTask(ResourceBundle resourceBundle, NetworkManager networkManager,
                                                                  UserConfiguration userConfiguration, ConsoleDAO consoleDAO,
                                                                  HibernateManager hibernateManager) {
        return new IdentifyConnectedConsoleTask(resourceBundle, networkManager, userConfiguration, consoleDAO, hibernateManager);
    }

    @Bean
    public UpdateUnknownLibrariesTask updateUnknownLibrariesTask(UserConfiguration userConfiguration,
                                                                 HibernateManager hibernateManager,
                                                                 LibraryDAO libraryDAO,
                                                                 ResourceBundle resourceBundle) {
        return new UpdateUnknownLibrariesTask(userConfiguration, hibernateManager, libraryDAO, resourceBundle);
    }

    @Bean
    public LoadLibrariesTask loadLibrariesTask(UserConfiguration userConfiguration, LibraryDAO libraryDAO, ConsoleDAO consoleDAO,
                                               ResourceBundle resourceBundle) {
        return new LoadLibrariesTask(userConfiguration, libraryDAO, consoleDAO, resourceBundle);
    }

    @Bean
    public CreateTempDataTask createTempDataTask(LibraryManager libraryManager, ResourceBundle resourceBundle, PathConfiguration pathConfiguration) {
        return new CreateTempDataTask(libraryManager, resourceBundle, pathConfiguration);
    }

    @Bean
    public RsyncDataTask rsyncDataTask(ResourceBundle resourceBundle) throws IOException {
        return new RsyncDataTask(resourceBundle);
    }

    @Bean
    public ShowSplashScreenAndStopUiTask showSplashScreenTask(NetworkManager networkManager, UserConfiguration userConfiguration,
                                                              ResourceBundle resourceBundle)
            throws URISyntaxException {
        return new ShowSplashScreenAndStopUiTask(networkManager, userConfiguration, resourceBundle);
    }

    @Bean
    public UnmountGamesTask unmountGamesTask(NetworkManager networkManager, UserConfiguration userConfiguration,
                                             ResourceBundle resourceBundle) {
        return new UnmountGamesTask(networkManager, userConfiguration, resourceBundle);
    }

    @Bean
    public MountGamesAndStartUiTask mountGamesAndStartUiTask(NetworkManager networkManager, UserConfiguration userConfiguration,
                                                             ResourceBundle resourceBundle) {
        return new MountGamesAndStartUiTask(networkManager, userConfiguration, resourceBundle);
    }

    @Bean
    public DumpOriginalGamesTask dumpOriginalGamesTask(UserConfiguration userConfiguration, PathConfiguration pathConfiguration,
                                                       NetworkManager networkManager, ResourceBundle resourceBundle,
                                                       RsyncRunnerDialog rsyncRunnerDialog) {
        return new DumpOriginalGamesTask(userConfiguration, pathConfiguration, networkManager, resourceBundle, rsyncRunnerDialog);
    }
}
