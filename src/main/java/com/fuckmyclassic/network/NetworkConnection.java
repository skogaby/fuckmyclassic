package com.fuckmyclassic.network;

import com.fuckmyclassic.ui.component.UiPropertyContainer;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.fuckmyclassic.network.NetworkConstants.CONNECTION_TIMEOUT;
import static com.fuckmyclassic.network.NetworkConstants.CONSOLE_IP;
import static com.fuckmyclassic.network.NetworkConstants.CONSOLE_PORT;
import static com.fuckmyclassic.network.NetworkConstants.USER_NAME;
import static com.fuckmyclassic.ui.component.UiPropertyContainer.CONNECTED_CIRCLE_COLOR;
import static com.fuckmyclassic.ui.component.UiPropertyContainer.CONNECTED_STATUS_KEY;
import static com.fuckmyclassic.ui.component.UiPropertyContainer.DISCONNECTED_CIRCLE_COLOR;
import static com.fuckmyclassic.ui.component.UiPropertyContainer.DISCONNECTED_STATUS_KEY;

/**
 * Class to represent a network connection to the Mini console. This
 * class handles connecting to the console and re-connecting automatically
 * once it's detected, as well as enabling SSH commands.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class NetworkConnection {

    static Logger LOG = LogManager.getLogger(NetworkConnection.class.getName());

    /** Private instance of the JSch structure. */
    private final JSch jSch;
    /** Property container so we can update the UI. */
    final UiPropertyContainer uiPropertyContainer;
    /** The actual SSH connection to the console. */
    public Session connection;
    /** Background service to poll for connections. */
    private final NetworkPollingService pollingService;
    /** List of listeners to alert when a new connection is made. */
    private Set<SshConnectionListener> connectionListeners;
    /** ResourceBundle for getting localized connection status strings. */
    private ResourceBundle resourceBundle;
    /** The SID of the currently connected console. */
    private String connectedConsoleSid;
    /** The system type of the currently connected console. */
    private String systemType;
    /** The sync path for the currently connected console. */
    private String systemSyncPath;

    /**
     * Constructor.
     * @throws JSchException

     */
    @Autowired
    public NetworkConnection(final JSch jSch, final UiPropertyContainer uiPropertyContainer) {
        this.jSch = jSch;
        this.uiPropertyContainer = uiPropertyContainer;
        this.connectionListeners = new HashSet<>();
        this.resourceBundle = ResourceBundle.getBundle("i18n/MainWindow");
        this.connectedConsoleSid = null;

        // set a background service that polls for a connection periodically
        this.pollingService = new NetworkPollingService();
        pollingService.setNetworkConnection(this);
        pollingService.setPeriod(Duration.seconds(1));
        pollingService.setOnSucceeded(t -> setConnectedProperties((boolean)t.getSource().getValue()));
    }

    /**
     * Connects the SSH session to the console.
     * @throws JSchException
     */
    public void connect() throws JSchException {
        if (this.connection != null) {
            this.connection.disconnect();
            this.connection = null;
        }

        final Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");

        this.connection = this.jSch.getSession(USER_NAME, CONSOLE_IP, CONSOLE_PORT);
        this.connection.setConfig(config);
        this.connection.setServerAliveInterval(5000);
        this.connection.connect(CONNECTION_TIMEOUT);
    }

    /**
     * Disconnects the SSH session from the console.
     */
    public void disconnect() {
        if (this.isConnected()) {
            this.connection.disconnect();
            this.connection = null;
        }
    }

    /**
     * Says whether the SSH connection is established.
     * @return
     */
    public boolean isConnected() {
        return (this.connection != null &&
                this.connection.isConnected());
    }

    /**
     * Starts polling for new connections.
     */
    public void beginPolling() {
        if (!this.pollingService.isRunning()) {
            this.pollingService.start();
        }
    }

    /**
     * Stops polling for new connections.
     */
    public void endPolling() {
        if (this.pollingService.isRunning()) {
            this.pollingService.cancel();
        }
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
     * @param command The command to execute
     * @return The output of the command
     * @throws IOException
     * @throws JSchException
     */
    public String runCommand(final String command) throws IOException, JSchException {
        return runCommand(command, true);
    }

    /**
     * Runs a command on the console, optionally throwing an exception if the exit code was non-zero.
     * @param command The command to execute
     * @param throwOnNonZero Whether or not to throw an exception if the exit code was non-zero
     * @return The output of the command
     * @throws IOException
     * @throws JSchException
     */
    public String runCommand(final String command, boolean throwOnNonZero) throws IOException, JSchException {
        final SshCommandResult result = getRunCommandResult(command);

        if (throwOnNonZero && result.getExitCode() != 0) {
            throw new SshNonZeroExitCodeException();
        }

        return result.getOutput();
    }

    /**
     * Runs a command through SSH on the console.
     * @param command The command to be run remotely.
     * @return The output and exit status of the command.
     */
    public SshCommandResult getRunCommandResult(final String command) throws JSchException, IOException {
        if (this.isConnected()) {
            final ChannelExec channel = (ChannelExec)this.connection.openChannel("exec");

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
     * @param command The remote command to run
     * @param stdin The stdin stream to use for the command
     * @param stdout The stdout stream to use for the command
     * @param stderr The stderr stream to use for the command
     * @return The exit status of the command
     * @throws JSchException
     * @throws IOException
     */
    public int runCommandWithStreams(final String command, final InputStream stdin, final OutputStream stdout,
                                     final OutputStream stderr) throws JSchException, IOException {
        if (this.isConnected()) {
            final ChannelExec channel = (ChannelExec)this.connection.openChannel("exec");

            try {
                channel.setCommand(command);
                channel.setInputStream(stdin);
                channel.setOutputStream(stdout);
                channel.setErrStream(stderr);

                channel.connect(CONNECTION_TIMEOUT);

                while (!channel.isClosed()) {
                    try {
                        this.connection.sendKeepAliveMsg();
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
     * Sets the connection related FXML properties, and notifies connection listeners if the status changes.
     * @param connected
     */
    public void setConnectedProperties(boolean connected) {
        // set the connection status properties
        this.uiPropertyContainer.connectionStatus.setValue(resourceBundle.getString(
                connected ? CONNECTED_STATUS_KEY : DISCONNECTED_STATUS_KEY));
        this.uiPropertyContainer.connectionStatusColor.setValue(Paint.valueOf(
                connected ? CONNECTED_CIRCLE_COLOR : DISCONNECTED_CIRCLE_COLOR));
    }

    /**
     * Notify the connection handlers of the current status if it's changed.
     * @param connected
     */
    public void notifyConnectionHandlers(boolean connected) {
        // notify the listeners if this is a new connect or disconnect
        if (this.uiPropertyContainer.disconnected.getValue() == connected) {
            try {
                if (connected) {
                    for (SshConnectionListener listener : this.connectionListeners) {
                        listener.onSshConnected();
                    }
                } else {
                    for (SshConnectionListener listener : this.connectionListeners) {
                        listener.onSshDisconnected();
                    }
                }
            } catch (Exception e) {
                LOG.error(e);
            }
        }

        this.uiPropertyContainer.disconnected.setValue(!connected);
    }

    public String getConnectedConsoleSid() {
        return connectedConsoleSid;
    }

    public NetworkConnection setConnectedConsoleSid(String connectedConsoleSid) {
        this.connectedConsoleSid = connectedConsoleSid;
        return this;
    }

    public String getSystemType() {
        return systemType;
    }

    public NetworkConnection setSystemType(String systemType) {
        this.systemType = systemType;
        return this;
    }

    public String getSystemSyncPath() {
        return systemSyncPath;
    }

    public NetworkConnection setSystemSyncPath(String systemSyncPath) {
        this.systemSyncPath = systemSyncPath;
        return this;
    }
}
