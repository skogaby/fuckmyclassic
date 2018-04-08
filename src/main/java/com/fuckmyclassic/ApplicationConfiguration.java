package com.fuckmyclassic;

import com.fuckmyclassic.boot.KernelFlasher;
import com.fuckmyclassic.boot.MembootHelper;
import com.fuckmyclassic.controller.MainWindow;
import com.fuckmyclassic.hibernate.ApplicationDAO;
import com.fuckmyclassic.hibernate.HibernateManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Bean config class for the top-level application.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Configuration
@ComponentScan({"com.fuckmyclassic.configuration"})
public class ApplicationConfiguration {

    @Bean
    public MainWindow getMainWindow(HibernateManager hibernateManager, ApplicationDAO applicationDAO,
                                    MembootHelper membootHelper, KernelFlasher kernelFlasher) {
        return new MainWindow(hibernateManager, applicationDAO, membootHelper, kernelFlasher);
    }
}
