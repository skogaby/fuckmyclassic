package com.fuckmyclassic.rsync;

import com.github.fracpete.processoutput4j.core.StreamingProcessOutputType;
import com.github.fracpete.processoutput4j.core.StreamingProcessOwner;

/**
 * Implementation of StreamingProcessOwner, so we can process the output of rsync in realtime and update
 * the UI appropriately.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class RsyncOutputProcessor implements StreamingProcessOwner {

    private RsyncOutputCallback stdoutCallback;
    private RsyncOutputCallback stderrCallback;

    @Override
    public StreamingProcessOutputType getOutputType() {
        return StreamingProcessOutputType.BOTH;
    }

    @Override
    public void processOutput(String line, boolean stdout) {
        if (stdout) {
            stdoutCallback.process(line);
        } else {
            stderrCallback.process(line);
        }
    }

    public RsyncOutputCallback getStdoutCallback() {
        return stdoutCallback;
    }

    public RsyncOutputProcessor setStdoutCallback(RsyncOutputCallback stdoutCallback) {
        this.stdoutCallback = stdoutCallback;
        return this;
    }

    public RsyncOutputCallback getStderrCallback() {
        return stderrCallback;
    }

    public RsyncOutputProcessor setStderrCallback(RsyncOutputCallback stderrCallback) {
        this.stderrCallback = stderrCallback;
        return this;
    }
}
