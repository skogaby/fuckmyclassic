package com.fuckmyclassic.management;

import com.fuckmyclassic.ui.controller.MainWindow;
import com.fuckmyclassic.hibernate.ApplicationDAO;
import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.LibraryDAO;
import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.ui.component.ApplicationTreeCell;
import com.fuckmyclassic.ui.util.BindingHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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

    @Autowired
    public LibraryManager(final HibernateManager hibernateManager, final ApplicationDAO applicationDAO,
                          final LibraryDAO libraryDAO) {
        this.hibernateManager = hibernateManager;
        this.applicationDAO = applicationDAO;
        this.libraryDAO = libraryDAO;
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
        mainWindow.treeViewGames.setCellFactory(param -> new ApplicationTreeCell(new AppImporter(this.hibernateManager, this)));
        mainWindow.imgBoxArtPreview.setFitWidth(228);
        mainWindow.imgBoxArtPreview.setPreserveRatio(true);
        mainWindow.imgBoxArtPreview.setSmooth(true);
        mainWindow.imgBoxArtPreview.setCache(true);

        // whenever an item is selected, we'll bind the data to the UI and save whatever app
        // was being viewed previously to the database
        mainWindow.treeViewGames.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            final Application app = newValue.getValue();
            final Application oldApp = oldValue == null ? null : oldValue.getValue();
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
        mainWindow.treeViewGames.setRoot(this.applicationDAO.loadLibraryForConsole(this.currentLibrary));
        mainWindow.treeViewGames.getSelectionModel().selectFirst();
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
}
