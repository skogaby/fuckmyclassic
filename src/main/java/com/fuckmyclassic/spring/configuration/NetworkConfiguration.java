package com.fuckmyclassic.spring.configuration;

import com.fuckmyclassic.network.SshConnection;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static com.fuckmyclassic.network.NetworkConstants.SSH_PRIVATE_KEY;

/**
 * Spring configuration class for network beans.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Configuration
public class NetworkConfiguration {

    @Bean
    public JSch getJschInstance() throws URISyntaxException, JSchException {
        final JSch jSch = new JSch();
        jSch.addIdentity(Paths.get(ClassLoader.getSystemResource(SSH_PRIVATE_KEY).toURI()).toString());

        return jSch;
    }

    @Bean
    public SshConnection getSshConnection(JSch jSch) throws JSchException {
        return new SshConnection(jSch);
    }
}
