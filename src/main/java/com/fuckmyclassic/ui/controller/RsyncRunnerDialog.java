package com.fuckmyclassic.ui.controller;

import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.task.impl.RsyncDataTask;
import com.fuckmyclassic.userconfig.UserConfiguration;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Dialog that's responsible for running rsync and showing the progress to the user.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class RsyncRunnerDialog {

    public static final String IN_PROGRESS_MESSAGE_KEY = "RsyncDataTask.inProgressMessage";
    public static final String RSYNC_MODULE_NAME = "root";
    public static final String RSYNC_USER_NAME = "root";
    public static final String RSYNC_PASSWORD_FILE = "rsync-password";

    static Logger LOG = LogManager.getLogger(RsyncRunnerDialog.class.getName());

    // FXML components
    public Label lblMainMessage;
    public Label lblSubTaskMessage;
    public ProgressBar prgMainTaskProgress;
    public ProgressBar prgSubTaskProgress;

    /** Rsync task */
    private final RsyncDataTask rsyncDataTask;
    /** The ResourceBundle for Task related strings */
    private final ResourceBundle resourceBundle;
    /** User configuration, so we can get console IPs, etc. */
    private final UserConfiguration userConfiguration;
    /** The local path to pass to rsync */
    private String localPath;
    /** The destination path to sync to */
    private String remotePath;
    /** Whether we're uploading to the console or downloading from the console */
    private boolean upload;

    @Autowired
    public RsyncRunnerDialog(final RsyncDataTask rsyncDataTask,
                             final ResourceBundle resourceBundle,
                             final UserConfiguration userConfiguration) {
        this.rsyncDataTask = rsyncDataTask;
        this.resourceBundle = resourceBundle;
        this.userConfiguration = userConfiguration;
        this.upload = false;
    }

    /**
     * Run rsync on window creation.
     * @throws InterruptedException
     */
    @FXML
    public void initialize() {
        final String source = upload ? this.localPath : this.remotePath;
        final String destination = upload ? this.remotePath : this.localPath;

        this.lblMainMessage.setText(String.format(resourceBundle.getString(IN_PROGRESS_MESSAGE_KEY),
                source, destination));

        rsyncDataTask.setSource(source);
        rsyncDataTask.setDestination(destination);
        rsyncDataTask.setStderrCallback(output -> LOG.error(output));
        rsyncDataTask.setStdoutCallback(output -> {
            LOG.info(output);

            if (output.contains("CLV-")) {
                Platform.runLater(() -> lblSubTaskMessage.setText(output));
            }

            if (output.contains("to-check") ||
                    output.contains("to-chk")) {
                final String index = output.trim().split("\\s+")[5].split("=")[1];
                final int first = Integer.parseInt(index.split("/")[0]);
                final int second = Integer.parseInt(index.split("/")[1].split("\\)")[0]);
                Platform.runLater(() -> prgMainTaskProgress.setProgress(((double) second - first) / second));
            }

            if (output.contains("%")) {
                int percentage = Integer.parseInt(output.trim().split("\\s+")[1].split("%")[0]);
                Platform.runLater(() -> prgSubTaskProgress.setProgress(((double) percentage) / 100));
            }
        });

        final Task<Void> task = rsyncDataTask.createTask();

        // close this window after everything is done
        task.setOnSucceeded(event -> {
            ((Stage) this.lblMainMessage.getScene().getWindow()).close();
        });

        task.setOnFailed(event -> {
            LOG.error(event.getSource().getMessage());
            event.getSource().getException().printStackTrace();
            ((Stage) this.lblMainMessage.getScene().getWindow()).close();
        });

        final Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Spawns a new task runner dialog.
     * @throws IOException
     */
    public void showDialog() throws IOException {
        final FXMLLoader loader = new FXMLLoader(RsyncRunnerDialog.class.getClassLoader()
                .getResource("fxml/RsyncRunnerDialog.fxml"));
        loader.setController(this);

        final Stage stage = new Stage();
        final Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(String.format("%s v%s", SharedConstants.APP_NAME, SharedConstants.APP_VERSION));
        stage.showAndWait();
    }

    /**
     * Takes the given remote
     * @param remotePath
     * @return
     */
    private String getConnectionPath(final String remotePath) {
        return String.format("rsync://%s@%s/%s%s", RSYNC_USER_NAME, this.userConfiguration.getSelectedConsole().getLastKnownAddress(),
                RSYNC_MODULE_NAME, remotePath);
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = getConnectionPath(remotePath);
    }

    public boolean isUpload() {
        return upload;
    }

    public void setUpload(boolean upload) {
        this.upload = upload;
    }
}
