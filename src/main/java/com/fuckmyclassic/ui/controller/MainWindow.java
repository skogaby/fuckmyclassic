package com.fuckmyclassic.ui.controller;

import com.fuckmyclassic.boot.KernelFlasher;
import com.fuckmyclassic.boot.MembootHelper;
import com.fuckmyclassic.management.LibraryManager;
import com.fuckmyclassic.hibernate.ApplicationDAO;
import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.network.NetworkConnection;
import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.ui.util.BindingHelper;
import com.fuckmyclassic.ui.util.ImageResizer;
import com.fuckmyclassic.userconfig.UserConfiguration;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.usb.UsbException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

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
    public TreeView<LibraryItem> treeViewGames;
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
    public Circle shpConnectionStatus;
    public Label lblConnectionStatus;
    public Button btnSyncGames;
    public MenuBar mainMenu;

    /**
     * The configuration object for user options and session settings.
     */
    private final UserConfiguration userConfiguration;

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
     * Manager for SSH operations and network connections.
     */
    private final NetworkConnection networkConnection;

    /**
     * Constructor.
     * @param membootHelper
     * @param kernelFlasher
     */
    @Autowired
    public MainWindow(final UserConfiguration userConfiguration,
                      final MembootHelper membootHelper,
                      final KernelFlasher kernelFlasher,
                      final LibraryManager libraryManager,
                      final NetworkConnection networkConnection) {
        this.userConfiguration = userConfiguration;
        this.membootHelper = membootHelper;
        this.kernelFlasher = kernelFlasher;
        this.libraryManager = libraryManager;
        this.networkConnection = networkConnection;
        this.networkConnection.addConnectionListener(() -> LOG.info("Console connected"));
        this.networkConnection.beginPolling();
    }

    /**
     * Initialize the main window of the application.
     */
    @FXML
    public void initialize() {
        LOG.info("Main window initializing");

        this.initializeSaveCountSpinner();
        this.initializeBoxartImageView();
        this.libraryManager.initializeLibrarySelection(this);
        this.libraryManager.initializeApplicationTreeView(this);
        this.initializePlayerCountSelection();
        this.initializeConnectionStatus();
        this.initializeConnectionBoundProperties();
        this.initializeMenuBar();
    }

    /**
     * Initializes the save count spinner's value factory.
     */
    private void initializeSaveCountSpinner() {
        LOG.debug("Initializing the spinner for save count");

        this.spnSaveCount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 4));
    }

    /**
     * Initialized the ImageView that shows the boxart preview.
     */
    private void initializeBoxartImageView() {
        this.imgBoxArtPreview.setFitWidth(SharedConstants.BOXART_SIZE);
        this.imgBoxArtPreview.setPreserveRatio(true);
        this.imgBoxArtPreview.setSmooth(true);
        this.imgBoxArtPreview.setCache(true);

        // enable drag 'n' drop to change the boxart image
        this.imgBoxArtPreview.setOnDragOver(event -> {
            final Dragboard db = event.getDragboard();

            if (db.hasFiles() && db.getFiles().size() == 1) {
                final String filename = db.getFiles().get(0).getName().toLowerCase(Locale.getDefault());

                // make sure it's an image file
                if (ImageResizer.isImageFile(filename)) {
                    event.acceptTransferModes(TransferMode.COPY);
                }
            }

            event.consume();
        });

        this.imgBoxArtPreview.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles() && db.getFiles().size() == 1) {
                final File boxart = db.getFiles().get(0);

                // make sure it's an image file
                if (ImageResizer.isImageFile(boxart.getName())) {
                    try {
                        final Image previewImage = this.libraryManager.importBoxartForCurrentApp(boxart);
                        this.imgBoxArtPreview.setImage(previewImage);
                        success = true;
                    } catch (IOException e) {
                        LOG.error("Unable to import new boxart for the current app", e);
                        success = false;
                    }
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
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

    /**
     * Bind the connection status display to the real connection object.
     */
    private void initializeConnectionStatus() {
        BindingHelper.bindProperty(this.networkConnection.connectionStatusColorProperty(),
                this.shpConnectionStatus.fillProperty());
        BindingHelper.bindProperty(this.networkConnection.connectionStatusProperty(),
                this.lblConnectionStatus.textProperty());
    }

    /**
     * Bind all the properties that are disabled/enabled depending on the presence on a console
     */
    private void initializeConnectionBoundProperties() {
        BindingHelper.bindProperty(this.networkConnection.disconnectedProperty(),
                this.btnSyncGames.disableProperty());
    }

    /**
     * Event handler for the button to browse for boxart.
     */
    @FXML
    private void onBrowseForBoxArtClicked() throws IOException {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Image files", "*.png", "*.bmp", "*.jpg"));
        final File boxartFile = fileChooser.showOpenDialog(this.treeViewGames.getScene().getWindow());

        if (boxartFile != null && ImageResizer.isImageFile(boxartFile.getName())) {
            final Image previewImage = this.libraryManager.importBoxartForCurrentApp(
                    fileChooser.showOpenDialog(this.treeViewGames.getScene().getWindow()));
            this.imgBoxArtPreview.setImage(previewImage);
        }
    }

    /**
     * Initializes the main menu bar.
     */
    private void initializeMenuBar() {
        if (System.getProperty("os.name", "UNKNOWN").equals("Mac OS X")) {
            this.mainMenu.setUseSystemMenuBar(true);
        }
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
