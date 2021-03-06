package com.fuckmyclassic.spring.configuration;

import com.fuckmyclassic.hibernate.dao.impl.ConsoleDAO;
import com.fuckmyclassic.hibernate.dao.impl.LibraryDAO;
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
import com.fuckmyclassic.task.impl.TakeScreenshotTask;
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
                                     MountGamesAndStartUiTask mountGamesAndStartUiTask, DumpOriginalGamesTask dumpOriginalGamesTask,
                                     TakeScreenshotTask takeScreenshotTask) {
        return new TaskProvider(createTempDataTask, identifyConnectedConsoleTask, loadLibrariesTask,
                updateUnknownLibrariesTask, rsyncDataTask, showSplashScreenAndStopUiTask, unmountGamesTask,
                mountGamesAndStartUiTask, dumpOriginalGamesTask, takeScreenshotTask);
    }

    @Bean
    public IdentifyConnectedConsoleTask getConsoleIdsAndPathsTask(ResourceBundle resourceBundle, NetworkManager networkManager,
                                                                  UserConfiguration userConfiguration, ConsoleDAO consoleDAO) {
        return new IdentifyConnectedConsoleTask(resourceBundle, networkManager, userConfiguration, consoleDAO);
    }

    @Bean
    public UpdateUnknownLibrariesTask updateUnknownLibrariesTask(UserConfiguration userConfiguration,
                                                                 LibraryDAO libraryDAO,
                                                                 ResourceBundle resourceBundle) {
        return new UpdateUnknownLibrariesTask(userConfiguration, libraryDAO, resourceBundle);
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
    public RsyncDataTask rsyncDataTask(ResourceBundle resourceBundle, PathConfiguration pathConfiguration) throws URISyntaxException, IOException, InterruptedException {
        return new RsyncDataTask(resourceBundle, pathConfiguration);
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

    @Bean
    public TakeScreenshotTask takeScreenshotTask(NetworkManager networkManager, ResourceBundle resourceBundle, UserConfiguration userConfiguration) {
        return new TakeScreenshotTask(networkManager, resourceBundle, userConfiguration);
    }
}
