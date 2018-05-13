package com.fuckmyclassic.network;

import com.fuckmyclassic.ui.component.UiPropertyContainer;
import com.fuckmyclassic.userconfig.UserConfiguration;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.application.Platform;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.fuckmyclassic.network.NetworkConstants.CONNECTION_TIMEOUT;
import static com.fuckmyclassic.network.NetworkConstants.CONSOLE_PORT;
import static com.fuckmyclassic.network.NetworkConstants.USER_NAME;
import static com.fuckmyclassic.ui.component.UiPropertyContainer.CONNECTED_CIRCLE_COLOR;
import static com.fuckmyclassic.ui.component.UiPropertyContainer.CONNECTED_STATUS_KEY;
import static com.fuckmyclassic.ui.component.UiPropertyContainer.DISCONNECTED_CIRCLE_COLOR;
import static com.fuckmyclassic.ui.component.UiPropertyContainer.DISCONNECTED_STATUS_KEY;

/**
 * Class to manage network connections to one or more Mini consoles. This
 * class handles connecting to the console and re-connecting automatically
 * once it's detected, as well as enabling SSH commands.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class NetworkManager {

    static Logger LOG = LogManager.getLogger(NetworkManager.class.getName());

    /** Private instance of the JSch structure. */
    private final JSch jSch;
    /** Property container so we can update the UI. */
    final UiPropertyContainer uiPropertyContainer;
    /** A mapping of currently connected console IPs to their Sessions */
    private final Map<String, Session> connectedConsoles;
    /** Background listener to poll for MDNS records */
    private final MdnsListener mdnsListener;
    /** Background service to poll for connections. */
    private final NetworkPollingService pollingService;
    /** User configuration instance, so we can keep track of the logically connected consoles */
    private final UserConfiguration userConfiguration;
    /** List of listeners to alert when a new connection is made. */
    private Set<SshConnectionListener> connectionListeners;
    /** ResourceBundle for getting localized connection status strings. */
    private ResourceBundle resourceBundle;

    /**
     * Constructor.
     * @throws JSchException
     */
    @Autowired
    public NetworkManager(final JSch jSch,
                          final MdnsListener mdnsListener,
                          final UiPropertyContainer uiPropertyContainer,
                          final UserConfiguration userConfiguration) {
        this.jSch = jSch;
        this.mdnsListener = mdnsListener;
        this.uiPropertyContainer = uiPropertyContainer;
        this.userConfiguration = userConfiguration;
        this.connectionListeners = new HashSet<>();
        this.resourceBundle = ResourceBundle.getBundle("i18n/MainWindow");
        this.connectedConsoles = new HashMap<>();

        // set a background service that polls for a connection periodically
        this.pollingService = new NetworkPollingService();
        pollingService.setNetworkManager(this);
        pollingService.setMdnsListener(mdnsListener);
        pollingService.setPeriod(Duration.seconds(1));
    }

    /**
     * Connects the SSH session to the given console.
     * @param address The IP address to connect to
     * @throws JSchException
     */
    public void connect(final String address) throws JSchException {
        if (this.connectedConsoles.containsKey(address)) {
            this.connectedConsoles.get(address).disconnect();
            this.connectedConsoles.remove(address);
        }

        final Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");

        final Session session = this.jSch.getSession(USER_NAME, address, CONSOLE_PORT);
        session.setConfig(config);
        session.setServerAliveInterval(5000);
        session.connect(CONNECTION_TIMEOUT);
        this.connectedConsoles.put(address, session);
    }

    /**
     * Disconnects the SSH session from the console.
     */
    public void disconnect(final String address) {
        if (this.isConnected(address)) {
            this.connectedConsoles.get(address).disconnect();
        }

        this.connectedConsoles.remove(address);
    }

    /**
     * Disconnects all currently connected SSH sessions.
     */
    public void disconnectAll() {
        for (Session session : this.connectedConsoles.values()) {
            if (session.isConnected()) {
                session.disconnect();
            }

            if (this.connectedConsoles.containsKey(session.getHost())) {
                this.connectedConsoles.remove(session.getHost());
            }
        }
    }

    /**
     * Says whether the SSH connection is established.
     * @return
     */
    public boolean isConnected(final String address) {
        return (this.connectedConsoles.containsKey(address) &&
                this.connectedConsoles.get(address).isConnected());
    }

    /**
     * Starts polling for new connections.
     */
    public void beginPolling() {
        if (!this.pollingService.isRunning()) {
            this.pollingService.start();
        }

        this.mdnsListener.beginPolling();
    }

    /**
     * Stops polling for new connections.
     */
    public void endPolling() throws IOException {
        if (this.pollingService.isRunning()) {
            this.pollingService.cancel();
        }

        this.mdnsListener.endPolling();
    }

    /**
     * Add a new connection listener.
     * @param connectionListener
     */
    public void addConnectionListener(final SshConnectionListener connectionListener) {
        this.connectionListeners.add(connectionListener);
    }

    /**
     * Removes a connection listener.
     * @param connectionListener
     */
    public void removeConnectionListener(final SshConnectionListener connectionListener) {
        this.connectionListeners.remove(connectionListener);
    }

    /**
     * Runs a command on the console, throwing an exception if the exit code was non-zero.
     * @param address The address of the console to run the command on
     * @param command The command to execute
     * @return The output of the command
     * @throws IOException
     * @throws JSchException
     */
    public String runCommand(final String address, final String command) throws IOException, JSchException {
        return runCommand(address, command, true);
    }

    /**
     * Runs a command on the console, optionally throwing an exception if the exit code was non-zero.
     * @param address The address of the console to run the command on
     * @param command The command to execute
     * @param throwOnNonZero Whether or not to throw an exception if the exit code was non-zero
     * @return The output of the command
     * @throws IOException
     * @throws JSchException
     */
    public String runCommand(final String address, final String command, boolean throwOnNonZero)
            throws IOException, JSchException {
        final SshCommandResult result = getRunCommandResult(address, command);

        if (throwOnNonZero && result.getExitCode() != 0) {
            throw new SshNonZeroExitCodeException();
        }

        return result.getOutput();
    }

    /**
     * Runs a command through SSH on the console.
     * @param address The address of the console to run the command on
     * @param command The command to be run remotely
     * @return The output and exit status of the command.
     */
    public SshCommandResult getRunCommandResult(final String address,
                                                final String command) throws JSchException, IOException {
        if (this.isConnected(address)) {
            final ChannelExec channel = (ChannelExec) this.connectedConsoles.get(address).openChannel("exec");

            try {
                channel.setCommand(command);
                channel.setErrStream(System.err);
                channel.setInputStream(null);

                final InputStream outStream = channel.getInputStream();
                final StringBuilder outputString = new StringBuilder();

                byte[] buf = new byte[1024];
                channel.connect(CONNECTION_TIMEOUT);

                while (true) {
                    while (outStream.available() > 0) {
                        int i = outStream.read(buf, 0, 1024);
                        if (i < 0) {
                            break;
                        }

                        outputString.append(new String(buf, 0, i));
                    }

                    if (channel.isClosed()) {
                        if (outStream.available() > 0) {
                            continue;
                        }

                        break;
                    }
                }

                final SshCommandResult result = new SshCommandResult(channel.getExitStatus(), outputString.toString());

                LOG.debug(String.format("[SSH] %s # exit code: %d", command, result.getExitCode()));
                LOG.trace(result.getOutput());

                return result;
            } finally {
                channel.disconnect();
            }
        } else {
            throw new RuntimeException("Cannot execute SSH command, the connection is null or terminated");
        }
    }

    /**
     * Runs a command remotely through SSH, redirecting the stdin/stdout/stderr streams locally.
     * @param address The address of the console to run the command on
     * @param command The remote command to run
     * @param stdin The stdin stream to use for the command
     * @param stdout The stdout stream to use for the command
     * @param stderr The stderr stream to use for the command
     * @return The exit status of the command
     * @throws JSchException
     * @throws IOException
     */
    public int runCommandWithStreams(final String address,
                                     final String command,
                                     final InputStream stdin,
                                     final OutputStream stdout,
                                     final OutputStream stderr) throws JSchException, IOException {
        if (this.isConnected(address)) {
            final ChannelExec channel = (ChannelExec) this.connectedConsoles.get(address).openChannel("exec");

            try {
                channel.setCommand(command);
                channel.setInputStream(stdin);
                channel.setOutputStream(stdout);
                channel.setErrStream(stderr);

                channel.connect(CONNECTION_TIMEOUT);

                while (!channel.isClosed()) {
                    try {
                        this.connectedConsoles.get(address).sendKeepAliveMsg();
                    } catch (final Exception ex) {
                        throw new IOException(ex);
                    }

                    try {
                        TimeUnit.SECONDS.sleep(1L);
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new IOException(ex);
                    }
                }

                int result = channel.getExitStatus();
                LOG.debug(String.format("[SSH] %s # exit code: %d", command, result));

                return result;
            } finally {
                channel.disconnect();
            }
        } else {
            throw new RuntimeException("Cannot execute SSH command, the connection is null or terminated");
        }
    }

    /**
     * Sets the connection related FXML properties.
     * @param connected
     */
    public void setConnectedProperties(boolean connected) {
        // set the connection status properties
        this.uiPropertyContainer.selectedConsoleConnectionStatus.setValue(resourceBundle.getString(
                connected ? CONNECTED_STATUS_KEY : DISCONNECTED_STATUS_KEY));
        this.uiPropertyContainer.connectionStatusColor.setValue(Paint.valueOf(
                connected ? CONNECTED_CIRCLE_COLOR : DISCONNECTED_CIRCLE_COLOR));
    }

    /**
     * Notify the connection handlers of the current status if it's changed.
     * @param address The address of the console that generated the event
     * @param connected Whether or not the console is connected
     */
    public void notifyConnectionHandlers(final String address,
                                         final boolean connected) {
        // notify the listeners if this is a new connect or disconnect
        if (this.userConfiguration.isAddressConnected(address) != connected) {
            try {
                if (connected) {
                    for (SshConnectionListener listener : this.connectionListeners) {
                        listener.onSshConnected(address);
                    }
                } else {
                    for (SshConnectionListener listener : this.connectionListeners) {
                        listener.onSshDisconnected(address);
                    }
                }

                if (userConfiguration.getSelectedConsole().getLastKnownAddress().equals(address)) {
                    this.uiPropertyContainer.selectedConsoleDisconnected.setValue(!connected);
                    Platform.runLater(() -> setConnectedProperties(connected));
                }
            } catch (Exception e) {
                LOG.error(e);
            }
        }
    }

    public Map<String, Session> getConnectedConsoles() {
        return connectedConsoles;
    }
}
