package com.fuckmyclassic.controller;

import com.fuckmyclassic.boot.KernelFlasher;
import com.fuckmyclassic.boot.MembootHelper;
import com.fuckmyclassic.controller.util.LibraryManager;
import com.fuckmyclassic.hibernate.ApplicationDAO;
import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Library;
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

import static com.fuckmyclassic.boot.KernelFlasher.BOOT_IMG_PATH;

/**
 * Controller for the main application window.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class MainWindow {

    static Logger LOG = LogManager.getLogger(MainWindow.class.getName());

    // References to all of the UI objects that we need to manipulate
    public ComboBox<Library> cmbCurrentCollection;
    public TreeView<Application> treeViewGames;
    public Label lblApplicationId;
    public Label lblGameSize;
    public CheckBox chkCompressed;
    public TextField txtApplicationName;
    public TextField txtApplicationSortName;
    public ToggleGroup maxPlayersToggleGroup;
    public RadioButton radOnePlayer;
    public RadioButton radTwoPlayerNoSim;
    public RadioButton radTwoPlayerSim;
    public DatePicker dateReleaseDate;
    public TextField txtPublisher;
    public Spinner spnSaveCount;
    public TextField txtCommandLine;
    public TextField txtGameGenieCodes;
    public ImageView imgBoxArtPreview;

    /**
     * Hibernate manager, for interacting with the database.
     */
    private final HibernateManager hibernateManager;

    /**
     * DAO for handling Applications.
     */
    private final ApplicationDAO applicationDAO;

    /**
     * Helper instance for membooting consoles.
     */
    private final MembootHelper membootHelper;

    /**
     * Helper instance for kernel flashing operations.
     */
    private final KernelFlasher kernelFlasher;

    /**
     * Helper instance for managing the library loading and view.
     */
    private final LibraryManager libraryManager;

    /**
     * Constructor.
     * @param hibernateManager
     * @param applicationDAO
     * @param membootHelper
     * @param kernelFlasher
     */
    @Autowired
    public MainWindow(final HibernateManager hibernateManager,
                      final ApplicationDAO applicationDAO,
                      final MembootHelper membootHelper,
                      final KernelFlasher kernelFlasher,
                      final LibraryManager libraryManager) {
        this.hibernateManager = hibernateManager;
        this.applicationDAO = applicationDAO;
        this.membootHelper = membootHelper;
        this.kernelFlasher = kernelFlasher;
        this.libraryManager = libraryManager;
    }

    /**
     * Initialize the main window of the application.
     */
    @FXML
    public void initialize() {
        LOG.info("Main window initializing");

        this.libraryManager.initializeLibrarySelection(this);
        this.libraryManager.initializeApplicationTreeView(this);
        initializeSaveCountSpinner();
        initializePlayerCountSelection();
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
                this.libraryManager.getCurrentApp().setSinglePlayer(userData.equals(userDataSinglePlayer));
                this.libraryManager.getCurrentApp().setNonSimultaneousMultiplayer(userData.equals(userDataNoSimMultiplayer));
                this.libraryManager.getCurrentApp().setSimultaneousMultiplayer(userData.equals(userDataSimMultiplayer));
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
