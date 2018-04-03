package com.fuckmyclassic.controller;

import com.fuckmyclassic.boot.KernelFlasher;
import com.fuckmyclassic.boot.MembootHelper;
import com.fuckmyclassic.management.Application;
import com.fuckmyclassic.testdata.ApplicationTestData;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.usb.UsbException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import static com.fuckmyclassic.boot.KernelFlasher.BOOT_IMG_PATH;

/**
 * Controller for the main application window.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class MainWindow {

    static Logger LOG = LogManager.getLogger(MainWindow.class.getName());

    // References to all of the UI objects that we need to manipulate
    @FXML private TreeView<Application> treeViewGames;
    @FXML private Label lblApplicationId;
    @FXML private Label lblGameSize;
    @FXML private CheckBox chkCompressed;
    @FXML private TextField txtApplicationName;
    @FXML private TextField txtApplicationSortName;
    @FXML private RadioButton radOnePlayer;
    @FXML private RadioButton radTwoPlayerNoSim;
    @FXML private RadioButton radTwoPlayerSim;
    @FXML private DatePicker dateReleaseDate;
    @FXML private TextField txtPublisher;
    @FXML private Spinner spnSaveCount;
    @FXML private TextField txtCommandLine;
    @FXML private TextField txtGameGenieCodes;

    /**
     * Helper instance for membooting consoles.
     */
    private final MembootHelper membootHelper;

    /**
     * Helper instance for kernel flashing operations.
     */
    private final KernelFlasher kernelFlasher;

    /**
     * Resource bundle so we can get localized strings.
     */
    private final ResourceBundle resources;

    /**
     * Constructor.
     * @param membootHelper
     * @param kernelFlasher
     */
    @Autowired
    public MainWindow(final MembootHelper membootHelper,
                      final KernelFlasher kernelFlasher) {
        this.membootHelper = membootHelper;
        this.kernelFlasher = kernelFlasher;
        this.resources = ResourceBundle.getBundle("i18n/MainWindow");
    }

    /**
     * Initialize the main window of the application.
     */
    @FXML
    public void initialize() {
        // setup the application view
        initializeApplicationTreeView();
    }

    /**
     * Initializes the TreeView display for the applications and games.
     */
    private void initializeApplicationTreeView() {
        this.treeViewGames.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // bind the UI to the data
            final Application app = newValue.getValue();
            final Application oldApp = oldValue == null ? null : oldValue.getValue();

            BindingHelper.bindProperty(app.applicationIdProperty(), this.lblApplicationId.textProperty());
            BindingHelper.bindProperty(app.applicationSizeProperty().asString(), this.lblGameSize.textProperty());
            BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.isCompressedProperty(),
                    app.isCompressedProperty(), this.chkCompressed.selectedProperty());
            BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.applicationNameProperty(),
                    app.applicationNameProperty(), this.txtApplicationName.textProperty());
            BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.sortNameProperty(),
                    app.sortNameProperty(), this.txtApplicationSortName.textProperty());
            BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.publisherProperty(),
                    app.publisherProperty(), this.txtPublisher.textProperty());
            BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.commandLineProperty(),
                    app.commandLineProperty(), this.txtCommandLine.textProperty());

            this.treeViewGames.refresh();
        });

        // populate with test data until we store real data persistently
        this.treeViewGames.setRoot(ApplicationTestData.getTestApplicationData());
    }

    // Stubbed out methods for testing FEL functionality
    @FXML
    private void onMembootCustomKernelClicked() throws UsbException, URISyntaxException {
        LOG.debug("Memboot button clicked");
        final Path bootImgPath = Paths.get(ClassLoader.getSystemResource(BOOT_IMG_PATH).toURI());
        this.membootHelper.membootKernelImage(bootImgPath);
    }

    @FXML
    private void onFlashCustomKernelClicked() throws UsbException, URISyntaxException, InterruptedException {
        LOG.debug("Custom kernel flash button clicked");
        this.kernelFlasher.flashCustomKernel();
    }
}
