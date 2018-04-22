package com.fuckmyclassic.network;

/**
 * Simple class to return SSH command results. Includes both the exit
 * code and the actual output.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class SshCommandResult {

    /** The exit code for the SSH command. */
    private int exitCode;
    /** The output for the SSH command. */
    private String output;

    /**
     * Constructor.
     * @param exitCode
     * @param output
     */
    public SshCommandResult(int exitCode, String output) {
        this.exitCode = exitCode;
        this.output = output;
    }

    /**
     * Get the exit code for the command.
     * @return
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * The the output for the command.
     * @return
     */
    public String getOutput() {
        return output;
    }
}
