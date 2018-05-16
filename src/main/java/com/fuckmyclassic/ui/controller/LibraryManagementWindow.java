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
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    public Button btnAddLibrary;
    public Button btnRemoveLibrary;
    public Button btnCopyLibrary;
    public ListView<Library> lstLibraries;
    public TextField txtLibraryName;
    public TextField txtConsoleName;
    public Label lblConsoleType;
    public Label lblConsoleIp;
    public Label lblConsoleSid;
    public TreeView<LibraryItem> treeViewGames;
    public Button btnCancel;
    public Button btnApply;
    public Button btnOK;

    /** The configuration object for user options and session settings */
    private final UserConfiguration userConfiguration;
    /** DAO for accessing known consoles */
    private final ConsoleDAO consoleDAO;
    /** DAO for library metadata */
    private final LibraryDAO libraryDAO;
    /** Manager for persisting entities to the database */
    private final HibernateManager hibernateManager;
    /** The set of Consoles and Libraries that have been edited that need to be saved if the user chooses to */
    private final Set<Object> editedEntities;
    /** The selected console for this window */
    private Console selectedConsole;
    /** The currently selected library for this window */
    private Library selectedLibrary;
    /** Says whether or not the current library has been edited so we can persist it before changing items */
    private boolean libraryEdited;
    /** Says whether or not the current console has been edited so we can persist it before changing items */
    private boolean consoleEdited;
    /** Says whether we've already initialized our listeners for the console list */
    private boolean consoleListenerAdded;
    /** Says whether we've already initialized our listeners for the library list */
    private boolean libraryListenerAdded;

    @Autowired
    public LibraryManagementWindow(final UserConfiguration userConfiguration,
                                   final ConsoleDAO consoleDAO,
                                   final LibraryDAO libraryDAO,
                                   final HibernateManager hibernateManager) {
        this.userConfiguration = userConfiguration;
        this.consoleDAO = consoleDAO;
        this.libraryDAO = libraryDAO;
        this.hibernateManager = hibernateManager;
        this.editedEntities = new HashSet<>();
    }

    @FXML
    public void initialize() {
        this.selectedConsole = null;
        this.libraryEdited = false;
        this.consoleEdited = false;
        this.consoleListenerAdded = false;
        this.libraryListenerAdded = false;
        this.editedEntities.clear();

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
        final ObservableList<Console> items = FXCollections.observableArrayList(consoles);
        this.cmbCurrentConsole.setItems(items);

        if (!this.consoleListenerAdded) {
            this.cmbCurrentConsole.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    this.selectedConsole = newValue;

                    if (this.consoleEdited && oldValue != null) {
                        this.editedEntities.add(oldValue);
                        // refresh the console dropdown
                        this.cmbCurrentConsole.setItems(this.cmbCurrentConsole.getItems());
                        this.consoleEdited = false;
                    }

                    if (this.libraryEdited) {
                        this.editedEntities.add(this.selectedLibrary);
                        this.libraryEdited = false;
                    }

                    initializeConsoleInfoPanel(oldValue, newValue);
                    initializeLibraryList();
                }
            });

            this.consoleListenerAdded = true;
        }

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
        final ObservableList<Library> items = FXCollections.observableArrayList(libraries);
        this.selectedLibrary = libraries.get(0);
        this.lstLibraries.setItems(items);

        if (!this.libraryListenerAdded) {
            this.lstLibraries.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    if (this.libraryEdited &&
                            oldValue != null) {
                        this.editedEntities.add(oldValue);
                        // refresh the console dropdown
                        this.cmbCurrentConsole.setItems(this.cmbCurrentConsole.getItems());
                        this.consoleEdited = false;
                    }

                    if (this.consoleEdited &&
                            oldValue != null) {
                        this.editedEntities.add(this.selectedConsole);
                        this.cmbCurrentConsole.setItems(this.cmbCurrentConsole.getItems());
                        this.consoleEdited = false;
                    }

                    this.selectedLibrary = newValue;
                    initializeLibraryInfoPanel(oldValue, newValue);
                }
            }));

            this.libraryListenerAdded = true;
        }

        this.lstLibraries.getSelectionModel().select(this.selectedLibrary);
    }

    /**
     * Initializes the data in the library information panel of the window.
     * @param oldLibrary The previously selected library
     * @param newLibrary The newly selected library
     */
    private void initializeLibraryInfoPanel(final Library oldLibrary, final Library newLibrary) {
        BindingHelper.bindPropertyBidirectional(oldLibrary == null ? null : oldLibrary.libraryNameProperty(),
                newLibrary.libraryNameProperty(), this.txtLibraryName.textProperty());

        final TreeItem<LibraryItem> applications = libraryDAO.loadApplicationTreeForLibrary(this.selectedLibrary, false);
        this.treeViewGames.setRoot(applications);
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
    }
}
