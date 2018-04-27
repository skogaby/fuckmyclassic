package com.fuckmyclassic.task;

import com.fuckmyclassic.task.impl.CreateTempDataTask;
import com.fuckmyclassic.task.impl.GetConsoleIdsAndPathsTask;
import com.fuckmyclassic.task.impl.LoadLibrariesTask;
import com.fuckmyclassic.task.impl.MountGamesAndStartUiTask;
import com.fuckmyclassic.task.impl.RsyncDataTask;
import com.fuckmyclassic.task.impl.ShowSplashScreenAndStopUiTask;
import com.fuckmyclassic.task.impl.UnmountGamesTask;
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
    public final RsyncDataTask rsyncDataTask;
    public final ShowSplashScreenAndStopUiTask showSplashScreenAndStopUiTask;
    public final UnmountGamesTask unmountGamesTask;
    public final MountGamesAndStartUiTask mountGamesAndStartUiTask;

    @Autowired
    public TaskProvider(final CreateTempDataTask createTempDataTask,
                        final GetConsoleIdsAndPathsTask getConsoleIdsAndPathsTask,
                        final LoadLibrariesTask loadLibrariesTask,
                        final UpdateUnknownLibrariesTask updateUnknownLibrariesTask,
                        final RsyncDataTask rsyncDataTask,
                        final ShowSplashScreenAndStopUiTask showSplashScreenAndStopUiTask,
                        final UnmountGamesTask unmountGamesTask,
                        final MountGamesAndStartUiTask mountGamesAndStartUiTask) {
        this.createTempDataTask = createTempDataTask;
        this.getConsoleIdsAndPathsTask = getConsoleIdsAndPathsTask;
        this.loadLibrariesTask = loadLibrariesTask;
        this.updateUnknownLibrariesTask = updateUnknownLibrariesTask;
        this.rsyncDataTask = rsyncDataTask;
        this.showSplashScreenAndStopUiTask = showSplashScreenAndStopUiTask;
        this.unmountGamesTask = unmountGamesTask;
        this.mountGamesAndStartUiTask = mountGamesAndStartUiTask;
    }
}
