package com.fuckmyclassic.controller;

import com.fuckmyclassic.boot.KernelFlasher;
import com.fuckmyclassic.boot.MembootHelper;
import com.fuckmyclassic.hibernate.ApplicationDAO;
import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.LibraryDAO;
import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.testdata.ApplicationTestData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.usb.UsbException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.fuckmyclassic.boot.KernelFlasher.BOOT_IMG_PATH;

/**
 * Controller for the main application window.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class MainWindow {

    static Logger LOG = LogManager.getLogger(MainWindow.class.getName());

    // References to all of the UI objects that we need to manipulate
    @FXML private ComboBox<Library> cmbCurrentCollection;
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
     * DAO for handling Applications.
     */
    private final ApplicationDAO applicationDAO;

    /**
     * DAO for library metadata.
     */
    private final LibraryDAO libraryDAO;

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
     * The SID for the console whose collection we're viewing.
     */
    private String currentConsoleSid;

    /**
     * The current library we're viewing.
     */
    private Library currentLibrary;

    /**
     * Constructor.
     * @param hibernateManager
     * @param applicationDAO
     * @param libraryDAO
     * @param membootHelper
     * @param kernelFlasher
     */
    @Autowired
    public MainWindow(final HibernateManager hibernateManager,
                      final ApplicationDAO applicationDAO,
                      final LibraryDAO libraryDAO,
                      final MembootHelper membootHelper,
                      final KernelFlasher kernelFlasher) {
        this.hibernateManager = hibernateManager;
        this.applicationDAO = applicationDAO;
        this.libraryDAO = libraryDAO;
        this.membootHelper = membootHelper;
        this.kernelFlasher = kernelFlasher;
        this.currentConsoleSid = SharedConstants.DEFAULT_CONSOLE_SID;
        this.currentLibrary = null;
    }

    /**
     * Initialize the main window of the application.
     */
    @FXML
    public void initialize() {
        LOG.info("Main window initializing");

        initializeLibrarySelection();
        initializeApplicationTreeView();
        initializeSaveCountSpinner();
        initializePlayerCountSelection();
    }

    /**
     * Sets up the dropdown for the library selection.
     */
    private void initializeLibrarySelection() {
        LOG.debug("Initializing the dropdown box for library selection");

        final List<Library> libraries = libraryDAO.getLibrariesForConsole(this.currentConsoleSid);
        final ObservableList<Library> items = FXCollections.observableArrayList(libraries);
        this.cmbCurrentCollection.setItems(items);
        this.cmbCurrentCollection.getSelectionModel().selectFirst();
        this.currentLibrary = items.get(0);
    }

    /**
     * Initializes the TreeView display for the applications and games.
     */
    private void initializeApplicationTreeView() {
        LOG.debug("Initializing the tree view for games");

        this.treeViewGames.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // bind the UI to the data
            final Application app = newValue.getValue();
            final Application oldApp = oldValue == null ? null : oldValue.getValue();
            this.currentApp = app;

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
            this.hibernateManager.updateEntity(oldApp);
            this.treeViewGames.refresh();
        });

        // load the library items for the current console and library
        LOG.info(String.format("Loading library for console %s from the database", this.currentConsoleSid));
        this.treeViewGames.setRoot(applicationDAO.loadLibraryForConsole(this.currentLibrary));
    }

    /**
     * Initializes the save count spinner's value factory.
     */
    private void initializeSaveCountSpinner() {
        LOG.debug("Initializing the spinner for save count");

        this.spnSaveCount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 4));
    }

    /**
     * Sets up the radio buttons to select player count for an app.
     */
    private void initializePlayerCountSelection() {
        LOG.debug("Initializing the radio buttons for player count selection");

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
