package com.fuckmyclassic.management;

import com.fuckmyclassic.hibernate.dao.impl.ApplicationDAO;
import com.fuckmyclassic.hibernate.dao.impl.LibraryItemDAO;
import com.fuckmyclassic.model.Console;
import com.fuckmyclassic.model.Folder;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.model.OriginalGame;
import com.fuckmyclassic.ui.component.UiPropertyContainer;
import com.fuckmyclassic.ui.controller.MainWindow;
import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.ui.component.ApplicationTreeCell;
import com.fuckmyclassic.ui.util.BindingHelper;
import com.fuckmyclassic.ui.util.ImageResizer;
import com.fuckmyclassic.userconfig.PathConfiguration;
import com.fuckmyclassic.userconfig.UserConfiguration;
import com.fuckmyclassic.util.FileUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.ResourceBundle;

/**
 * Class to manage libraries. This includes keeping track of what the current console and library
 * is, as well as the logic to initialize the library (application) TreeView.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class LibraryManager {

    static Logger LOG = LogManager.getLogger(LibraryManager.class.getName());

    private static final String LIBRARY_SIZE_LABEL_KEY = "MainWindow.lblSizeOfLibrary";

    /** User configuration object. */
    private final UserConfiguration userConfiguration;
    /** Path configuration for runtime operations */
    private final PathConfiguration pathConfiguration;
    /** DAO for application metadata */
    private final ApplicationDAO applicationDAO;
    /** DAO for library item metadata */
    private final LibraryItemDAO libraryItemDAO;
    /** For resizing boxart images we import. */
    private final ImageResizer imageResizer;
    /** The currently selected application in the TreeView */
    private Application currentApp;
    /** The current library we're viewing */
    private Library currentLibrary;
    /** A reference to the current library's actual tree structure data. */
    private TreeItem<LibraryItem> currentLibraryTree;
    /** Container for UI properties we need to update */
    private final UiPropertyContainer uiPropertyContainer;

    @Autowired
    public LibraryManager(final UserConfiguration userConfiguration,
                          final PathConfiguration pathConfiguration,
                          final ApplicationDAO applicationDAO,
                          final LibraryItemDAO libraryItemDAO,
                          final ImageResizer imageResizer,
                          final UiPropertyContainer uiPropertyContainer) {
        this.userConfiguration = userConfiguration;
        this.pathConfiguration = pathConfiguration;
        this.applicationDAO = applicationDAO;
        this.libraryItemDAO = libraryItemDAO;
        this.imageResizer = imageResizer;
        this.currentApp = null;
        this.currentLibrary = null;
        this.uiPropertyContainer = uiPropertyContainer;
    }

    /**
     * Sets up the dropdown for the library selection.
     */
    public void initializeLibrarySelection(final MainWindow mainWindow) {
        if (!mainWindow.initialized) {
            LOG.debug("Initializing the dropdown box for library selection");

            mainWindow.cmbCurrentLibrary.valueProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    final Console selectedConsole = this.userConfiguration.getSelectedConsole();
                    this.currentLibrary = newValue;
                    this.currentLibraryTree = this.libraryItemDAO.getApplicationTreeForLibrary(this.currentLibrary, true, false);
                    mainWindow.treeViewGames.setRoot(this.currentLibraryTree);
                    mainWindow.treeViewGames.getSelectionModel().selectFirst();
                    this.userConfiguration.setSelectedLibraryID(this.currentLibrary.getId());

                    BindingHelper.bindProperty(Bindings.format(
                            ResourceBundle.getBundle(MainWindow.RESOURCE_BUNDLE_PATH).getString(LIBRARY_SIZE_LABEL_KEY),
                            this.currentLibraryTree.getValue().treeFilesizeStringProperty()),
                            mainWindow.lblSizeOfLibrary.textProperty());

                    if (selectedConsole.getSpaceForGames() != 0) {
                        BindingHelper.bindProperty(Bindings.format("%s / %s", this.currentLibraryTree.getValue().treeFilesizeStringProperty(),
                                FileUtils.convertToHumanReadable(selectedConsole.getSpaceForGames())),
                                mainWindow.lblFreeSpace.textProperty());
                        this.uiPropertyContainer.gameSpaceUsed.setValue(
                                (double) this.currentLibraryTree.getValue().getTreeFilesize() /
                                (double) selectedConsole.getSpaceForGames());
                    } else {
                        BindingHelper.bindProperty(Bindings.format("%s / ??? MB",
                                this.currentLibraryTree.getValue().treeFilesizeStringProperty()),
                                mainWindow.lblFreeSpace.textProperty());
                        this.uiPropertyContainer.gameSpaceUsed.setValue(0);
                    }
                }
            }));
        }
    }

    /**
     * Initializes the TreeView display for the applications and games.
     */
    public void initializeApplicationTreeView(final MainWindow mainWindow) {
        if (!mainWindow.initialized) {
            LOG.debug("Initializing the tree view for games");

            // initialize the cell factory so we can control theming, drag and drop, etc.
            mainWindow.treeViewGames.setCellFactory(param ->
                    new ApplicationTreeCell(this.applicationDAO, this.libraryItemDAO, new AppImporter(
                            this.applicationDAO, this.libraryItemDAO, this, this.uiPropertyContainer,
                            this.pathConfiguration, this.userConfiguration), this.pathConfiguration));

            // whenever an item is selected, we'll bind the data to the UI and save whatever app
            // was being viewed previously to the database
            mainWindow.treeViewGames.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    final Application app = newValue.getValue().getApplication();
                    final Application oldApp = oldValue == null ? null : oldValue.getValue().getApplication();
                    this.currentApp = app;

                    final Property oldGameSizeProperty = (oldApp == null) ? null :
                                ((oldApp instanceof Folder) ? oldValue.getValue().treeFilesizeStringProperty() :
                                        oldValue.getValue().getApplication().applicationSizeStringProperty());

                    if (app instanceof Folder) {
                        BindingHelper.bindPropertyBidirectional(oldGameSizeProperty, newValue.getValue().treeFilesizeStringProperty(),
                                mainWindow.lblGameSize.textProperty());
                    } else {
                        BindingHelper.bindPropertyBidirectional(oldGameSizeProperty, app.applicationSizeStringProperty(),
                                mainWindow.lblGameSize.textProperty());
                    }

                    BindingHelper.bindProperty((ReadOnlyProperty<?>) app.applicationIdProperty(), mainWindow.lblApplicationId.textProperty());
                    BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.compressedProperty(),
                            app.compressedProperty(), mainWindow.chkCompressed.selectedProperty());
                    BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.applicationNameProperty(),
                            app.applicationNameProperty(), mainWindow.txtApplicationName.textProperty());
                    BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.sortNameProperty(),
                            app.sortNameProperty(), mainWindow.txtApplicationSortName.textProperty());
                    BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.releaseDateProperty(),
                            app.releaseDateProperty(), mainWindow.dateReleaseDate.valueProperty());
                    BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.publisherProperty(),
                            app.publisherProperty(), mainWindow.txtPublisher.textProperty());
                    BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.saveCountProperty(),
                            app.saveCountProperty(), mainWindow.spnSaveCount.getValueFactory().valueProperty());
                    BindingHelper.bindPropertyBidirectional(oldApp == null ? null : oldApp.commandLineProperty(),
                            app.commandLineProperty(), mainWindow.txtCommandLine.textProperty());

                    mainWindow.radOnePlayer.setSelected(app.isSinglePlayer());
                    mainWindow.radTwoPlayerNoSim.setSelected(app.getNonSimultaneousMultiplayer());
                    mainWindow.radTwoPlayerSim.setSelected(app.isSimultaneousMultiplayer());

                    // set the box art if there is any
                    try {
                        if (app instanceof OriginalGame &&
                                !((OriginalGame) app).isBoxartModified()) {
                            mainWindow.imgBoxArtPreview.setImage(new Image(
                                    Paths.get(pathConfiguration.originalGamesDirectory, ((OriginalGame) app).getConsoleType(),
                                            app.getApplicationId(), app.getBoxArtPath()).toUri().toURL().toExternalForm()));
                        } else {
                            if (!StringUtils.isEmpty(app.getBoxArtPath())) {
                                mainWindow.imgBoxArtPreview.setImage(new Image(
                                        Paths.get(pathConfiguration.gamesDirectory, app.getApplicationId(),
                                                app.getBoxArtPath()).toUri().toURL().toExternalForm()));
                            } else {
                                mainWindow.imgBoxArtPreview.setImage(new Image(Paths.get(
                                        PathConfiguration.IMAGES_DIRECTORY, SharedConstants.WARNING_IMAGE).toString()));
                            }
                        }
                    } catch (MalformedURLException e) {
                        LOG.error(e);
                    }

                    // persist the item to the database and refresh the application view
                    if (oldApp != null) {
                        this.applicationDAO.update(oldApp);
                    }

                    mainWindow.treeViewGames.refresh();

                    // test, make this integrate more cleanly with the above
                    if (app instanceof Folder) {
                        mainWindow.paneFolderOptions.toFront();
                    } else {
                        mainWindow.paneGameOptions.toFront();
                    }
                }
            });
        }
    }

    /**
     * Imports a new boxart file for the currently selected app.
     * @param boxArt The File containing the new boxart
     * @return An Image object to set for the boxart preview
     * @throws IOException
     */
    public Image importBoxartForCurrentApp(final File boxArt) throws IOException {
        if (boxArt != null) {
            // we need to read the selected image into a buffer, resize it to our desired dimensions,
            // then save the 2 new copies in our boxart directory.
            LOG.debug(String.format("Selected '%s' as boxart for '%s'", boxArt.getName(), this.currentApp.getApplicationName()));

            final String newBoxartFile = String.format("%s.png", this.currentApp.getApplicationId());
            final String newThumbnailFile = String.format("%s_small.png", this.currentApp.getApplicationId());

            // first, do the main boxart
            BufferedImage inputImage = ImageIO.read(boxArt);
            BufferedImage resizedImage = this.imageResizer.resizeProportionally(inputImage,
                    SharedConstants.BOXART_SIZE, SharedConstants.BOXART_SIZE);
            File outputFile = new File(Paths.get(this.pathConfiguration.gamesDirectory, this.currentApp.getApplicationId(),
                    newBoxartFile).toUri());
            ImageIO.write(resizedImage, "png", outputFile);

            // now, do the thumbnail
            resizedImage = this.imageResizer.resizeProportionally(inputImage,
                    SharedConstants.THUMBNAIL_SIZE, SharedConstants.THUMBNAIL_SIZE);
            outputFile = new File(Paths.get(this.pathConfiguration.gamesDirectory, this.currentApp.getApplicationId(), newThumbnailFile).toUri());
            ImageIO.write(resizedImage, "png", outputFile);

            // also update the Application itself
            if (this.currentApp instanceof OriginalGame) {
                ((OriginalGame) this.currentApp).setBoxartModified(true);
            }

            this.currentApp.setBoxArtPath(newBoxartFile);
            this.applicationDAO.update(currentApp);

            return new Image(Paths.get(this.pathConfiguration.gamesDirectory, this.currentApp.getApplicationId(),
                    newBoxartFile).toUri().toURL().toExternalForm());
        } else {
            return null;
        }
    }

    public Application getCurrentApp() {
        return currentApp;
    }

    public LibraryManager setCurrentApp(Application currentApp) {
        this.currentApp = currentApp;
        return this;
    }

    public Library getCurrentLibrary() {
        return currentLibrary;
    }

    public LibraryManager setCurrentLibrary(Library currentLibrary) {
        this.currentLibrary = currentLibrary;
        return this;
    }

    public TreeItem<LibraryItem> getCurrentLibraryTree() {
        return currentLibraryTree;
    }

    public LibraryManager setCurrentLibraryTree(TreeItem<LibraryItem> currentLibraryTree) {
        this.currentLibraryTree = currentLibraryTree;
        return this;
    }
}
