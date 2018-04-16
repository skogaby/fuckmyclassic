package com.fuckmyclassic.network;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.beans.property.ObjectProperty;
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
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import static com.fuckmyclassic.network.NetworkConstants.CONNECTION_TIMEOUT;
import static com.fuckmyclassic.network.NetworkConstants.CONSOLE_IP;
import static com.fuckmyclassic.network.NetworkConstants.CONSOLE_PORT;
import static com.fuckmyclassic.network.NetworkConstants.USER_NAME;

/**
 * Class to represent a network connection to the Mini console. This
 * class handles connecting to the console and re-connecting automatically
 * once it's detected, as well as enabling SSH commands.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class NetworkConnection {

    static Logger LOG = LogManager.getLogger(NetworkConnection.class.getName());

    // Resource keys for connection status localized strings and displays
    private static final String DISCONNECTED_STATUS_KEY = "MainWindow.lblConsoleDisconnected";
    private static final String CONNECTED_STATUS_KEY = "MainWindow.lblConsoleConnected";
    private static final String DISCONNECTED_CIRCLE_COLOR = "CRIMSON";
    private static final String CONNECTED_CIRCLE_COLOR = "LIMEGREEN";

    /**
     * Private instance of the JSch structure.
     */
    private final JSch jSch;

    /**
     * The actual SSH connection to the console.
     */
    public Session connection;

    /**
     * An FXML property that exposes whether the console is connected so we can
     * bind it to the UI.
     */
    private boolean connected;

    /**
     * An FXML property that displays the localized connection status so
     * we can bind it to the UI.
     */
    private StringProperty connectionStatus;

    /**
     * An FXML property that displays the color representing the connection status.
     */
    private ObjectProperty<Paint> connectionStatusColor;

    /**
     * ResourceBundle for getting localized connection status strings.
     */
    private ResourceBundle resourceBundle;

    /**
     * Constructor.
     * @throws JSchException

     */
    @Autowired
    public NetworkConnection(final JSch jSch) throws JSchException {
        this.jSch = jSch;
        this.resourceBundle = ResourceBundle.getBundle("i18n/MainWindow");
        this.connected = false;
        this.connectionStatus = new SimpleStringProperty(resourceBundle.getString(DISCONNECTED_STATUS_KEY));
        this.connectionStatusColor = new SimpleObjectProperty<>(Paint.valueOf(DISCONNECTED_CIRCLE_COLOR));

        // set a background service that polls for a connection periodically
        final NetworkPollingService pollingService = new NetworkPollingService();
        pollingService.setNetworkConnection(this);
        pollingService.setPeriod(Duration.seconds(3));
        pollingService.setOnSucceeded(t -> setConnected((boolean)t.getSource().getValue()));

        pollingService.start();
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

        Properties config = new java.util.Properties();
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
     * Runs a command through SSH on the console.
     * @param command The command to be run remotely.
     * @return The output and exit status of the command.
     */
    public SshCommandResult runCommand(final String command) throws JSchException, IOException {
        if (this.connection != null &&
                this.connection.isConnected()) {
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
        if (this.connection != null &&
                this.connection.isConnected()) {
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

    public void setConnected(boolean connected) {
        // set the connection status properties
        setConnectionStatus(resourceBundle.getString(
                connected ? CONNECTED_STATUS_KEY : DISCONNECTED_STATUS_KEY));
        setConnectionStatusColor(Paint.valueOf(
                connected ? CONNECTED_CIRCLE_COLOR : DISCONNECTED_CIRCLE_COLOR));
    }

    public String getConnectionStatus() {
        return connectionStatus.get();
    }

    public StringProperty connectionStatusProperty() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus.set(connectionStatus);
    }

    public Paint getConnectionStatusColor() {
        return connectionStatusColor.get();
    }

    public ObjectProperty<Paint> connectionStatusColorProperty() {
        return connectionStatusColor;
    }

    public void setConnectionStatusColor(Paint connectionStatusColor) {
        this.connectionStatusColor.set(connectionStatusColor);
    }
}
