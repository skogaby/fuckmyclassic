package com.fuckmyclassic.controller;

import com.fuckmyclassic.boot.KernelFlasher;
import com.fuckmyclassic.boot.MembootHelper;
import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.testdata.ApplicationTestData;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;

import javax.usb.UsbException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    @FXML private ToggleGroup maxPlayersToggleGroup;
    @FXML private RadioButton radOnePlayer;
    @FXML private RadioButton radTwoPlayerNoSim;
    @FXML private RadioButton radTwoPlayerSim;
    @FXML private DatePicker dateReleaseDate;
    @FXML private TextField txtPublisher;
    @FXML private Spinner spnSaveCount;
    @FXML private TextField txtCommandLine;
    @FXML private TextField txtGameGenieCodes;
    @FXML private ImageView imgBoxArtPreview;

    /**
     * Hibernate manager, for interacting with the database.
     */
    private final HibernateManager hibernateManager;

    /**
     * Helper instance for membooting consoles.
     */
    private final MembootHelper membootHelper;

    /**
     * Helper instance for kernel flashing operations.
     */
    private final KernelFlasher kernelFlasher;

    /**
     * The currently selected application in the TreeView.
     */
    private Application currentApp;

    /**
     * Constructor.
     * @param hibernateManager
     * @param membootHelper
     * @param kernelFlasher
     */
    @Autowired
    public MainWindow(final HibernateManager hibernateManager,
                      final MembootHelper membootHelper,
                      final KernelFlasher kernelFlasher) {
        this.hibernateManager = hibernateManager;
        this.membootHelper = membootHelper;
        this.kernelFlasher = kernelFlasher;
    }

    /**
     * Initialize the main window of the application.
     */
    @FXML
    public void initialize() {
        initializeApplicationTreeView();
        initializeSaveCountSpinner();
        initializePlayerCountSelection();
    }

    /**
     * Initializes the TreeView display for the applications and games.
     */
    private void initializeApplicationTreeView() {
        this.treeViewGames.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // bind the UI to the data
            final Application app = newValue.getValue();
            final Application oldApp = oldValue == null ? null : oldValue.getValue();
            this.currentApp = app;

            // LOG.debug(String.format("Selected '%s'", app.getApplicationName()));

            BindingHelper.bindProperty(app.applicationIdProperty(), this.lblApplicationId.textProperty());
            BindingHelper.bindProperty(app.applicationSizeProperty().asString(), this.lblGameSize.textProperty());
            BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.compressedProperty(),
                    app.compressedProperty(), this.chkCompressed.selectedProperty());
            BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.applicationNameProperty(),
                    app.applicationNameProperty(), this.txtApplicationName.textProperty());
            BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.sortNameProperty(),
                    app.sortNameProperty(), this.txtApplicationSortName.textProperty());
            BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.releaseDateProperty(),
                    app.releaseDateProperty(), this.dateReleaseDate.valueProperty());
            BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.publisherProperty(),
                    app.publisherProperty(), this.txtPublisher.textProperty());
            BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.saveCountProperty(),
                    app.saveCountProperty(), this.spnSaveCount.getValueFactory().valueProperty());
            BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.commandLineProperty(),
                    app.commandLineProperty(), this.txtCommandLine.textProperty());

            this.radOnePlayer.setSelected(app.isSinglePlayer());
            this.radTwoPlayerNoSim.setSelected(app.getNonSimultaneousMultiplayer());
            this.radTwoPlayerSim.setSelected(app.isSimultaneousMultiplayer());

            // persist the item to the database and refresh the application view
            this.hibernateManager.persistEntity(oldApp);
            this.treeViewGames.refresh();
        });

        // populate with test data until we store real data persistently
        this.treeViewGames.setRoot(ApplicationTestData.getTestApplicationData());
    }

    /**
     * Initializes the save count spinner's value factory.
     */
    private void initializeSaveCountSpinner() {
        this.spnSaveCount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 4));
    }

    /**
     * Sets up the radio buttons to select player count for an app.
     */
    private void initializePlayerCountSelection() {
        final String userDataSinglePlayer = "single";
        final String userDataNoSimMultiplayer = "multi";
        final String userDataSimMultiplayer = "simul";
        this.maxPlayersToggleGroup = new ToggleGroup();

        this.radOnePlayer.setToggleGroup(this.maxPlayersToggleGroup);
        this.radOnePlayer.setUserData(userDataSinglePlayer);
        this.radTwoPlayerNoSim.setToggleGroup(this.maxPlayersToggleGroup);
        this.radTwoPlayerNoSim.setUserData(userDataNoSimMultiplayer);
        this.radTwoPlayerSim.setToggleGroup(this.maxPlayersToggleGroup);
        this.radTwoPlayerSim.setUserData(userDataSimMultiplayer);

        this.maxPlayersToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                final String userData = (String) newValue.getUserData();
                this.currentApp.setSinglePlayer(userData.equals(userDataSinglePlayer));
                this.currentApp.setNonSimultaneousMultiplayer(userData.equals(userDataNoSimMultiplayer));
                this.currentApp.setSimultaneousMultiplayer(userData.equals(userDataSimMultiplayer));
            }
        });
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
