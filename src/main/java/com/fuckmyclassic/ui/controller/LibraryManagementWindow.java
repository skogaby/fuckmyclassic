package com.fuckmyclassic.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * The window to let the user manage libraries and see data about them.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class LibraryManagementWindow {

    static Logger LOG = LogManager.getLogger(LibraryManagementWindow.class.getName());

    private static final String TITLE_STRING_KEY = "LibraryManagementWindow.titleBar";

    @Autowired
    public LibraryManagementWindow() {

    }

    @FXML
    public void initialize() {

    }

    /**
     * Spawns a new management window.
     */
    public void showWindow() throws IOException {
        final ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/LibraryManagementWindow");
        final FXMLLoader loader = new FXMLLoader(LibraryManagementWindow.class.getClassLoader()
                .getResource("fxml/LibraryManagementWindow.fxml"), resourceBundle);
        loader.setController(this);

        final Stage stage = new Stage();
        final Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(resourceBundle.getString(TITLE_STRING_KEY));
        stage.showAndWait();
    }
}
