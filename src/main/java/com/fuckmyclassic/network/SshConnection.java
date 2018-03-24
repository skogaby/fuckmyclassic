package com.fuckmyclassic.network;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

/**
 * Class to represent an SSH connection to the Mini console.
 * It can also be used to statically issue one-off commands
 * if you don't need to retain a long-lived connection.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class SshConnection {

    /**
     * The default username to login to SSH with.
     */
    public static final String USER_NAME = "root";

    /**
     * The hardcoded static IP address for the console.
     */
    public static final String CONSOLE_IP = "10.234.137.10";

    /**
     * The hardcoded port to SSH into for the console.
     */
    public static final int CONSOLE_PORT = 22;

    /**
     * The connection timeout for SSH connections.
     */
    public static final int CONNECTION_TIMEOUT = 5000;

    /**
     * Private instance of the JSch structure.
     */
    private JSch jSch;

    /**
     * The actual SSH connection to the console.
     */
    private Session connection;

    /**
     * Constructor.
     * @throws JSchException
     */
    public SshConnection() throws JSchException {
        this.jSch = new JSch();
        this.connection = this.jSch.getSession(USER_NAME, CONSOLE_IP, CONSOLE_PORT);
    }

    /**
     * Constructor.
     * @param connection An existing SSH connection to the console.
     */
    public SshConnection(final Session connection) {
        this.connection = connection;
    }

    /**
     * Connects the SSH session to the console.
     * @throws JSchException
     */
    public void connect() throws JSchException {
        if (this.connection != null &&
                !this.connection.isConnected()) {
            this.connection.connect(CONNECTION_TIMEOUT);
        }
    }

    /**
     * Returns whether or not the SSH session is currently connected.
     * @return
     */
    public boolean isConnected() {
        return (this.connection != null && this.connection.isConnected());
    }

    /**
     * Disconnects the SSH session from the console.
     */
    public void disconnect() {
        if (isConnected()) {
            this.connection.disconnect();
        }
    }

    /**
     * Adds a private SSH key to the identity story.
     * @param key The key to add
     * @throws JSchException
     */
    public void addPrivateSshkey(final String key) throws JSchException {
        this.jSch.addIdentity(key);
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

                System.out.println(String.format("[SSH] %s # exit code: %d", command, result.getExitCode()));
                System.out.println(outputString.toString());

                return result;
            } finally {
                channel.disconnect();
            }
        } else {
            return new SshCommandResult(-1, "[ERROR] Cannot run SSH command, connection is null or disconnected.");
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
                System.out.println(String.format("[SSH] %s # exit code: %d", command, result));

                return result;
            } finally {
                channel.disconnect();
            }
        } else {
            return -1;
        }
    }
}
