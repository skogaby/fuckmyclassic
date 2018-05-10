package com.fuckmyclassic.spring.configuration;

import com.fuckmyclassic.boot.KernelFlasher;
import com.fuckmyclassic.boot.MembootHelper;
import com.fuckmyclassic.hibernate.dao.ConsoleDAO;
import com.fuckmyclassic.management.LibraryManager;
import com.fuckmyclassic.network.NetworkManager;
import com.fuckmyclassic.task.TaskProvider;
import com.fuckmyclassic.task.impl.RsyncDataTask;
import com.fuckmyclassic.ui.component.UiPropertyContainer;
import com.fuckmyclassic.ui.controller.MainWindow;
import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.dao.LibraryDAO;
import com.fuckmyclassic.ui.controller.RsyncRunnerDialog;
import com.fuckmyclassic.ui.controller.SequentialTaskRunnerDialog;
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
                                 RsyncRunnerDialog rsyncRunnerDialog, SequentialTaskRunnerDialog sequentialTaskRunnerDialog, TaskProvider taskProvider,
                                 UiPropertyContainer uiPropertyContainer, PathConfiguration pathConfiguration) {
        return new MainWindow(userConfiguration, pathConfiguration, membootHelper, kernelFlasher, libraryManager,
                networkManager, tasksResourceBundle, sequentialTaskRunnerDialog, rsyncRunnerDialog, taskProvider, uiPropertyContainer);
    }

    @Bean
    public SequentialTaskRunnerDialog sequentialTaskRunnerDialog() {
        return new SequentialTaskRunnerDialog();
    }

    @Bean RsyncRunnerDialog rsyncRunnerDialog(RsyncDataTask rsyncDataTask, ResourceBundle resourceBundle) {
        return new RsyncRunnerDialog(rsyncDataTask, resourceBundle);
    }

    @Bean
    public LibraryManager libraryManager(UserConfiguration userConfiguration, PathConfiguration pathConfiguration, HibernateManager hibernateManager,
                                         LibraryDAO libraryDAO, ImageResizer imageResizer, UiPropertyContainer uiPropertyContainer) {
        return new LibraryManager(userConfiguration, pathConfiguration, hibernateManager, libraryDAO, imageResizer, uiPropertyContainer);
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
