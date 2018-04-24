package com.fuckmyclassic;

import com.fuckmyclassic.network.NetworkConnection;
import com.fuckmyclassic.network.SshConnectionListener;
import com.fuckmyclassic.spring.configuration.ApplicationConfiguration;
import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.userconfig.UserConfiguration;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.hibernate.Session;
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

    /**
     * Spring context that we can load beans from manually.
     */
    private AnnotationConfigApplicationContext applicationContext;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // do a little pre-setup
        setupRuntimeDirectories();

        // load the controller and show the window
        applicationContext = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
        ResourceBundle resources = ResourceBundle.getBundle("i18n/MainWindow");
        FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("fxml/MainWindow.fxml"), resources);
        loader.setControllerFactory(applicationContext::getBean);

        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle(String.format("%s v%s", SharedConstants.APP_NAME, SharedConstants.APP_VERSION));

        // fix for the process not fully ending when you click 'X'
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();

        // start polling for a network connection once the main window is initialized
        final NetworkConnection networkConnection = applicationContext.getBean(NetworkConnection.class);
        final SshConnectionListener connectionListener = applicationContext.getBean(SshConnectionListener.class);
        networkConnection.addConnectionListener(connectionListener);
        networkConnection.beginPolling();
    }

    @Override
    public void stop() throws IOException {
        if (applicationContext != null) {
            // shut down the Hibernate session if one exists
            final Session session = applicationContext.getBean(Session.class);

            if (session != null && session.isOpen()) {
                session.close();
            }

            // close the SSH connection if there is one
            final NetworkConnection networkConnection = applicationContext.getBean(NetworkConnection.class);

            if (networkConnection != null) {
                networkConnection.endPolling();
                networkConnection.disconnect();
            }

            // save the config file to disk
            final UserConfiguration userConfiguration = applicationContext.getBean(UserConfiguration.class);

            if (userConfiguration != null) {
                UserConfiguration.saveTomlFile(userConfiguration);
            }

            applicationContext.close();
        }
    }

    /**
     * Create the directories we need for runtime such as boxart, games, etc.
     * and also pre-populate any static content we need.
     */
    public static void setupRuntimeDirectories() throws URISyntaxException, IOException {
        // create the directories
        final File gamesDir = new File(SharedConstants.GAMES_DIRECTORY);
        final File boxartDir = new File(SharedConstants.BOXART_DIRECTORY);
        gamesDir.mkdirs();
        boxartDir.mkdirs();

        // prepopulate static assets
        URL warningResource = ClassLoader.getSystemResource(
                Paths.get(SharedConstants.IMAGES_DIRECTORY, SharedConstants.WARNING_IMAGE).toString());
        Files.copy(
                Paths.get(warningResource.toURI()).toFile().toPath(),
                Paths.get(SharedConstants.BOXART_DIRECTORY, SharedConstants.WARNING_IMAGE),
                StandardCopyOption.REPLACE_EXISTING);

        warningResource = ClassLoader.getSystemResource(
                Paths.get(SharedConstants.IMAGES_DIRECTORY, SharedConstants.WARNING_IMAGE_THUMBNAIL).toString());
        Files.copy(
                Paths.get(warningResource.toURI()).toFile().toPath(),
                Paths.get(SharedConstants.BOXART_DIRECTORY, SharedConstants.WARNING_IMAGE_THUMBNAIL),
                StandardCopyOption.REPLACE_EXISTING);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
