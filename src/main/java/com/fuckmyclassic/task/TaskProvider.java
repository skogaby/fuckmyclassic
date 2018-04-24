package com.fuckmyclassic.task;

import com.fuckmyclassic.task.impl.CreateTempDataTask;
import com.fuckmyclassic.task.impl.GetConsoleIdsAndPathsTask;
import com.fuckmyclassic.task.impl.LoadLibrariesTask;
import com.fuckmyclassic.task.impl.UpdateUnknownLibrariesTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Simple class that contains an instance of all the Task beans so we don't need
 * to add an instance for every task we need to all the dependent classes.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class TaskProvider {

    // Tasks to provide
    public final CreateTempDataTask createTempDataTask;
    public final GetConsoleIdsAndPathsTask getConsoleIdsAndPathsTask;
    public final LoadLibrariesTask loadLibrariesTask;
    public final UpdateUnknownLibrariesTask updateUnknownLibrariesTask;

    @Autowired
    public TaskProvider(final CreateTempDataTask createTempDataTask,
                        final GetConsoleIdsAndPathsTask getConsoleIdsAndPathsTask,
                        final LoadLibrariesTask loadLibrariesTask,
                        final UpdateUnknownLibrariesTask updateUnknownLibrariesTask) {
        this.createTempDataTask = createTempDataTask;
        this.getConsoleIdsAndPathsTask = getConsoleIdsAndPathsTask;
        this.loadLibrariesTask = loadLibrariesTask;
        this.updateUnknownLibrariesTask = updateUnknownLibrariesTask;
    }
}
