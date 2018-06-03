package com.fuckmyclassic.spring.configuration;

import com.fuckmyclassic.boot.KernelFlasher;
import com.fuckmyclassic.boot.MembootHelper;
import com.fuckmyclassic.hibernate.dao.impl.ApplicationDAO;
import com.fuckmyclassic.hibernate.dao.impl.ConsoleDAO;
import com.fuckmyclassic.hibernate.dao.impl.LibraryDAO;
import com.fuckmyclassic.hibernate.dao.impl.LibraryItemDAO;
import com.fuckmyclassic.management.LibraryManager;
import com.fuckmyclassic.network.NetworkManager;
import com.fuckmyclassic.task.TaskProvider;
import com.fuckmyclassic.task.impl.RsyncDataTask;
import com.fuckmyclassic.ui.component.UiPropertyContainer;
import com.fuckmyclassic.ui.controller.LibraryManagementWindow;
import com.fuckmyclassic.ui.controller.MainWindow;
import com.fuckmyclassic.ui.controller.RsyncRunnerDialog;
import com.fuckmyclassic.ui.controller.SequentialTaskRunnerDialog;
import com.fuckmyclassic.ui.controller.SingleTaskRunnerDialog;
import com.fuckmyclassic.ui.util.ImageResizer;
import com.fuckmyclassic.userconfig.PathConfiguration;
import com.fuckmyclassic.userconfig.UserConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Bean config class for the top-level application.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Configuration
@ComponentScan({"com.fuckmyclassic.spring.configuration"})
public class ApplicationConfiguration {

    @Bean
    public MainWindow mainWindow(UserConfiguration userConfiguration, MembootHelper membootHelper, KernelFlasher kernelFlasher,
                                 LibraryManager libraryManager, NetworkManager networkManager, ResourceBundle tasksResourceBundle,
                                 RsyncRunnerDialog rsyncRunnerDialog, SingleTaskRunnerDialog singleTaskRunnerDialog,
                                 SequentialTaskRunnerDialog sequentialTaskRunnerDialog, TaskProvider taskProvider,
                                 UiPropertyContainer uiPropertyContainer, PathConfiguration pathConfiguration, ConsoleDAO consoleDAO,
                                 LibraryManagementWindow libraryManagementWindow) {
        return new MainWindow(userConfiguration, pathConfiguration, membootHelper, kernelFlasher, libraryManager,
                networkManager, tasksResourceBundle, singleTaskRunnerDialog, sequentialTaskRunnerDialog, rsyncRunnerDialog, taskProvider,
                uiPropertyContainer, consoleDAO, libraryManagementWindow);
    }

    @Bean
    public LibraryManagementWindow libraryManagementWindow(UserConfiguration userConfiguration,
                                                           PathConfiguration pathConfiguration,
                                                           ConsoleDAO consoleDAO,
                                                           LibraryDAO libraryDAO,
                                                           ApplicationDAO applicationDAO,
                                                           LibraryItemDAO libraryItemDAO) {
        return new LibraryManagementWindow(userConfiguration, pathConfiguration, consoleDAO, libraryDAO, applicationDAO, libraryItemDAO);
    }

    @Bean
    public SingleTaskRunnerDialog singleTaskRunnerDialog() {
        return new SingleTaskRunnerDialog();
    }

    @Bean
    public SequentialTaskRunnerDialog sequentialTaskRunnerDialog() {
        return new SequentialTaskRunnerDialog();
    }

    @Bean RsyncRunnerDialog rsyncRunnerDialog(RsyncDataTask rsyncDataTask, ResourceBundle resourceBundle) {
        return new RsyncRunnerDialog(rsyncDataTask, resourceBundle);
    }

    @Bean
    public LibraryManager libraryManager(UserConfiguration userConfiguration, PathConfiguration pathConfiguration,
                                         ApplicationDAO applicationDAO, LibraryItemDAO libraryItemDAO, ImageResizer imageResizer,
                                         UiPropertyContainer uiPropertyContainer) {
        return new LibraryManager(userConfiguration, pathConfiguration, applicationDAO, libraryItemDAO,
                imageResizer, uiPropertyContainer);
    }

    @Bean
    public ImageResizer imageResizer() {
        return new ImageResizer();
    }

    @Bean
    public UserConfiguration userConfiguration(ConsoleDAO consoleDAO) {
        final UserConfiguration userConfiguration = new UserConfiguration(consoleDAO);
        userConfiguration.initFromTomlFile();

        return userConfiguration;
    }

    @Bean
    public ResourceBundle tasksResourceBundle() {
        return ResourceBundle.getBundle("i18n/Tasks");
    }

    @Bean
    public UiPropertyContainer uiPropertyContainer() {
        return new UiPropertyContainer();
    }

    @Bean
    public PathConfiguration pathConfiguration() throws IOException {
        return new PathConfiguration();
    }
}
