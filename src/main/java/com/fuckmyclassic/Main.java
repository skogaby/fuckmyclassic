package com.fuckmyclassic;

import com.fuckmyclassic.model.ConsoleType;
import com.fuckmyclassic.network.NetworkManager;
import com.fuckmyclassic.network.SshConnectionListener;
import com.fuckmyclassic.spring.configuration.ApplicationConfiguration;
import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.userconfig.PathConfiguration;
import com.fuckmyclassic.userconfig.UserConfiguration;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

/**
 * Main driver class for the program.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class Main extends Application {

    static Logger LOG = LogManager.getLogger(Main.class.getName());

    /** Spring context that we can load beans from manually. */
    private static AnnotationConfigApplicationContext applicationContext;

    @Override
    public void start(Stage primaryStage) throws Exception {
        applicationContext = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);

        // do a little pre-setup
        setupRuntimeDirectories();

        // load the controller and show the window
        ResourceBundle resources = ResourceBundle.getBundle("i18n/MainWindow");
        FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("fxml/MainWindow.fxml"), resources);
        loader.setControllerFactory(applicationContext::getBean);

        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle(String.format("%s v%s", SharedConstants.APP_NAME, SharedConstants.APP_VERSION));

        // fix for the process not fully ending when you click 'X'
        primaryStage.setOnCloseRequest(t -> {
            try {
                stop();
            } catch (IOException e) {
                LOG.error(e);
            }

            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();

        // start polling for a network connection once the main window is initialized
        final NetworkManager networkManager = applicationContext.getBean(NetworkManager.class);
        final SshConnectionListener connectionListener = applicationContext.getBean(SshConnectionListener.class);
        networkManager.addConnectionListener(connectionListener);
        networkManager.beginPolling();
    }

    @Override
    public void stop() throws IOException {
        if (applicationContext != null) {
            // close the SSH connections if there is one
            final NetworkManager networkManager = applicationContext.getBean(NetworkManager.class);

            if (networkManager != null) {
                networkManager.endPolling();
                networkManager.disconnectAll();
            }

            // save the config file to disk
            final UserConfiguration userConfiguration = applicationContext.getBean(UserConfiguration.class);

            if (userConfiguration != null) {
                userConfiguration.saveTomlFile();
            }

            applicationContext.close();
        }
    }

    /**
     * Create the directories we need for runtime such as boxart, games, etc.
     * and also pre-populate any static content we need.
     */
    public static void setupRuntimeDirectories() {
        // create the directories
        final PathConfiguration pathConfiguration = applicationContext.getBean(PathConfiguration.class);
        new File(pathConfiguration.gamesDirectory).mkdirs();

        for (ConsoleType consoleType : ConsoleType.values()) {
            new File(Paths.get(pathConfiguration.originalGamesDirectory, consoleType.getConsoleCode()).toString()).mkdirs();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
