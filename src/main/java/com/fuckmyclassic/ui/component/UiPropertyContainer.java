package com.fuckmyclassic.ui.component;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

    /** A property that exposes whether the console is connected */
    public final BooleanProperty disconnected;
    /** A property that displays the localized connection status */
    public final StringProperty connectionStatus;
    /** A property that displays the color representing the connection status */
    public final ObjectProperty<Paint> connectionStatusColor;
    /** A property to display how many games are currently selected */
    public final LongProperty numSelected;

    @Autowired
    public UiPropertyContainer() {
        final ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/MainWindow");
        this.disconnected = new SimpleBooleanProperty(true);
        this.connectionStatus = new SimpleStringProperty(resourceBundle.getString(DISCONNECTED_STATUS_KEY));
        this.connectionStatusColor = new SimpleObjectProperty<>(Paint.valueOf(DISCONNECTED_CIRCLE_COLOR));
        this.numSelected = new SimpleLongProperty(0);
    }
}
