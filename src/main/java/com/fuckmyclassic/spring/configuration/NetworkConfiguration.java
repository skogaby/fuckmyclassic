package com.fuckmyclassic.spring.configuration;

import com.fuckmyclassic.network.NetworkConnection;
import com.fuckmyclassic.network.SshConnectionListener;
import com.fuckmyclassic.network.SshConnectionListenerImpl;
import com.fuckmyclassic.task.GetConsoleSidTask;
import com.fuckmyclassic.task.UpdateUnknownLibrariesTask;
import com.fuckmyclassic.ui.controller.SequentialTaskRunnerDialog;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static com.fuckmyclassic.network.NetworkConstants.SSH_PRIVATE_KEY;

/**
 * Spring configuration class for network beans.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Configuration
@ComponentScan({"com.fuckmyclassic.spring.configuration"})
public class NetworkConfiguration {

    @Bean
    public JSch jSch() throws URISyntaxException, JSchException {
        final JSch jSch = new JSch();
        jSch.addIdentity(Paths.get(ClassLoader.getSystemResource(SSH_PRIVATE_KEY).toURI()).toString());

        return jSch;
    }

    @Bean
    public NetworkConnection networkConnection(JSch jSch) throws JSchException {
        return new NetworkConnection(jSch);
    }

    @Bean
    public SshConnectionListener sshConnectionListener(SequentialTaskRunnerDialog sequentialTaskRunnerDialog,
                                                       GetConsoleSidTask getConsoleSidTask, UpdateUnknownLibrariesTask updateUnknownLibrariesTask) {
        return new SshConnectionListenerImpl(sequentialTaskRunnerDialog, getConsoleSidTask, updateUnknownLibrariesTask);
    }
}
