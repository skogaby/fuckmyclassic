package com.fuckmyclassic.ui.controller;

import com.fuckmyclassic.boot.KernelFlasher;
import com.fuckmyclassic.boot.MembootHelper;
import com.fuckmyclassic.management.LibraryManager;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.network.NetworkConnection;
import com.fuckmyclassic.network.NetworkConstants;
import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.task.TaskProvider;
import com.fuckmyclassic.ui.component.UiPropertyContainer;
import com.fuckmyclassic.ui.util.BindingHelper;
import com.fuckmyclassic.ui.util.ImageResizer;
import com.fuckmyclassic.userconfig.ConsoleConfiguration;
import com.fuckmyclassic.userconfig.UserConfiguration;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyProperty;
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
import java.util.ResourceBundle;

import static com.fuckmyclassic.boot.KernelFlasher.BOOT_IMG_PATH;

/**
 * Controller for the main application window.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class MainWindow {

    static Logger LOG = LogManager.getLogger(MainWindow.class.getName());

    private static final String RESOURCE_BUNDLE_PATH = "i18n/MainWindow";
    private static final String SYNC_TASK_TITLE_KEY = "SyncTaskLabel";
    private static final String CONNECTED_GAMES_LABEL_KEY = "MainWindow.lblNumGamesSelected";

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
    public Label lblNumGamesSelected;

    /** The configuration object for user options and session settings */
    private final UserConfiguration userConfiguration;
    /** Configuration about the connected console */
    private final ConsoleConfiguration consoleConfiguration;
    /** Helper instance for membooting consoles */
    private final MembootHelper membootHelper;
    /** Helper instance for kernel flashing operations */
    private final KernelFlasher kernelFlasher;
    /** Helper instance for managing the library loading and view */
    private final LibraryManager libraryManager;
    /** Manager for SSH operations and network connections */
    private final NetworkConnection networkConnection;
    /** Resource bundle for internationalized task strings */
    private final ResourceBundle tasksResourceBundle;
    /** The dialog to run sequential tasks */
    private final SequentialTaskRunnerDialog sequentialTaskRunnerDialog;
    /** Rsync runner dialog for game syncing */
    private final RsyncRunnerDialog rsyncRunnerDialog;
    /** Provider for the Tasks we need during runtime for miscellaneous operations */
    private final TaskProvider taskProvider;
    /** Container for UI properties we need to update */
    private final UiPropertyContainer uiPropertyContainer;

    /**
     * Constructor.
     */
    @Autowired
    public MainWindow(final UserConfiguration userConfiguration,
                      final ConsoleConfiguration consoleConfiguration,
                      final MembootHelper membootHelper,
                      final KernelFlasher kernelFlasher,
                      final LibraryManager libraryManager,
                      final NetworkConnection networkConnection,
                      final ResourceBundle tasksResourceBundle,
                      final SequentialTaskRunnerDialog sequentialTaskRunnerDialog,
                      final RsyncRunnerDialog rsyncRunnerDialog,
                      final TaskProvider taskProvider,
                      final UiPropertyContainer uiPropertyContainer) {
        this.userConfiguration = userConfiguration;
        this.consoleConfiguration = consoleConfiguration;
        this.membootHelper = membootHelper;
        this.kernelFlasher = kernelFlasher;
        this.libraryManager = libraryManager;
        this.networkConnection = networkConnection;
        this.tasksResourceBundle = tasksResourceBundle;
        this.sequentialTaskRunnerDialog = sequentialTaskRunnerDialog;
        this.rsyncRunnerDialog = rsyncRunnerDialog;
        this.taskProvider = taskProvider;
        this.uiPropertyContainer = uiPropertyContainer;
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
        this.initializeConnectionBoundProperties();
        this.initializeMenuBar();
        this.initializeNumSelectedLabel();

        // small hack to remove a circular dependency in the Spring dependency graph
        this.taskProvider.loadLibrariesTask.setMainWindow(this);
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
     * Bind all the properties that are disabled/enabled depending on the presence on a console
     */
    private void initializeConnectionBoundProperties() {
        BindingHelper.bindProperty(this.uiPropertyContainer.connectionStatusColor,
                this.shpConnectionStatus.fillProperty());
        BindingHelper.bindProperty((ReadOnlyProperty<?>) this.uiPropertyContainer.connectionStatus,
                this.lblConnectionStatus.textProperty());
        BindingHelper.bindProperty(this.uiPropertyContainer.disconnected,
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

    /**
     * Initializes the label that shows the number of selected games.
     */
    private void initializeNumSelectedLabel() {
        final ResourceBundle resources = ResourceBundle.getBundle(RESOURCE_BUNDLE_PATH);
        BindingHelper.bindProperty(Bindings.format(resources.getString(CONNECTED_GAMES_LABEL_KEY),
                this.uiPropertyContainer.numSelected), this.lblNumGamesSelected.textProperty());
    }

    /**
     * Handler for clicking the button to sync games to the connected console (internal sync).
     * @throws IOException
     */
    @FXML
    private void onSyncGamesClicked() throws IOException {
        LOG.info("Attempting to sync games to the console's internal memory");

        // setup the temp data task
        final String syncPath = String.format("%s/%s/%s", this.consoleConfiguration.getSystemSyncPath(),
                this.consoleConfiguration.getSystemType(), SharedConstants.CONSOLE_STORAGE_DIR);
        this.taskProvider.createTempDataTask.setSyncPath(syncPath);

        // run the pre-sync tasks
        sequentialTaskRunnerDialog.setMainTaskMessage(this.tasksResourceBundle.getString(SYNC_TASK_TITLE_KEY));
        sequentialTaskRunnerDialog.setTaskCreators(taskProvider.createTempDataTask, taskProvider.showSplashScreenAndStopUiTask,
                taskProvider.unmountGamesTask);
        sequentialTaskRunnerDialog.showDialog();

        // setup the rsync task
        rsyncRunnerDialog.setSource(Paths.get(SharedConstants.TEMP_DIRECTORY).toString() + "/");
        rsyncRunnerDialog.setDestination(String.format(
                "%s@%s:%s/%s/", NetworkConstants.USER_NAME, NetworkConstants.CONSOLE_IP,
                consoleConfiguration.getSystemSyncPath(), consoleConfiguration.getSystemType()));
        rsyncRunnerDialog.showDialog();

        // start the UI back up
        sequentialTaskRunnerDialog.setTaskCreators(taskProvider.mountGamesAndStartUiTask);
        sequentialTaskRunnerDialog.showDialog();
    }

    //////////////////////////////////////////////////////////////////////
    //     Stubbed out methods for testing half-baked functionality     //
    //////////////////////////////////////////////////////////////////////

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
