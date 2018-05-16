package com.fuckmyclassic.ui.controller;

import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.hibernate.dao.ConsoleDAO;
import com.fuckmyclassic.hibernate.dao.LibraryDAO;
import com.fuckmyclassic.model.Console;
import com.fuckmyclassic.model.Library;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.ui.util.BindingHelper;
import com.fuckmyclassic.userconfig.UserConfiguration;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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
    /** Manager for persisting entities to the database */
    private final HibernateManager hibernateManager;
    /** Hibernate session, for purging the context after the window closes */
    private final Session session;
    /** The set of Consoles and Libraries that have been edited that need to be saved if the user chooses to */
    private final Set<Object> editedEntities;
    /** The selected console for this window */
    private Console selectedConsole;
    /** The currently selected library for this window */
    private Library selectedLibrary;
    /** The list of consoles that we display in the dropdown */
    private ObservableList<Console> displayedConsoles;
    /** The list of libraries that we display in the list */
    private ObservableList<Library> displayedLibraries;

    @Autowired
    public LibraryManagementWindow(final UserConfiguration userConfiguration,
                                   final ConsoleDAO consoleDAO,
                                   final LibraryDAO libraryDAO,
                                   final HibernateManager hibernateManager,
                                   final Session session) {
        this.userConfiguration = userConfiguration;
        this.consoleDAO = consoleDAO;
        this.libraryDAO = libraryDAO;
        this.hibernateManager = hibernateManager;
        this.editedEntities = new HashSet<>();
        this.session = session;
    }

    @FXML
    public void initialize() {
        this.selectedConsole = null;
        this.selectedLibrary = null;
        this.editedEntities.clear();
        this.cmbCurrentConsole.setItems(null);
        this.lstLibraries.setItems(null);

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

                this.displayedLibraries.forEach(library -> {
                    if (library.getId() == this.selectedLibrary.getId()) {
                        library.setLibraryName(this.selectedLibrary.getLibraryName());
                        this.lstLibraries.refresh();
                    }
                });
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
        this.editedEntities.forEach(object -> {
            this.hibernateManager.updateEntity(object);
        });

        this.editedEntities.clear();
    }

    /**
     * Closes this dialog window.
     */
    private void closeWindow() {
        ((Stage) this.cmbCurrentConsole.getScene().getWindow()).close();
    }

    /**
     * OnClick handler for the Cancel button.
     */
    @FXML
    private void onCancelClick() {
        this.closeWindow();
    }

    /**
     * OnClick handler for the Apply button.
     */
    @FXML
    private void onApplyClick() {
        this.saveEditedEntities();
    }

    /**
     * OnClick handler for the OK button.
     */
    @FXML
    private void onOKClick() {
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
    }
}
