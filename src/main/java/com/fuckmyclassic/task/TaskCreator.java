package com.fuckmyclassic.task;

import javafx.concurrent.Task;

/**
 * Simple interface for a class that creates Tasks. Similar to Service, except
 * it doesn't run the Tasks for you.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public interface TaskCreator<V> {

    Task<V> createTask();
}
