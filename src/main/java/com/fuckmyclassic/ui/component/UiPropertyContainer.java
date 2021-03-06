package com.fuckmyclassic.ui.component;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Paint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ResourceBundle;

/**
 * Container class to hold properties to bind to the UI, such as connection
 * status and number of games currently selected.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class UiPropertyContainer {

    // Resource keys for connection status localized strings and displays
    public static final String DISCONNECTED_STATUS_KEY = "MainWindow.lblConsoleDisconnected";
    public static final String CONNECTED_STATUS_KEY = "MainWindow.lblConsoleConnected";
    public static final String DISCONNECTED_CIRCLE_COLOR = "CRIMSON";
    public static final String CONNECTED_CIRCLE_COLOR = "LIMEGREEN";

    // static CSS for simple theming
    private static final String BASE_PROGRESSBAR_STYLE = "-fx-control-inner-background: dimgray; " +
            "-fx-text-box-border: white; -fx-shadow-highlight-color: gray; ";
    private static final String GREEN_PROGRESSBAR_STYLE = BASE_PROGRESSBAR_STYLE + "-fx-box-border: black; -fx-accent: limegreen;";
    private static final String YELLOW_PROGRESSBAR_STYLE = BASE_PROGRESSBAR_STYLE + "-fx-box-border: black; -fx-accent: goldenrod;";
    private static final String RED_PROGRESSBAR_STYLE = BASE_PROGRESSBAR_STYLE + "-fx-box-border: black; -fx-accent: red;";
    private static final String BLACK_PROGRESSBAR_STYLE = BASE_PROGRESSBAR_STYLE + "-fx-box-border: black; -fx-accent: black;";

    /** A property that exposes whether the console is connected */
    public final BooleanProperty selectedConsoleDisconnected;
    /** A property that displays the localized connection status */
    public final StringProperty selectedConsoleConnectionStatus;
    /** A property that displays the color representing the connection status */
    public final ObjectProperty<Paint> connectionStatusColor;
    /** A property to display how many games are currently selected */
    public final LongProperty numSelected;
    /** A property to display the overall space usage in a progress bar */
    public final DoubleProperty gameSpaceUsed;
    /** The style for the progress bar in the main window */
    public final StringProperty progressBarStyle;
    /** ResourceBundle for getting localized connection status strings. */
    private final ResourceBundle resourceBundle;

    @Autowired
    public UiPropertyContainer() {
        this.resourceBundle = ResourceBundle.getBundle("i18n/MainWindow");
        this.selectedConsoleDisconnected = new SimpleBooleanProperty(true);
        this.selectedConsoleConnectionStatus = new SimpleStringProperty(resourceBundle.getString(DISCONNECTED_STATUS_KEY));
        this.connectionStatusColor = new SimpleObjectProperty<>(Paint.valueOf(DISCONNECTED_CIRCLE_COLOR));
        this.numSelected = new SimpleLongProperty(0);
        this.gameSpaceUsed = new SimpleDoubleProperty(0);
        this.progressBarStyle = new SimpleStringProperty(GREEN_PROGRESSBAR_STYLE);

        this.gameSpaceUsed.addListener(((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() >= 1.0) {
                this.progressBarStyle.setValue(BLACK_PROGRESSBAR_STYLE);
            } else if (newValue.doubleValue() > 0.90) {
                this.progressBarStyle.setValue(RED_PROGRESSBAR_STYLE);
            } else if (newValue.doubleValue() > 0.80) {
                this.progressBarStyle.setValue(YELLOW_PROGRESSBAR_STYLE);
            } else {
                this.progressBarStyle.setValue(GREEN_PROGRESSBAR_STYLE);
            }
        }));
    }

    /**
     * Sets the connection related FXML properties.
     * @param connected
     */
    public void setConnectedProperties(boolean connected) {
        // set the connection status properties
        this.selectedConsoleConnectionStatus.setValue(resourceBundle.getString(
                connected ? CONNECTED_STATUS_KEY : DISCONNECTED_STATUS_KEY));
        this.connectionStatusColor.setValue(Paint.valueOf(
                connected ? CONNECTED_CIRCLE_COLOR : DISCONNECTED_CIRCLE_COLOR));
        this.selectedConsoleDisconnected.setValue(!connected);
    }
}
