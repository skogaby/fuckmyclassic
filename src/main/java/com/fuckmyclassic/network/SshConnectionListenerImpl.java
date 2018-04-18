package com.fuckmyclassic.network;

import com.fuckmyclassic.task.GetConsoleSidTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

/**
 * Default listener for new console connections to the app that gets
 * registered on app start.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class SshConnectionListenerImpl implements SshConnectionListener {

    static Logger LOG = LogManager.getLogger(SshConnectionListenerImpl.class.getName());

    /**
     * Task to get the console's SID.
     */
    private final GetConsoleSidTask getConsoleSidTask;

    @Autowired
    public SshConnectionListenerImpl(final GetConsoleSidTask getConsoleSidTask) {
        this.getConsoleSidTask = getConsoleSidTask;
    }

    /**
     * Handles all the tasks that need to be performed when a console first connects.
     */
    @Override
    public void onSshConnected() throws InterruptedException, ExecutionException {
        LOG.info("New console connected");

        final String consoleSid = this.getConsoleSidTask.runAndGetResult();
        LOG.info(String.format("Fetched the console SID: %s", consoleSid));
    }

    /**
     * Handler for when the console disconnects.
     */
    @Override
    public void onSshDisconnected() {
        LOG.info("Console disconnected");
    }
}
