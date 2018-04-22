package com.fuckmyclassic.network;

/**
 * Simple class to hold constants and hardcoded values related to networking.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class NetworkConstants {

    /** The default username to login to SSH with. */
    public static final String USER_NAME = "root";
    /** The hardcoded static IP address for the console. */
    public static final String CONSOLE_IP = "10.234.137.10";
    /** The hardcoded port to SSH into for the console. */
    public static final int CONSOLE_PORT = 22;
    /** The connection timeout for SSH connections. */
    public static final int CONNECTION_TIMEOUT = 5000;
    /** The path to the hardcoded SSH private key. */
    public static final String SSH_PRIVATE_KEY = "networking/sshkey_private";
    /** The path to the hardcoded SSH private key. */
    public static final String SSH_PUBLIC_KEY = "networking/sshkey_public";
}
