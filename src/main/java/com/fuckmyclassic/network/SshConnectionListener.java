package com.fuckmyclassic.network;

/**
 * Simple listener interface so objects can take action when a new console is connected.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public interface SshConnectionListener {

    /**
     * Callback for when a new console is connected.
     */
    void onSshConnected(String address);

    /**
     * Callback for when a console disconnects.
     */
    void onSshDisconnected(String address);
}
