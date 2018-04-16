package com.fuckmyclassic.management;

import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.ui.controller.MainWindow;
import com.fuckmyclassic.hibernate.ApplicationDAO;
import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.LibraryDAO;
import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.ui.component.ApplicationTreeCell;
import com.fuckmyclassic.ui.util.BindingHelper;
import com.fuckmyclassic.ui.util.ImageResizer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBoxTreeItem;
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
import java.nio.file.Paths;
import java.util.List;

/**
 * Class to manage libraries. This includes keeping track of what the current console and library
 * is, as well as the logic to initialize the library (application) TreeView.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class LibraryManager {

    static Logger LOG = LogManager.getLogger(LibraryManager.class.getName());

    /**
     * Manager for interacting with the Hibernate session
     */
    private final HibernateManager hibernateManager;

    /**
     * DAO for application metadata
     */
    private final ApplicationDAO applicationDAO;

    /**
     * DAO for library metadata
     */
    private final LibraryDAO libraryDAO;

    /**
     * For resizing boxart images we import.
     */
    private final ImageResizer imageResizer;

    /**
     * The currently selected application in the TreeView
     */
    private Application currentApp;

    /**
     * The SID for the console whose collection we're viewing
     */
    private String currentConsoleSid;

    /**
     * The current library we're viewing
     */
    private Library currentLibrary;

    /**
     * A reference to the current library's actual tree structure data.
     */
    private CheckBoxTreeItem<LibraryItem> currentLibraryTree;

    @Autowired
    public LibraryManager(final HibernateManager hibernateManager, final ApplicationDAO applicationDAO,
                          final LibraryDAO libraryDAO, final ImageResizer imageResizer) {
        this.hibernateManager = hibernateManager;
        this.applicationDAO = applicationDAO;
        this.libraryDAO = libraryDAO;
        this.imageResizer = imageResizer;
        this.currentApp = null;
        this.currentConsoleSid = SharedConstants.DEFAULT_CONSOLE_SID;
        this.currentLibrary = null;
    }

    /**
     * Sets up the dropdown for the library selection.
     */
    public void initializeLibrarySelection(final MainWindow mainWindow) {
        LOG.debug("Initializing the dropdown box for library selection");

        final List<Library> libraries = libraryDAO.getLibrariesForConsole(this.currentConsoleSid);
        final ObservableList<Library> items = FXCollections.observableArrayList(libraries);
        mainWindow.cmbCurrentCollection.setItems(items);
        mainWindow.cmbCurrentCollection.getSelectionModel().selectFirst();
        this.currentLibrary = items.get(0);
    }

    /**
     * Initializes the TreeView display for the applications and games.
     */
    public void initializeApplicationTreeView(final MainWindow mainWindow) {
        LOG.debug("Initializing the tree view for games");

        // initialize the cell factory so we can control theming, drag and drop, etc.
        mainWindow.treeViewGames.setCellFactory(param ->
                new ApplicationTreeCell(new AppImporter(this.hibernateManager, this)));

        // whenever an item is selected, we'll bind the data to the UI and save whatever app
        // was being viewed previously to the database
        mainWindow.treeViewGames.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            final Application app = newValue.getValue().getApplication();
            final Application oldApp = oldValue == null ? null : oldValue.getValue().getApplication();
            this.currentApp = app;

            BindingHelper.bindProperty(app.applicationIdProperty(), mainWindow.lblApplicationId.textProperty());
            BindingHelper.bindProperty(app.applicationSizeProperty().asString(), mainWindow.lblGameSize.textProperty());
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
            if (!StringUtils.isEmpty(app.getBoxArtPath())) {
                mainWindow.imgBoxArtPreview.setImage(new Image(
                        Paths.get("file:" + SharedConstants.BOXART_DIRECTORY, app.getBoxArtPath()).toString()));
            } else {
                mainWindow.imgBoxArtPreview.setImage(new Image(SharedConstants.WARNING_IMAGE));
            }

            // persist the item to the database and refresh the application view
            this.hibernateManager.updateEntity(oldApp);
            mainWindow.treeViewGames.refresh();
        });

        // load the library items for the current console and library
        LOG.info(String.format("Loading library for console %s from the database", this.currentConsoleSid));
        this.currentLibraryTree = this.applicationDAO.loadApplicationTreeForLibrary(this.currentLibrary);
        mainWindow.treeViewGames.setRoot(this.currentLibraryTree);
        mainWindow.treeViewGames.getSelectionModel().selectFirst();
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
            File outputFile = new File(Paths.get(SharedConstants.BOXART_DIRECTORY, newBoxartFile).toUri());
            ImageIO.write(resizedImage, "png", outputFile);

            // now, do the thumbnail
            resizedImage = this.imageResizer.resizeProportionally(inputImage,
                    SharedConstants.THUMBNAIL_SIZE, SharedConstants.THUMBNAIL_SIZE);
            outputFile = new File(Paths.get(SharedConstants.BOXART_DIRECTORY, newThumbnailFile).toUri());
            ImageIO.write(resizedImage, "png", outputFile);

            // also update the Application itself
            this.currentApp.setBoxArtPath(newBoxartFile);
            this.hibernateManager.updateEntity(currentApp);

            return new Image(Paths.get("file:" + SharedConstants.BOXART_DIRECTORY, newBoxartFile).toString());
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

    public String getCurrentConsoleSid() {
        return currentConsoleSid;
    }

    public LibraryManager setCurrentConsoleSid(String currentConsoleSid) {
        this.currentConsoleSid = currentConsoleSid;
        return this;
    }

    public Library getCurrentLibrary() {
        return currentLibrary;
    }

    public LibraryManager setCurrentLibrary(Library currentLibrary) {
        this.currentLibrary = currentLibrary;
        return this;
    }

    public CheckBoxTreeItem<LibraryItem> getCurrentLibraryTree() {
        return currentLibraryTree;
    }
}
