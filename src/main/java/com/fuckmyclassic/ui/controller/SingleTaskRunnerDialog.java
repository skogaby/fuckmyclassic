package com.fuckmyclassic.ui.controller;

import com.fuckmyclassic.task.SequentialTaskRunner;
import com.fuckmyclassic.task.TaskCreator;
import com.fuckmyclassic.ui.util.BindingHelper;
import javafx.beans.property.ReadOnlyProperty;
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

/**
 * Dialog window to run a single task on a background thread
 * and display its progress to the user.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class SingleTaskRunnerDialog {

    static Logger LOG = LogManager.getLogger(SingleTaskRunnerDialog.class.getName());

    // FXML components
    public Label lblMainMessage;
    public ProgressBar prgMainTaskProgress;

    /** The TaskCreator that should be run on the next invocation */
    private TaskCreator taskCreator;
    /** The title of the window */
    private String title;

    @Autowired
    public SingleTaskRunnerDialog() {

    }

    /**
     * Run the designated task on window creation.
     */
    @FXML
    public void initialize() {
        if (this.taskCreator != null) {
            final SequentialTaskRunner taskRunner = new SequentialTaskRunner();
            taskRunner.setTaskCreators(taskCreator);

            // bind our properties and start the task runner
            BindingHelper.bindProperty((ReadOnlyProperty<?>) taskRunner.subTaskMessageProperty(), lblMainMessage.textProperty());
            BindingHelper.bindProperty(taskRunner.subTaskProgressProperty(), prgMainTaskProgress.progressProperty());

            // close this window after everything is done
            taskRunner.setOnSucceeded(event -> {
                ((Stage) this.lblMainMessage.getScene().getWindow()).close();
            });

            taskRunner.setOnFailed(event -> {
                LOG.error(event.getSource().getMessage());
                ((Stage) this.lblMainMessage.getScene().getWindow()).close();
            });

            final Thread thread = new Thread(taskRunner);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * Spawns a new task runner dialog.
     * @throws IOException
     */
    public void showDialog() throws IOException {
        final FXMLLoader loader = new FXMLLoader(SingleTaskRunnerDialog.class.getClassLoader()
                .getResource("fxml/SingleTaskRunnerDialog.fxml"));
        loader.setController(this);

        final Stage stage = new Stage();
        final Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(this.title);
        stage.showAndWait();
        this.taskCreator = null;
    }

    public SingleTaskRunnerDialog setTaskCreator(TaskCreator taskCreator) {
        this.taskCreator = taskCreator;
        return this;
    }

    public SingleTaskRunnerDialog setTitle(String title) {
        this.title = title;
        return this;
    }
}
