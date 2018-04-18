package com.fuckmyclassic.network;

import com.jcraft.jsch.JSchException;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Simple scheduled service to poll the console for a connection and set the
 * connection status appropriately.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class NetworkPollingService extends ScheduledService<Boolean> {

    private NetworkConnection networkConnection;

    public NetworkConnection getNetworkConnection() {
        return networkConnection;
    }

    public NetworkPollingService setNetworkConnection(NetworkConnection networkConnection) {
        this.networkConnection = networkConnection;
        return this;
    }

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws IOException {
                boolean connected = false;

                if (networkConnection != null) {
                    if (networkConnection.isConnected()) {
                        connected = true;
                    } else {
                        if (InetAddress.getByName(NetworkConstants.CONSOLE_IP).isReachable(500)) {
                            try {
                                networkConnection.connect();
                                connected = true;
                            } catch (JSchException e) {
                                connected = false;
                            }
                        }
                    }
                }

                networkConnection.notifyConnectionHandlers(connected);
                return connected;
            }
        };
    }
}
