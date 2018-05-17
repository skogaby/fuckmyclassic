package com.fuckmyclassic.ui.controller;

import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.dao.ApplicationDAO;
import com.fuckmyclassic.hibernate.dao.ConsoleDAO;
import com.fuckmyclassic.hibernate.dao.LibraryDAO;
import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Console;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.ui.util.BindingHelper;
import com.fuckmyclassic.userconfig.UserConfiguration;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The window to let the user manage libraries and see data about them.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class LibraryManagementWindow {

    static Logger LOG = LogManager.getLogger(LibraryManagementWindow.class.getName());

    private static final String TITLE_STRING_KEY = "LibraryManagementWindow.titleBar";

    public ComboBox<Console> cmbCurrentConsole;
    public ListView<Library> lstLibraries;
    public TextField txtLibraryName;
    public TextField txtConsoleName;
    public Label lblConsoleType;
    public Label lblConsoleIp;
    public Label lblConsoleSid;
    public TreeView<LibraryItem> treeViewGames;

    /** The configuration object for user options and session settings */
    private final UserConfiguration userConfiguration;
    /** DAO for accessing known consoles */
    private final ConsoleDAO consoleDAO;
    /** DAO for library metadata */
    private final LibraryDAO libraryDAO;
    /** DAO for application metadata */
    private final ApplicationDAO applicationDAO;
    /** Manager for persisting entities to the database */
    private final HibernateManager hibernateManager;
    /** Hibernate session, for purging the context after the window closes */
    private final Session session;
    /** The set of Consoles and Libraries that have been edited that need to be saved if the user chooses to */
    private final Set<Object> editedEntities;
    /** The set of Libraries and LibraryItems that have been added */
    private final Set<Object> addedLibraryData;
    /** The set of Libraries and LibraryItems that have been removed */
    private final Set<Object> removedLibraryData;
    /** The selected console for this window */
    private Console selectedConsole;
    /** The currently selected library for this window */
    private Library selectedLibrary;
    /** The list of consoles that we display in the dropdown */
    private ObservableList<Console> displayedConsoles;
    /** The list of libraries that we display in the list */
    private ObservableList<Library> displayedLibraries;
    /** Says whether or not we should save the edited items when the window closes */
    private boolean shouldSave;

    @Autowired
    public LibraryManagementWindow(final UserConfiguration userConfiguration,
                                   final ConsoleDAO consoleDAO,
                                   final LibraryDAO libraryDAO,
                                   final ApplicationDAO applicationDAO,
                                   final HibernateManager hibernateManager,
                                   final Session session) {
        this.userConfiguration = userConfiguration;
        this.consoleDAO = consoleDAO;
        this.libraryDAO = libraryDAO;
        this.applicationDAO = applicationDAO;
        this.hibernateManager = hibernateManager;
        this.editedEntities = new HashSet<>();
        this.addedLibraryData = new HashSet<>();
        this.removedLibraryData = new HashSet<>();
        this.session = session;
    }

    @FXML
    public void initialize() {
        this.selectedConsole = null;
        this.selectedLibrary = null;
        this.editedEntities.clear();
        this.addedLibraryData.clear();
        this.removedLibraryData.clear();
        this.cmbCurrentConsole.setItems(null);
        this.lstLibraries.setItems(null);
        this.shouldSave = false;

        if (this.displayedConsoles != null) {
            this.displayedConsoles.clear();
        }

        if (this.displayedLibraries != null) {
            this.displayedLibraries.clear();
        }

        initializeConsoleDropdown();
    }

    /**
     * Initializes the console selection ComboBox.
     */
    private void initializeConsoleDropdown() {
        if (this.selectedConsole == null) {
            this.selectedConsole = this.userConfiguration.getSelectedConsole();
        }

        final List<Console> consoles = this.consoleDAO.getAllConsoles();
        this.displayedConsoles = FXCollections.observableArrayList(consoles);
        this.cmbCurrentConsole.setItems(this.displayedConsoles);

        // add the combobox listener
        this.cmbCurrentConsole.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.selectedConsole = newValue;
                initializeConsoleInfoPanel(oldValue, newValue);
                initializeLibraryList();
            }
        });

        // add the console name textfield listener
        this.txtConsoleName.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            // if we had focus and lost it, update the view and add the entity to the list
            if (oldValue && !newValue) {
                this.editedEntities.add(this.selectedConsole);

                this.displayedConsoles.forEach(console -> {
                    if (console.getConsoleSid().equals(this.selectedConsole.getConsoleSid())) {
                        console.setNickname(this.selectedConsole.getNickname());
                    }
                });
            }
        }));

        this.cmbCurrentConsole.getSelectionModel().select(this.selectedConsole);
    }

    /**
     * Initializes the data in the console information panel of the window.
     * @param oldConsole The previously selected console
     * @param console The newly selected console
     */
    private void initializeConsoleInfoPanel(final Console oldConsole, final Console console) {
        BindingHelper.bindPropertyBidirectional(oldConsole == null ? null : oldConsole.nicknameProperty(),
                console.nicknameProperty(), this.txtConsoleName.textProperty());
        BindingHelper.bindProperty(console.consoleTypeProperty().asString(), this.lblConsoleType.textProperty());
        BindingHelper.bindProperty((ReadOnlyProperty) console.lastKnownAddressProperty(), this.lblConsoleIp.textProperty());
        BindingHelper.bindProperty((ReadOnlyProperty) console.consoleSidProperty(), this.lblConsoleSid.textProperty());
    }

    /**
     * Initializes the ListView that shows the libraries.
     */
    private void initializeLibraryList() {
        final List<Library> libraries = this.libraryDAO.getLibrariesForConsole(this.selectedConsole.getConsoleSid());
        displayedLibraries = FXCollections.observableArrayList(libraries);
        this.lstLibraries.setItems(displayedLibraries);

        // add the listview listener
        this.lstLibraries.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                initializeLibraryInfoPanel(this.selectedLibrary, newValue);
                this.selectedLibrary = newValue;
            }
        }));

        // add the library name textview listener
        this.txtLibraryName.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            // if we had focus and lost it, update the view and add the entity to the list
            if (oldValue && !newValue) {
                this.editedEntities.add(this.selectedLibrary);

                this.addedLibraryData.forEach(library -> {
                    if (library instanceof Library) {
                        if (((Library) library).getId() == this.selectedLibrary.getId()) {
                            ((Library) library).setLibraryName(this.selectedLibrary.getLibraryName());
                        }
                    }
                });

                this.displayedLibraries.forEach(library -> {
                    if (library.getId() == this.selectedLibrary.getId()) {
                        library.setLibraryName(this.selectedLibrary.getLibraryName());
                    }
                });

                this.lstLibraries.refresh();
            }
        }));

        this.lstLibraries.getSelectionModel().select(libraries.get(0));
    }

    /**
     * Initializes the data in the library information panel of the window.
     * @param oldLibrary The previously selected library
     * @param newLibrary The newly selected library
     */
    private void initializeLibraryInfoPanel(final Library oldLibrary, final Library newLibrary) {
        BindingHelper.bindPropertyBidirectional(oldLibrary == null ? null : oldLibrary.libraryNameProperty(),
                newLibrary.libraryNameProperty(), this.txtLibraryName.textProperty());

        final TreeItem<LibraryItem> applications = libraryDAO.loadApplicationTreeForLibrary(newLibrary, false, true);
        this.treeViewGames.setRoot(applications);
    }

    /**
     * Saves all the edited entities and clears the set.
     */
    private void saveEditedEntities() {
        this.session.clear();
        this.hibernateManager.updateEntities(this.editedEntities.toArray());

        this.editedEntities.clear();
        this.addedLibraryData.clear();
        this.removedLibraryData.clear();
    }

    /**
     * Closes this dialog window.
     */
    private void closeWindow() {
        ((Stage) this.cmbCurrentConsole.getScene().getWindow()).close();
    }

    /**
     * OnClick handler for the add library button.
     */
    @FXML
    private void onAddLibraryClick() {
        final Library defaultLibrary = new Library(this.selectedConsole.getConsoleSid(),
                SharedConstants.DEFAULT_LIBRARY_NAME);
        this.addedLibraryData.add(defaultLibrary);
        this.hibernateManager.saveEntities(defaultLibrary);

        final Application homeFolder = this.applicationDAO.loadApplicationByAppId(SharedConstants.HOME_FOLDER_ID);
        final LibraryItem homeFolderItem = new LibraryItem()
                .setFolder(null)
                .setApplication(homeFolder)
                .setLibrary(defaultLibrary)
                .setSelected(true);
        this.addedLibraryData.add(homeFolderItem);
        this.hibernateManager.saveEntities(homeFolderItem);

        // update the list
        this.displayedLibraries.add(defaultLibrary);
        this.lstLibraries.refresh();
    }

    /**
     * OnClick handler for the remove library button.
     */
    @FXML
    private void onRemoveLibraryClick() {
        final Library libraryToDelete = new Library(this.selectedLibrary);
        this.removedLibraryData.add(libraryToDelete);

        // remove the library items from the database. we add copies of the original
        // objects instead of the originals themselves, because if we cancel the window
        // and need to re-insert the records to the database, they have to have new IDs.
        // so we'll construct new objects and let Hibernate do its thing
        final List<LibraryItem> libraryItems = this.libraryDAO.getApplicationsForLibrary(this.selectedLibrary, false);
        this.removedLibraryData.addAll(libraryItems.stream()
                .map(item -> new LibraryItem(libraryToDelete, item.getApplication(), item.getFolder(),
                        item.isSelected(), item.getNumNodes()))
                .collect(Collectors.toList()));
        this.hibernateManager.deleteEntities(libraryItems.toArray());

        // remove the library from the database
        this.hibernateManager.deleteEntities(this.selectedLibrary);

        // remove the library from the collections
        this.editedEntities.remove(this.selectedLibrary);
        this.addedLibraryData.remove(this.selectedLibrary);
        this.displayedLibraries.remove(this.selectedLibrary);

        // refresh the list and select the first item
        this.lstLibraries.refresh();
        this.lstLibraries.getSelectionModel().selectFirst();
    }

    /**
     * OnClick handler for the copy library button.
     */
    @FXML
    private void onCopyLibraryClick() {

    }

    /**
     * OnClick handler for the Cancel button.
     */
    @FXML
    private void onCancelClick() {
        this.shouldSave = false;
        this.closeWindow();
    }

    /**
     * OnClick handler for the Apply button.
     */
    @FXML
    private void onApplyClick() {
        this.shouldSave = true;
        this.saveEditedEntities();
    }

    /**
     * OnClick handler for the OK button.
     */
    @FXML
    private void onOKClick() {
        this.shouldSave = true;
        this.saveEditedEntities();
        this.closeWindow();
    }

    /**
     * Spawns a new management window.
     */
    public void showWindow() throws IOException {
        final ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/LibraryManagementWindow");
        final FXMLLoader loader = new FXMLLoader(LibraryManagementWindow.class.getClassLoader()
                .getResource("fxml/LibraryManagementWindow.fxml"), resourceBundle);
        loader.setController(this);

        final Stage stage = new Stage();
        final Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(resourceBundle.getString(TITLE_STRING_KEY));
        stage.showAndWait();

        // clear out the Hibernate context, just in case we edited data that we never saved,
        // we don't want Hibernate returning it in subsequent queries
        this.session.clear();

        // if we didn't save the edited entities, we need to remove the new libraries that were created as well
        if (!this.shouldSave) {
            this.hibernateManager.deleteEntities(this.addedLibraryData.toArray());
            this.hibernateManager.saveEntities(this.removedLibraryData.toArray());
        }
    }
}
