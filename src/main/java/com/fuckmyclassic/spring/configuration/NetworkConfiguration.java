package com.fuckmyclassic.spring.configuration;

import com.fuckmyclassic.network.NetworkConnection;
import com.fuckmyclassic.network.SshConnectionListener;
import com.fuckmyclassic.network.SshConnectionListenerImpl;
import com.fuckmyclassic.task.TaskProvider;
import com.fuckmyclassic.ui.component.UiPropertyContainer;
import com.fuckmyclassic.ui.controller.SequentialTaskRunnerDialog;
import com.jcraft.jsch.JSch;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.ResourceBundle;

/**
 * Spring configuration class for network beans.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Configuration
@ComponentScan({"com.fuckmyclassic.spring.configuration"})
public class NetworkConfiguration {

    @Bean
    public JSch jSch() {
        return new JSch();
    }

    @Bean
    public NetworkConnection networkConnection(JSch jSch, UiPropertyContainer uiPropertyContainer) {
        return new NetworkConnection(jSch, uiPropertyContainer);
    }

    @Bean
    public SshConnectionListener sshConnectionListener(ResourceBundle resourceBundle, SequentialTaskRunnerDialog sequentialTaskRunnerDialog,
                                                       TaskProvider taskProvider) {
        return new SshConnectionListenerImpl(resourceBundle, sequentialTaskRunnerDialog, taskProvider);
    }
}
