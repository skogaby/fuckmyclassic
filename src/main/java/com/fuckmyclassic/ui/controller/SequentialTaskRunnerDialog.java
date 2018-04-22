package com.fuckmyclassic.ui.controller;

import com.fuckmyclassic.shared.SharedConstants;
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

@Component
public class SequentialTaskRunnerDialog {

    static Logger LOG = LogManager.getLogger(SequentialTaskRunnerDialog.class.getName());

    // FXML components
    public Label lblMainMessage;
    public Label lblSubTaskMessage;
    public ProgressBar prgMainTaskProgress;
    public ProgressBar prgSubTaskProgress;

    /** The list of TaskCreators that should be run on the next invocation. */
    private TaskCreator[] taskCreators;
    /** The message to set for the main task. */
    private String mainTaskMessage;

    @Autowired
    public SequentialTaskRunnerDialog() {

    }

    /**
     * Run the designated tasks on window creation.
     * @throws InterruptedException
     */
    @FXML
    public void initialize() throws InterruptedException {
        if (this.taskCreators != null && this.taskCreators.length != 0) {
            final SequentialTaskRunner taskRunner = new SequentialTaskRunner();
            taskRunner.setTaskCreators(taskCreators);
            taskRunner.setMainTaskMessage(mainTaskMessage);

            // bind our properties and start the task runner
            BindingHelper.bindProperty(taskRunner.messageProperty(), lblMainMessage.textProperty());
            BindingHelper.bindProperty((ReadOnlyProperty<?>) taskRunner.subTaskMessageProperty(), lblSubTaskMessage.textProperty());
            BindingHelper.bindProperty(taskRunner.progressProperty(), prgMainTaskProgress.progressProperty());
            BindingHelper.bindProperty((ReadOnlyProperty<?>) taskRunner.subTaskProgressProperty(), prgSubTaskProgress.progressProperty());

            // close this window after everything is done
            taskRunner.setOnSucceeded(event -> {
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
        final FXMLLoader loader = new FXMLLoader(SequentialTaskRunnerDialog.class.getClassLoader()
                .getResource("fxml/SequentialTaskRunnerDialog.fxml"));
        loader.setController(this);

        final Stage stage = new Stage();
        final Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(String.format("%s v%s", SharedConstants.APP_NAME, SharedConstants.APP_VERSION));
        stage.show();
    }

    public TaskCreator[] getTaskCreators() {
        return taskCreators;
    }

    public SequentialTaskRunnerDialog setTaskCreators(TaskCreator... taskCreators) {
        this.taskCreators = taskCreators;
        return this;
    }

    public String getMainTaskMessage() {
        return mainTaskMessage;
    }

    public SequentialTaskRunnerDialog setMainTaskMessage(String mainTaskMessage) {
        this.mainTaskMessage = mainTaskMessage;
        return this;
    }
}
