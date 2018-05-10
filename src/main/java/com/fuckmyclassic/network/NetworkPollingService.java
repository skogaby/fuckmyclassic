package com.fuckmyclassic.network;

import com.jcraft.jsch.JSchException;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Simple scheduled service to poll the known consoles for a connection and set the
 * connection status appropriately.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class NetworkPollingService extends ScheduledService<Void> {

    /** Network connection manager so we can connect to consoles */
    private NetworkManager networkManager;
    /** The MDNS listener that tells us which IPs to poll for */
    private MdnsListener mdnsListener;

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws IOException {
                boolean connected = false;

                if (networkManager != null &&
                        mdnsListener != null) {
                    for (String address : mdnsListener.getAdvertisedAddresses()) {
                        if (networkManager.isConnected(address)) {
                            connected = true;
                        } else {
                            if (InetAddress.getByName(address).isReachable(500)) {
                                try {
                                    networkManager.connect(address);
                                    connected = true;
                                } catch (JSchException e) {
                                    connected = false;
                                }
                            }
                        }

                        networkManager.notifyConnectionHandlers(address, connected);
                    }
                }

                return null;
            }
        };
    }

    public NetworkPollingService setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
        return this;
    }

    public NetworkPollingService setMdnsListener(MdnsListener mdnsListener) {
        this.mdnsListener = mdnsListener;
        return this;
    }
}
