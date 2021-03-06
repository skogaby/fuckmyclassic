package com.fuckmyclassic.task.impl;

import com.fuckmyclassic.model.Console;
import com.fuckmyclassic.model.OriginalGameLists;
import com.fuckmyclassic.network.NetworkManager;
import com.fuckmyclassic.task.AbstractTaskCreator;
import com.fuckmyclassic.ui.controller.RsyncRunnerDialog;
import com.fuckmyclassic.ui.util.PlatformUtils;
import com.fuckmyclassic.userconfig.PathConfiguration;
import com.fuckmyclassic.userconfig.UserConfiguration;
import com.fuckmyclassic.util.FileUtils;
import com.jcraft.jsch.JSchException;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

/**
 * Task to handle dumping the original game data from the consoles.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class DumpOriginalGamesTask extends AbstractTaskCreator<Void> {

    static Logger LOG = LogManager.getLogger(DumpOriginalGamesTask.class.getName());

    private final String IN_PROGRESS_MESSAGE_KEY = "DumpOriginalGamesTask.inProgressMessage";
    private final String COMPLETE_MESSAGE_KEY = "DumpOriginalGamesTask.completeMessage";

    private final UserConfiguration userConfiguration;
    private final PathConfiguration pathConfiguration;
    private final NetworkManager networkManager;
    private final ResourceBundle resourceBundle;
    private final RsyncRunnerDialog rsyncRunnerDialog;

    @Autowired
    public DumpOriginalGamesTask(final UserConfiguration userConfiguration,
                                 final PathConfiguration pathConfiguration,
                                 final NetworkManager networkManager,
                                 final ResourceBundle resourceBundle,
                                 final RsyncRunnerDialog rsyncRunnerDialog) {
        this.userConfiguration = userConfiguration;
        this.pathConfiguration = pathConfiguration;
        this.networkManager = networkManager;
        this.resourceBundle = resourceBundle;
        this.rsyncRunnerDialog = rsyncRunnerDialog;
    }

    @Override
    public Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws IOException, JSchException {
                LOG.info("Ensuring local games cache is in sync");

                updateMessage(resourceBundle.getString(IN_PROGRESS_MESSAGE_KEY));
                updateProgress(0, 1);

                // use rsync to make sure the squashfs games and the games cache are in sync
                // for the running console type
                final Console selectedConsole = userConfiguration.getSelectedConsole();
                final Path dstDir = Paths.get(pathConfiguration.originalGamesDirectory, selectedConsole.getConsoleType().getConsoleCode());
                final String[] defaultGames = OriginalGameLists.getGamesForConsoleType(selectedConsole.getConsoleType());
                final int numLocalDirs = FileUtils.numFilesInDirectory(dstDir);

                // only run rsync if the count of folders in the cache is different than the canonical count
                if (numLocalDirs != defaultGames.length) {
                    final String remoteGamesPath = networkManager.runCommand("hakchi eval 'echo \"$squashfs$gamepath\"'");

                    PlatformUtils.runAndWait(() -> {
                        rsyncRunnerDialog.setRemotePath(remoteGamesPath + "/");
                        rsyncRunnerDialog.setLocalPath(dstDir.toString());
                        rsyncRunnerDialog.setUpload(false);

                        try {
                            rsyncRunnerDialog.showDialog();
                        } catch (IOException e) {
                            LOG.error(e);
                            e.printStackTrace();
                        }
                    });

                    // once we've synced the games, make sure they exist in the database
                }

                LOG.info("Local original games cache is in sync");
                updateMessage(resourceBundle.getString(COMPLETE_MESSAGE_KEY));
                updateProgress(1, 1);

                return null;
            }
        };
    }
}
