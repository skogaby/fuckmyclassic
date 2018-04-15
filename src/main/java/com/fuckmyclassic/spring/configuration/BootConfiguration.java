package com.fuckmyclassic.spring.configuration;

import com.fuckmyclassic.boot.KernelFlasher;
import com.fuckmyclassic.boot.MembootHelper;
import com.fuckmyclassic.network.NetworkConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Bean config class for the boot package.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Configuration
@ComponentScan({"com.fuckmyclassic.spring.configuration"})
public class BootConfiguration {

    @Bean
    public KernelFlasher getKernelFlasher(NetworkConnection networkConnection, MembootHelper membootHelper) {
        return new KernelFlasher(networkConnection, membootHelper);
    }

    @Bean MembootHelper getMembootHelper() {
        return new MembootHelper();
    }
}
