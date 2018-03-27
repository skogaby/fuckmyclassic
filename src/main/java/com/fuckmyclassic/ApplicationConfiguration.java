package com.fuckmyclassic;

import com.fuckmyclassic.boot.KernelFlasher;
import com.fuckmyclassic.boot.MembootHelper;
import com.fuckmyclassic.ui.MainWindow;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Bean config class for the top-level application.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Configuration
@ComponentScan({"com.fuckmyclassic.boot"})
public class ApplicationConfiguration {

    @Bean
    public MainWindow getMainWindow(MembootHelper membootHelper, KernelFlasher kernelFlasher) {
        return new MainWindow(membootHelper, kernelFlasher);
    }
}
