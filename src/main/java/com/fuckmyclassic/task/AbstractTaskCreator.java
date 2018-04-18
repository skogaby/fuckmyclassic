package com.fuckmyclassic.task;

import javafx.concurrent.Task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Abstract class for the TaskCreators to encapsulate being able to run one in isolation.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public abstract class AbstractTaskCreator<T> implements TaskCreator<T> {

    @Override
    public abstract Task createTask();

    /**
     * Creates a Task from this TaskCreator, executes it, and returns the result.
     * @param <T>
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public <T extends Object> T runAndGetResult() throws InterruptedException, ExecutionException {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final Task<T> task = this.createTask();
        executorService.submit(task);
        return task.get();
    }
}
