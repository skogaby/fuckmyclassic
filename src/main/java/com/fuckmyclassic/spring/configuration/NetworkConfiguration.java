package com.fuckmyclassic.spring.configuration;

import com.fuckmyclassic.hibernate.dao.ConsoleDAO;
import com.fuckmyclassic.network.MdnsListener;
import com.fuckmyclassic.network.NetworkManager;
import com.fuckmyclassic.network.SshConnectionListener;
import com.fuckmyclassic.network.SshConnectionListenerImpl;
import com.fuckmyclassic.task.TaskProvider;
import com.fuckmyclassic.ui.component.UiPropertyContainer;
import com.fuckmyclassic.ui.controller.SequentialTaskRunnerDialog;
import com.fuckmyclassic.userconfig.UserConfiguration;
import com.jcraft.jsch.JSch;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
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
    public NetworkManager networkConnection(JSch jSch, MdnsListener mdnsListener, UiPropertyContainer uiPropertyContainer) {
        return new NetworkManager(jSch, mdnsListener, uiPropertyContainer);
    }

    @Bean
    public SshConnectionListener sshConnectionListener(ResourceBundle resourceBundle, SequentialTaskRunnerDialog sequentialTaskRunnerDialog,
                                                       TaskProvider taskProvider, ConsoleDAO consoleDAO, UserConfiguration userConfiguration,
                                                       NetworkManager networkManager) {
        return new SshConnectionListenerImpl(resourceBundle, sequentialTaskRunnerDialog, taskProvider, consoleDAO, userConfiguration, networkManager);
    }

    @Bean
    public MdnsListener mdnsListener() throws IOException {
        return new MdnsListener();
    }
}
