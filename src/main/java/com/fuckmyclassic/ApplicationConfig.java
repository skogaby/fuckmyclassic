package com.fuckmyclassic;

import com.fuckmyclassic.boot.KernelFlasher;
import com.fuckmyclassic.network.SshConnection;
import com.fuckmyclassic.ui.MainWindow;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Bean config class for the top-level application.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Configuration
@ComponentScan({"com.fuckmyclassic.network"})
public class ApplicationConfig {

    @Bean
    public MainApplication getMainApplication(MainWindow mainWindow) {
        return new MainApplication(mainWindow);
    }

    @Bean
    public MainWindow getMainWindow(KernelFlasher kernelFlasher) {
        return new MainWindow(kernelFlasher);
    }

    @Bean
    public KernelFlasher getKernelFlasher(SshConnection sshConnection) {
        return new KernelFlasher(sshConnection);
    }
}
