package com.fuckmyclassic.task.impl;

import com.fuckmyclassic.rsync.RsyncCompletionCallback;
import com.fuckmyclassic.rsync.RsyncOutputCallback;
import com.fuckmyclassic.rsync.RsyncOutputProcessor;
import com.fuckmyclassic.task.AbstractTaskCreator;
import com.github.fracpete.processoutput4j.output.StreamingProcessOutput;
import com.github.fracpete.rsync4j.RSync;
import javafx.concurrent.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ResourceBundle;

/**
 * Task that will use rsync to sync data from a given source folder
 * to a given destination folder.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class RsyncDataTask extends AbstractTaskCreator<Void> {

    static Logger LOG = LogManager.getLogger(RsyncDataTask.class.getName());

    private final String IN_PROGRESS_MESSAGE_KEY = "RsyncDataTask.inProgressMessage";
    private final String COMPLETE_MESSAGE_KEY = "RsyncDataTask.completeMessage";

    /** The ResourceBundle for Task related strings */
    private final ResourceBundle resourceBundle;
    /** Private instance of the rsync wrapper */
    private RSync rSync;
    /** The source path to sync from */
    private String source;
    /** The destination path to sync to */
    private String destination;
    /** Callback for the stdout of the rsync process */
    private RsyncOutputCallback stdoutCallback;
    /** Callback for the stderr of the rsync process */
    private RsyncOutputCallback stderrCallback;
    /** Callback for rsync process completion */
    private RsyncCompletionCallback rsyncCompletionCallback;

    @Autowired
    public RsyncDataTask(final ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    @Override
    public Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (StringUtils.isBlank(source) ||
                        StringUtils.isBlank(destination)) {
                    throw new RuntimeException("Both source and destination need to be set for rsync");
                }

                LOG.info(String.format("Beginning data sync from %s to %s", source, destination));
                updateMessage(String.format(resourceBundle.getString(IN_PROGRESS_MESSAGE_KEY), source, destination));
                updateProgress(0, 100);

                // setup an rsync client and its monitor
                rSync = new RSync()
                        .source(source)
                        .destination(destination)
                        .delete(true)
                        .copyLinks(true)
                        .progress(true)
                        .humanReadable(true)
                        .owner(false)
                        .group(false)
                        .recursive(true);

                final RsyncOutputProcessor rsyncOutputProcessor = new RsyncOutputProcessor();
                final StreamingProcessOutput processOutput = new StreamingProcessOutput(rsyncOutputProcessor);

                if (stdoutCallback != null) {
                    rsyncOutputProcessor.setStdoutCallback(stdoutCallback);
                }

                if (stderrCallback != null) {
                    rsyncOutputProcessor.setStderrCallback(stderrCallback);
                }

                // perform the actual sync
                processOutput.monitor(rSync.builder());

                LOG.info(String.format("Done syncing data from %s to %s", source, destination));
                updateMessage(COMPLETE_MESSAGE_KEY);
                updateProgress(100, 100);

                if (rsyncCompletionCallback != null) {
                    rsyncCompletionCallback.call();
                }

                return null;
            }
        };
    }

    public String getSource() {
        return source;
    }

    public RsyncDataTask setSource(String source) {
        this.source = source;
        return this;
    }

    public String getDestination() {
        return destination;
    }

    public RsyncDataTask setDestination(String destination) {
        this.destination = destination;
        return this;
    }

    public RsyncOutputCallback getStdoutCallback() {
        return stdoutCallback;
    }

    public RsyncDataTask setStdoutCallback(RsyncOutputCallback stdoutCallback) {
        this.stdoutCallback = stdoutCallback;
        return this;
    }

    public RsyncOutputCallback getStderrCallback() {
        return stderrCallback;
    }

    public RsyncDataTask setStderrCallback(RsyncOutputCallback stderrCallback) {
        this.stderrCallback = stderrCallback;
        return this;
    }

    public RsyncCompletionCallback getRsyncCompletionCallback() {
        return rsyncCompletionCallback;
    }

    public RsyncDataTask setRsyncCompletionCallback(RsyncCompletionCallback rsyncCompletionCallback) {
        this.rsyncCompletionCallback = rsyncCompletionCallback;
        return this;
    }
}
