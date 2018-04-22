package com.fuckmyclassic.task;

import com.fuckmyclassic.ui.util.BindingHelper;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class that handles running a set of Tasks sequentially in a single thread.
 * If any of the tasks fails, the rest of them will not execute. This class itself
 * is a Task so we can run the needed tasks in the background and keep track
 * of the current progress and message in one place.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class SequentialTaskRunner extends Task<Void> {

    static Logger LOG = LogManager.getLogger(SequentialTaskRunner.class.getName());

    /** This progress of the currently-executing subtask. */
    private final DoubleProperty subTaskProgress;
    /** The message we should set for the top-level parent task. */
    private final StringProperty mainTaskMessage;
    /** The current message of the subtask. */
    private final StringProperty subTaskMessage;
    /** The list of TaskCreators to execute in a sequence. */
    private TaskCreator[] taskCreators;

    @Autowired
    public SequentialTaskRunner() {
        this.subTaskProgress = new SimpleDoubleProperty(0.0);
        this.subTaskMessage = new SimpleStringProperty("");
        this.mainTaskMessage = new SimpleStringProperty("");
    }

    /**
     * Executes the given tasks in sequential order, throwing an exception if any of the subtasks
     * doesn't complete successfully.
     * @return
     * @throws InterruptedException
     */
    @Override
    protected Void call() throws ExecutionException, InterruptedException {
        updateMessage(mainTaskMessage.get());
        updateProgress(0, taskCreators.length);

        final ExecutorService executorService = Executors.newSingleThreadExecutor();

        // now, actually execute the tasks
        for (int i = 0; i < taskCreators.length; i++) {
            final TaskCreator service = taskCreators[i];
            final Task task = service.createTask();

            // bind the message and subtask progress to the task runner
            BindingHelper.bindProperty(task.progressProperty(), subTaskProgress);
            BindingHelper.bindProperty(task.messageProperty(), subTaskMessage);

            // execute each task sequentially. if there's an exception then don't execute the rest, otherwise
            // update the message and progress
            executorService.submit(task);
            task.get();

            updateProgress(i + 1, taskCreators.length);
        }

        return null;
    }

    /**
     * Creates a SequentialTaskRunner for the given tasks, creates a new thread for it and
     * waits for the thread to finish.
     * @param taskCreators The TaskCreators to execute.
     */
    public static void createAndRunTaskCreators(final String mainTaskMessage, final TaskCreator... taskCreators) throws InterruptedException {
        final SequentialTaskRunner taskRunner = new SequentialTaskRunner();
        taskRunner.setTaskCreators(taskCreators);
        taskRunner.setMainTaskMessage(mainTaskMessage);

        final Thread thread = new Thread(taskRunner);
        thread.setDaemon(true);
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            LOG.error("Error executing sequential tasks", e);
            throw e;
        }
    }

    public String getMainTaskMessage() {
        return mainTaskMessage.get();
    }

    public StringProperty mainTaskMessageProperty() {
        return mainTaskMessage;
    }


    public SequentialTaskRunner setMainTaskMessage(String mainTaskMessage) {
        this.mainTaskMessage.set(mainTaskMessage);
        return this;
    }

    public TaskCreator[] getTaskCreators() {
        return taskCreators;
    }

    public SequentialTaskRunner setTaskCreators(TaskCreator[] taskCreators) {
        this.taskCreators = taskCreators;
        return this;
    }

    public double getSubTaskProgress() {
        return subTaskProgress.get();
    }

    public DoubleProperty subTaskProgressProperty() {
        return subTaskProgress;
    }

    public void setSubTaskProgress(double subTaskProgress) {
        this.subTaskProgress.set(subTaskProgress);
    }

    public String getSubTaskMessage() {
        return subTaskMessage.get();
    }

    public StringProperty subTaskMessageProperty() {
        return subTaskMessage;
    }

    public void setSubTaskMessage(String subTaskMessage) {
        this.subTaskMessage.set(subTaskMessage);
    }
}
