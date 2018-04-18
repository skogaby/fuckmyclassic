package com.fuckmyclassic.network;

import java.util.concurrent.ExecutionException;

/**
 * Simple listener interface so objects can take action when a new console is connected.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public interface SshConnectionListener {

    /**
     * Callback for when a new console is connected.
     */
    void onSshConnected() throws InterruptedException, ExecutionException;

    /**
     * Callback for when a console disconnects.
     */
    void onSshDisconnected();
}
