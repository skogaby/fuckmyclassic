package com.fuckmyclassic.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default listener for new console connections to the app that gets
 * registered on app start.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class SshConnectionListenerImpl implements SshConnectionListener {

    static Logger LOG = LogManager.getLogger(SshConnectionListenerImpl.class.getName());

    @Autowired
    public SshConnectionListenerImpl() {

    }

    /**
     * Handles all the tasks that need to be performed when a console first connects.
     */
    @Override
    public void onSshConnected() {
        LOG.info("New console connected");
    }
}
