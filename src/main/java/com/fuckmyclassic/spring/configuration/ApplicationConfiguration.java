package com.fuckmyclassic.spring.configuration;

import com.fuckmyclassic.boot.KernelFlasher;
import com.fuckmyclassic.boot.MembootHelper;
import com.fuckmyclassic.management.LibraryManager;
import com.fuckmyclassic.network.NetworkConnection;
import com.fuckmyclassic.ui.controller.MainWindow;
import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.LibraryDAO;
import com.fuckmyclassic.ui.util.ImageResizer;
import com.fuckmyclassic.userconfig.UserConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Bean config class for the top-level application.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Configuration
@ComponentScan({"com.fuckmyclassic.spring.configuration"})
public class ApplicationConfiguration {

    @Bean
    public MainWindow mainWindow(UserConfiguration userConfiguration, MembootHelper membootHelper, KernelFlasher kernelFlasher,
                                    LibraryManager libraryManager, NetworkConnection networkConnection) {
        return new MainWindow(userConfiguration, membootHelper, kernelFlasher,
                libraryManager, networkConnection);
    }

    @Bean
    public LibraryManager libraryManager(UserConfiguration userConfiguration, HibernateManager hibernateManager,
                                         LibraryDAO libraryDAO, ImageResizer imageResizer) {
        return new LibraryManager(userConfiguration, hibernateManager, libraryDAO, imageResizer);
    }

    @Bean
    public ImageResizer imageResizer() {
        return new ImageResizer();
    }

    @Bean
    public UserConfiguration userConfiguration() {
        return UserConfiguration.loadFromTomlFile();
    }
}
