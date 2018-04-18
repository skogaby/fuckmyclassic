package com.fuckmyclassic.task;

import com.fuckmyclassic.ui.util.BindingHelper;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

    /**
     * This progress of the currently-executing subtask.
     */
    private final DoubleProperty subTaskProgress;

    /**
     * The list of TaskCreators to execute in a sequence.
     */
    private TaskCreator[] taskCreators;

    @Autowired
    public SequentialTaskRunner() {
        this.subTaskProgress = new SimpleDoubleProperty(0.0);
    }

    /**
     * Executes the given tasks in sequential order, throwing an exception if any of the subtasks
     * doesn't complete successfully.
     * @return
     * @throws InterruptedException
     */
    @Override
    protected Void call() throws InterruptedException {
        final AtomicBoolean exceptionThrown = new AtomicBoolean(false);
        final Thread.UncaughtExceptionHandler exceptionHandler = ((t, e) -> exceptionThrown.set(true));

        // now, actually execute the tasks
        for (AtomicInteger i = new AtomicInteger(0); i.get() < taskCreators.length; i.incrementAndGet()) {
            TaskCreator service = taskCreators[i.get()];
            Task task = service.createTask();

            // bind the message and subtask progress to the task runner
            BindingHelper.bindProperty(task.progressProperty(), subTaskProgress);
            task.messageProperty().addListener(((observable, oldValue, newValue) -> updateMessage(newValue)));

            // execute each task sequentially. if there's an exception then don't execute the rest, otherwise
            // update the message and progress
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler(exceptionHandler);
            thread.start();
            thread.join();

            // if the last task threw an exception, stop here and throw our own exception
            if (exceptionThrown.get()) {
                throw new InterruptedException();
            }
        }

        return null;
    }

    public SequentialTaskRunner setServicesToExecute(TaskCreator... taskCreators) {
        this.taskCreators = taskCreators;
        return this;
    }

    /**
     * Creates a SequentialTaskRunner for the given tasks, creates a new thread for it and
     * waits for the thread to finish.
     * @param taskCreators The TaskCreators to execute.
     */
    public static void createAndRunTaskRunner(TaskCreator... taskCreators) throws InterruptedException {
        final SequentialTaskRunner taskRunner = new SequentialTaskRunner();
        taskRunner.setServicesToExecute(taskCreators);

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
}
