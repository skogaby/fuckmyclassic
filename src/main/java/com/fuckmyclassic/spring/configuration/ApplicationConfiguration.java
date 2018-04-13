package com.fuckmyclassic.spring.configuration;

import com.fuckmyclassic.boot.KernelFlasher;
import com.fuckmyclassic.boot.MembootHelper;
import com.fuckmyclassic.management.LibraryManager;
import com.fuckmyclassic.ui.controller.MainWindow;
import com.fuckmyclassic.hibernate.ApplicationDAO;
import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.LibraryDAO;
import com.fuckmyclassic.ui.util.ImageResizer;
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
    public MainWindow getMainWindow(HibernateManager hibernateManager, ApplicationDAO applicationDAO,
                                    MembootHelper membootHelper, KernelFlasher kernelFlasher,
                                    LibraryManager libraryManager) {
        return new MainWindow(hibernateManager, applicationDAO, membootHelper, kernelFlasher, libraryManager);
    }

    @Bean
    public LibraryManager libraryManager(HibernateManager hibernateManager, ApplicationDAO applicationDAO,
                                         LibraryDAO libraryDAO, ImageResizer imageResizer) {
        return new LibraryManager(hibernateManager, applicationDAO, libraryDAO, imageResizer);
    }

    @Bean
    public ImageResizer imageResizer() {
        return new ImageResizer();
    }
}
