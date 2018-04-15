package com.fuckmyclassic;

import com.fuckmyclassic.network.NetworkConnection;
import com.fuckmyclassic.spring.configuration.ApplicationConfiguration;
import com.fuckmyclassic.shared.SharedConstants;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.hibernate.Session;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ResourceBundle;

/**
 * Main driver class for the program.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class Main extends Application {

    private AnnotationConfigApplicationContext applicationContext;

    @Override
    public void start(Stage primaryStage) throws Exception {
        applicationContext = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
        ResourceBundle resources = ResourceBundle.getBundle("i18n/MainWindow");
        FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("fxml/MainWindow.fxml"), resources);
        loader.setControllerFactory(applicationContext::getBean);

        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle(String.format("%s v%s", SharedConstants.APP_NAME, SharedConstants.APP_VERSION));
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (applicationContext != null) {
            // shut down the Hibernate session if one exists
            final Session session = applicationContext.getBean(Session.class);

            if (session != null && session.isOpen()) {
                session.close();
            }

            // close the SSH connection if there is one
            final NetworkConnection networkConnection = applicationContext.getBean(NetworkConnection.class);

            if (networkConnection != null) {
                networkConnection.disconnect();
            }

            applicationContext.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
