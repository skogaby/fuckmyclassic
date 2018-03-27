package com.fuckmyclassic.boot;

import com.fuckmyclassic.network.SshConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Bean config class for the boot package.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Configuration
@ComponentScan({"com.fuckmyclassic.network"})
public class BootConfiguration {

    @Bean
    public KernelFlasher getKernelFlasher(SshConnection sshConnection, MembootHelper membootHelper) {
        return new KernelFlasher(sshConnection, membootHelper);
    }

    @Bean MembootHelper getMembootHelper() {
        return new MembootHelper();
    }
}
