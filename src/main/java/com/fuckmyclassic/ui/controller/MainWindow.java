package com.fuckmyclassic.ui.controller;

import com.fuckmyclassic.boot.KernelFlasher;
import com.fuckmyclassic.boot.MembootHelper;
import com.fuckmyclassic.hibernate.dao.impl.ConsoleDAO;
import com.fuckmyclassic.management.LibraryManager;
import com.fuckmyclassic.model.Console;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.network.NetworkManager;
import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.task.TaskProvider;
import com.fuckmyclassic.ui.component.UiPropertyContainer;
import com.fuckmyclassic.ui.util.BindingHelper;
import com.fuckmyclassic.ui.util.ImageResizer;
import com.fuckmyclassic.userconfig.PathConfiguration;
import com.fuckmyclassic.userconfig.UserConfiguration;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
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

import javax.imageio.ImageIO;
import javax.usb.UsbException;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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

    public static final String RESOURCE_BUNDLE_PATH = "i18n/MainWindow";
    private static final String SYNC_TASK_TITLE_KEY = "SyncTaskLabel";
    private static final String ON_CONSOLE_SWITCH_MESSAGE_KEY = "SwitchConsoleTaskLabel";
    private static final String CONNECTED_GAMES_LABEL_KEY = "MainWindow.lblNumGamesSelected";
    private static final String SAVE_SCREENSHOT_DIALOG_TITLE_KEY = "TakeScreenshotTask.saveFileDialogTitle";
    private static final String SPACE_DIALOG_TITLE_KEY = "MainWindow.spaceDialogTitle";
    private static final String SPACE_DIALOG_HEADER_KEY = "MainWindow.spaceDialogHeader";
    private static final String SPACE_DIALOG_CONTENT_KEY = "MainWindow.spaceDialogContent";

    // References to all of the UI objects that we need to manipulate
    public ComboBox<Console> cmbCurrentConsole;
    public ComboBox<Library> cmbCurrentLibrary;
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
    public MenuItem mnuTakeScreenshot;
    public Label lblSizeOfLibrary;
    public ProgressBar prgFreeSpace;
    public Label lblFreeSpace;
    public TitledPane paneGameOptions;
    public TitledPane paneFolderOptions;

    /** The configuration object for user options and session settings */
    private final UserConfiguration userConfiguration;
    /** Path configuration for runtime operations */
    private final PathConfiguration pathConfiguration;
    /** Helper instance for membooting consoles */
    private final MembootHelper membootHelper;
    /** Helper instance for kernel flashing operations */
    private final KernelFlasher kernelFlasher;
    /** Helper instance for managing the library loading and view */
    private final LibraryManager libraryManager;
    /** Manager for SSH operations and network connections */
    private final NetworkManager networkManager;
    /** Resource bundle for internationalized task strings */
    private final ResourceBundle tasksResourceBundle;
    /** Resource bundle for the MainWindow strings */
    private final ResourceBundle mainResourceBundle;
    /** The dialog to run single tasks */
    private final SingleTaskRunnerDialog singleTaskRunnerDialog;
    /** The dialog to run sequential tasks */
    private final SequentialTaskRunnerDialog sequentialTaskRunnerDialog;
    /** Rsync runner dialog for game syncing */
    private final RsyncRunnerDialog rsyncRunnerDialog;
    /** Provider for the Tasks we need during runtime for miscellaneous operations */
    private final TaskProvider taskProvider;
    /** Container for UI properties we need to update */
    private final UiPropertyContainer uiPropertyContainer;
    /** The DAO for accessing known consoles */
    private final ConsoleDAO consoleDAO;
    /** Window for managing library and console metadata */
    private final LibraryManagementWindow libraryManagementWindow;
    /** Says whether or not we've already initialized this window once before */
    public boolean initialized;
    /** Says whether or not the console dropdown listener should respond to events */
    public boolean shouldConsoleListenerRespond;

    /**
     * Constructor.
     */
    @Autowired
    public MainWindow(final UserConfiguration userConfiguration,
                      final PathConfiguration pathConfiguration,
                      final MembootHelper membootHelper,
                      final KernelFlasher kernelFlasher,
                      final LibraryManager libraryManager,
                      final NetworkManager networkManager,
                      final ResourceBundle tasksResourceBundle,
                      final SingleTaskRunnerDialog singleTaskRunnerDialog,
                      final SequentialTaskRunnerDialog sequentialTaskRunnerDialog,
                      final RsyncRunnerDialog rsyncRunnerDialog,
                      final TaskProvider taskProvider,
                      final UiPropertyContainer uiPropertyContainer,
                      final ConsoleDAO consoleDAO,
                      final LibraryManagementWindow libraryManagementWindow) {
        this.userConfiguration = userConfiguration;
        this.pathConfiguration = pathConfiguration;
        this.membootHelper = membootHelper;
        this.kernelFlasher = kernelFlasher;
        this.libraryManager = libraryManager;
        this.networkManager = networkManager;
        this.tasksResourceBundle = tasksResourceBundle;
        this.singleTaskRunnerDialog = singleTaskRunnerDialog;
        this.sequentialTaskRunnerDialog = sequentialTaskRunnerDialog;
        this.rsyncRunnerDialog = rsyncRunnerDialog;
        this.taskProvider = taskProvider;
        this.uiPropertyContainer = uiPropertyContainer;
        this.consoleDAO = consoleDAO;
        this.libraryManagementWindow = libraryManagementWindow;
        this.initialized = false;
        this.shouldConsoleListenerRespond = true;
        this.mainResourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_PATH);
    }

    /**
     * Initialize the main window of the application.
     */
    @FXML
    public void initialize() {
        LOG.info("Main window initializing");

        if (!initialized) {
            this.initializeSaveCountSpinner();
            this.initializeBoxartImageView();
            this.initializePlayerCountSelection();
            this.initializeConnectionBoundProperties();
            this.initializeMenuBar();
            this.initializeNumSelectedLabel();
            this.initializeConsoleSelection();
            this.initializeGameSpaceProgressBar();
            this.libraryManager.initializeLibrarySelection(this);
            this.libraryManager.initializeApplicationTreeView(this);

            // small hack to remove a circular dependency in the Spring dependency graph
            this.taskProvider.loadLibrariesTask.setMainWindow(this);
            this.initialized = true;
        }

        // once all the listeners, etc. are initialized, load the console
        // and library data
        final List<Console> consoles = this.consoleDAO.getAllConsoles();
        final ObservableList<Console> items = FXCollections.observableArrayList(consoles);
        this.userConfiguration.setSelectedConsole(consoles.get(0));
        this.cmbCurrentConsole.setItems(items);
        this.cmbCurrentConsole.getSelectionModel().selectFirst();
        this.treeViewGames.getSelectionModel().selectFirst();
    }

    /**
     * Initialize the combo box for console selection.
     */
    private void initializeConsoleSelection() {
        if (!this.initialized) {
            this.cmbCurrentConsole.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    if (this.shouldConsoleListenerRespond) {
                        try {
                            this.userConfiguration.setSelectedConsole(newValue);

                            this.sequentialTaskRunnerDialog.setMainTaskMessage(this.tasksResourceBundle.getString(ON_CONSOLE_SWITCH_MESSAGE_KEY));
                            this.sequentialTaskRunnerDialog.setTaskCreators(taskProvider.loadLibrariesTask);
                            this.sequentialTaskRunnerDialog.showDialog();

                            this.uiPropertyContainer.setConnectedProperties(
                                    this.networkManager.isConnected(newValue.getLastKnownAddress()));
                        } catch (IOException e) {
                            LOG.error(e);
                        }
                    }
                }
            });
        }
    }

    /**
     * Initializes the save count spinner's value factory.
     */
    private void initializeSaveCountSpinner() {
        if (!this.initialized) {
            LOG.debug("Initializing the spinner for save count");

            this.spnSaveCount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 4));
        }
    }

    /**
     * Initialized the ImageView that shows the boxart preview.
     */
    private void initializeBoxartImageView() {
        if (!this.initialized) {
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
    }

    /**
     * Sets up the radio buttons to select player count for an app.
     */
    private void initializePlayerCountSelection() {
        if (!this.initialized) {
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
    }

    /**
     * Bind all the properties that are disabled/enabled depending on the presence on a console
     */
    private void initializeConnectionBoundProperties() {
        if (!this.initialized) {
            BindingHelper.bindProperty(this.uiPropertyContainer.connectionStatusColor,
                    this.shpConnectionStatus.fillProperty());
            BindingHelper.bindProperty((ReadOnlyProperty<?>) this.uiPropertyContainer.selectedConsoleConnectionStatus,
                    this.lblConnectionStatus.textProperty());
            BindingHelper.bindProperty(this.uiPropertyContainer.selectedConsoleDisconnected,
                    this.btnSyncGames.disableProperty());
            BindingHelper.bindProperty(this.uiPropertyContainer.selectedConsoleDisconnected,
                    this.mnuTakeScreenshot.disableProperty());
        }
    }

    /**
     * Initializes the main menu bar.
     */
    private void initializeMenuBar() {
        if (!this.initialized) {
            if (System.getProperty("os.name", "UNKNOWN").equals("Mac OS X")) {
                this.mainMenu.setUseSystemMenuBar(true);
            }
        }
    }

    /**
     * Initializes the label that shows the number of selected games.
     */
    private void initializeNumSelectedLabel() {
        if (!this.initialized) {
            BindingHelper.bindProperty(Bindings.format(this.mainResourceBundle.getString(CONNECTED_GAMES_LABEL_KEY),
                    this.uiPropertyContainer.numSelected), this.lblNumGamesSelected.textProperty());
        }
    }

    /**
     * Initializes the progress bar that shows the space used / available for games
     */
    private void initializeGameSpaceProgressBar() {
        if (!this.initialized) {
            BindingHelper.bindProperty(this.uiPropertyContainer.gameSpaceUsed, this.prgFreeSpace.progressProperty());
            BindingHelper.bindProperty((StringExpression) this.uiPropertyContainer.progressBarStyle, this.prgFreeSpace.styleProperty());
        }
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
     * Handler for clicking the button to sync games to the connected console (internal sync).
     * @throws IOException
     */
    @FXML
    private void onSyncGamesClicked() throws IOException {
        LOG.info("Attempting to sync games to the console's internal memory");

        // setup the temp data task
        if (this.libraryManager.getCurrentLibraryTree().getValue().getTreeFilesize() <=
                this.userConfiguration.getSelectedConsole().getSpaceForGames()) {
            final Console selectedConsole = this.userConfiguration.getSelectedConsole();
            final String syncPath = String.format("%s/%s/%s", selectedConsole.getConsoleSyncPath(),
                    selectedConsole.getConsoleType().getConsoleCode(), PathConfiguration.CONSOLE_STORAGE_DIR);
            this.taskProvider.createTempDataTask.setSyncPath(syncPath);

            // run the pre-sync tasks
            sequentialTaskRunnerDialog.setMainTaskMessage(this.tasksResourceBundle.getString(SYNC_TASK_TITLE_KEY));
            sequentialTaskRunnerDialog.setTaskCreators(taskProvider.createTempDataTask, taskProvider.showSplashScreenAndStopUiTask,
                    taskProvider.unmountGamesTask);
            sequentialTaskRunnerDialog.showDialog();

            // run the rsync task
            rsyncRunnerDialog.setLocalPath(this.pathConfiguration.tempDirectory + File.separator);
            rsyncRunnerDialog.setRemotePath(String.format("%s/%s/",
                    selectedConsole.getConsoleSyncPath(), selectedConsole.getConsoleType().getConsoleCode()));
            rsyncRunnerDialog.setUpload(true);
            rsyncRunnerDialog.showDialog();

            // start the console's UI back up
            sequentialTaskRunnerDialog.setMainTaskMessage(this.tasksResourceBundle.getString(SYNC_TASK_TITLE_KEY));
            sequentialTaskRunnerDialog.setTaskCreators(taskProvider.mountGamesAndStartUiTask);
            sequentialTaskRunnerDialog.showDialog();
        } else {
            final Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(this.mainResourceBundle.getString(SPACE_DIALOG_TITLE_KEY));
            alert.setHeaderText(this.mainResourceBundle.getString(SPACE_DIALOG_HEADER_KEY));
            alert.setContentText(this.mainResourceBundle.getString(SPACE_DIALOG_CONTENT_KEY));

            alert.showAndWait();
        }
    }

    /**
     * Handler for clicking the button to manage games and consoles.
     */
    @FXML
    private void onManageLibrariesAndConsolesClicked() throws IOException {
        this.libraryManagementWindow.showWindow();
        this.initialize();
    }

    /**
     * Event handler for the menu item to take a screenshot
     */
    @FXML
    private void onTakeScreenshotClicked() throws IOException {
        this.singleTaskRunnerDialog.setTaskCreator(this.taskProvider.takeScreenshotTask);
        this.singleTaskRunnerDialog.setTitle(this.tasksResourceBundle.getString(SAVE_SCREENSHOT_DIALOG_TITLE_KEY));
        this.singleTaskRunnerDialog.showDialog();

        if (this.userConfiguration.getLastScreenshot() != null) {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(this.tasksResourceBundle.getString(SAVE_SCREENSHOT_DIALOG_TITLE_KEY));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));

            final File outputFile = fileChooser.showSaveDialog(this.treeViewGames.getScene().getWindow());
            if (outputFile != null) {
                try {
                    ImageIO.write(this.userConfiguration.getLastScreenshot(), "png", outputFile);
                    this.userConfiguration.setLastScreenshot(null);
                    Desktop.getDesktop().open(outputFile);
                } catch (IOException e) {
                    LOG.error(e);
                    throw e;
                }
            }
        }
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
