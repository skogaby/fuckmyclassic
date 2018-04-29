package com.fuckmyclassic.spring.configuration;

import com.fuckmyclassic.boot.KernelFlasher;
import com.fuckmyclassic.boot.MembootHelper;
import com.fuckmyclassic.management.LibraryManager;
import com.fuckmyclassic.network.NetworkConnection;
import com.fuckmyclassic.task.TaskProvider;
import com.fuckmyclassic.task.impl.RsyncDataTask;
import com.fuckmyclassic.ui.component.UiPropertyContainer;
import com.fuckmyclassic.ui.controller.MainWindow;
import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.dao.LibraryDAO;
import com.fuckmyclassic.ui.controller.RsyncRunnerDialog;
import com.fuckmyclassic.ui.controller.SequentialTaskRunnerDialog;
import com.fuckmyclassic.ui.util.ImageResizer;
import com.fuckmyclassic.userconfig.ConsoleConfiguration;
import com.fuckmyclassic.userconfig.UserConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

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
                                 LibraryManager libraryManager, NetworkConnection networkConnection, ResourceBundle tasksResourceBundle,
                                 RsyncRunnerDialog rsyncRunnerDialog, SequentialTaskRunnerDialog sequentialTaskRunnerDialog, TaskProvider taskProvider,
                                 UiPropertyContainer uiPropertyContainer, ConsoleConfiguration consoleConfiguration) {
        return new MainWindow(userConfiguration, consoleConfiguration, membootHelper, kernelFlasher, libraryManager, networkConnection, tasksResourceBundle,
                sequentialTaskRunnerDialog, rsyncRunnerDialog, taskProvider, uiPropertyContainer);
    }

    @Bean
    public SequentialTaskRunnerDialog sequentialTaskRunnerDialog() {
        return new SequentialTaskRunnerDialog();
    }

    @Bean RsyncRunnerDialog rsyncRunnerDialog(RsyncDataTask rsyncDataTask, ResourceBundle resourceBundle) {
        return new RsyncRunnerDialog(rsyncDataTask, resourceBundle);
    }

    @Bean
    public LibraryManager libraryManager(UserConfiguration userConfiguration, HibernateManager hibernateManager,
                                         LibraryDAO libraryDAO, ImageResizer imageResizer, UiPropertyContainer uiPropertyContainer) {
        return new LibraryManager(userConfiguration, hibernateManager, libraryDAO, imageResizer, uiPropertyContainer);
    }

    @Bean
    public ImageResizer imageResizer() {
        return new ImageResizer();
    }

    @Bean
    public UserConfiguration userConfiguration() {
        return UserConfiguration.loadFromTomlFile();
    }

    @Bean
    public ConsoleConfiguration consoleConfiguration() {
        return new ConsoleConfiguration();
    }

    @Bean
    public ResourceBundle tasksResourceBundle() {
        return ResourceBundle.getBundle("i18n/Tasks");
    }

    @Bean
    public UiPropertyContainer uiPropertyContainer() {
        return new UiPropertyContainer();
    }
}
