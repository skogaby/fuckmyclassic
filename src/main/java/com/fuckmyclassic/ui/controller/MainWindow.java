package com.fuckmyclassic.ui.controller;

import com.fuckmyclassic.boot.KernelFlasher;
import com.fuckmyclassic.boot.MembootHelper;
import com.fuckmyclassic.management.LibraryManager;
import com.fuckmyclassic.hibernate.ApplicationDAO;
import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.ui.util.ImageResizer;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.usb.UsbException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
     * Helper to resize boxart images when we import them.
     */
    private final ImageResizer imageResizer;

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
                      final LibraryManager libraryManager,
                      final ImageResizer imageResizer) {
        this.hibernateManager = hibernateManager;
        this.applicationDAO = applicationDAO;
        this.membootHelper = membootHelper;
        this.kernelFlasher = kernelFlasher;
        this.libraryManager = libraryManager;
        this.imageResizer = imageResizer;
    }

    /**
     * Initialize the main window of the application.
     */
    @FXML
    public void initialize() {
        LOG.info("Main window initializing");

        initializeSaveCountSpinner();
        this.libraryManager.initializeLibrarySelection(this);
        this.libraryManager.initializeApplicationTreeView(this);
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

    /**
     * Event handler for the button to browse for boxart.
     */
    @FXML
    private void onBrowseForBoxArtClicked() throws IOException {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
        final File boxArt = fileChooser.showOpenDialog(this.treeViewGames.getScene().getWindow());

        // if they selected a file, copy it to the boxart directory and replace whatever was there before
        if (boxArt != null) {
            // we need to read the selected image into a buffer, resize it to our desired dimensions,
            // then save the 2 new copies in our boxart directory.
            final Application currentApp = this.libraryManager.getCurrentApp();
            LOG.debug(String.format("Selected '%s' as boxart for '%s'", boxArt.getName(), currentApp.getApplicationName()));

            final String newBoxartFile = String.format("%s.png", currentApp.getApplicationId());
            final String newThumbnailFile = String.format("%s_small.png", currentApp.getApplicationId());

            // first, do the main boxart
            BufferedImage inputImage = ImageIO.read(boxArt);
            BufferedImage resizedImage = this.imageResizer.resizeProportionally(inputImage,
                    SharedConstants.BOXART_SIZE, SharedConstants.BOXART_SIZE);
            File outputFile = new File(Paths.get(SharedConstants.BOXART_DIRECTORY, newBoxartFile).toUri());
            ImageIO.write(resizedImage, "png", outputFile);

            // now, do the thumbnail
            resizedImage = this.imageResizer.resizeProportionally(inputImage,
                    SharedConstants.THUMBNAIL_SIZE, SharedConstants.THUMBNAIL_SIZE);
            outputFile = new File(Paths.get(SharedConstants.BOXART_DIRECTORY, newThumbnailFile).toUri());
            ImageIO.write(resizedImage, "png", outputFile);

            // also update the Application itself and refresh the view
            currentApp.setBoxArtPath(newBoxartFile);
            this.hibernateManager.updateEntity(currentApp);
            this.imgBoxArtPreview.setImage(new Image(
                    Paths.get("file:" + SharedConstants.BOXART_DIRECTORY, newBoxartFile).toString()));
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
