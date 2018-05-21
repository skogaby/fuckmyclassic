package com.fuckmyclassic.ui.controller;

import com.fuckmyclassic.hibernate.dao.impl.ApplicationDAO;
import com.fuckmyclassic.hibernate.dao.impl.ConsoleDAO;
import com.fuckmyclassic.hibernate.dao.impl.LibraryDAO;
import com.fuckmyclassic.hibernate.dao.impl.LibraryItemDAO;
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
    /** DAO for library item metadata */
    private final LibraryItemDAO libraryItemDAO;
    /** The set of Consoles that have been edited */
    private final Set<Console> editedConsoles;
    /** The set of Libraries that have been edited */
    private final Set<Library> editedLibraries;
    /** The set of Libraries that are newly created */
    private final Set<Library> newLibraries;
    /** The set of LibraryItems that are newly created */
    private final Set<LibraryItem> newLibraryItems;
    /** The set of Libraries that are newly deleted */
    private final Set<Library> removedLibraries;
    /** The set of LibraryItems that are newly deleted */
    private final Set<LibraryItem> removedLibraryItems;
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
                                   final LibraryItemDAO libraryItemDAO) {
        this.userConfiguration = userConfiguration;
        this.consoleDAO = consoleDAO;
        this.libraryDAO = libraryDAO;
        this.applicationDAO = applicationDAO;
        this.libraryItemDAO = libraryItemDAO;
        this.editedConsoles = new HashSet<>();
        this.editedLibraries = new HashSet<>();
        this.newLibraries = new HashSet<>();
        this.newLibraryItems = new HashSet<>();
        this.removedLibraries = new HashSet<>();
        this.removedLibraryItems = new HashSet<>();
    }

    @FXML
    public void initialize() {
        this.selectedConsole = null;
        this.selectedLibrary = null;
        this.editedConsoles.clear();
        this.editedLibraries.clear();
        this.newLibraries.clear();
        this.newLibraryItems.clear();
        this.removedLibraries.clear();
        this.removedLibraryItems.clear();
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
                this.editedConsoles.add(this.selectedConsole);

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
                this.editedLibraries.add(this.selectedLibrary);

                this.newLibraries.forEach(library -> {
                    if (library.getId() == this.selectedLibrary.getId()) {
                        library.setLibraryName(this.selectedLibrary.getLibraryName());
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
        this.libraryDAO.update(this.editedLibraries.toArray(new Library[this.editedLibraries.size()]));
        this.consoleDAO.update(this.editedConsoles.toArray(new Console[this.editedConsoles.size()]));

        this.editedConsoles.clear();
        this.editedLibraries.clear();
        this.newLibraries.clear();
        this.newLibraryItems.clear();
        this.removedLibraryItems.clear();
        this.removedLibraries.clear();
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
        this.newLibraries.add(defaultLibrary);
        this.libraryDAO.create(defaultLibrary);

        final Application homeFolder = this.applicationDAO.loadApplicationByAppId(SharedConstants.HOME_FOLDER_ID);
        final LibraryItem homeFolderItem = new LibraryItem()
                .setFolder(null)
                .setApplication(homeFolder)
                .setLibrary(defaultLibrary)
                .setSelected(true);
        this.newLibraryItems.add(homeFolderItem);
        this.libraryItemDAO.create(homeFolderItem);

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
        this.removedLibraries.add(libraryToDelete);

        // make sure we didn't delete the selected console
        if (this.selectedLibrary.getId() == this.userConfiguration.getSelectedLibraryID()) {
            final Console c = this.displayedConsoles.get(0);
            this.userConfiguration.setSelectedConsole(c);

            for (Library l : this.displayedLibraries) {
                if (l.getConsoleSid().equals(c.getConsoleSid()) &&
                        !l.equals(this.selectedLibrary)) {
                    this.userConfiguration.setSelectedLibraryID(l.getId());
                    break;
                }
            }
        }

        // remove the library items from the database. we add copies of the original
        // objects instead of the originals themselves, because if we cancel the window
        // and need to re-insert the records to the database, they have to have new IDs.
        // so we'll construct new objects and let Hibernate do its thing
        final List<LibraryItem> libraryItems = this.libraryDAO.getApplicationsForLibrary(this.selectedLibrary, false);
        this.removedLibraryItems.addAll(libraryItems.stream()
                .map(item -> new LibraryItem(libraryToDelete, item.getApplication(), item.getFolder(),
                        item.isSelected(), item.getNumNodes(), item.getTreeFilesize()))
                .collect(Collectors.toList()));
        this.libraryItemDAO.delete(libraryItems.toArray(new LibraryItem[libraryItems.size()]));

        // remove the library from the database
        this.libraryDAO.delete(this.selectedLibrary);

        // remove the library from the collections
        this.editedLibraries.remove(this.selectedLibrary);
        this.newLibraries.remove(this.selectedLibrary);
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
        final Library libraryToAdd = new Library(this.selectedLibrary);
        this.newLibraries.add(libraryToAdd);
        this.libraryDAO.create(libraryToAdd);

        final List<LibraryItem> newItems = this.libraryDAO.getApplicationsForLibrary(this.selectedLibrary, false)
                .stream()
                .map(item -> new LibraryItem(libraryToAdd, item.getApplication(), item.getFolder(),
                        item.isSelected(), item.getNumNodes(), item.getTreeFilesize()))
                .collect(Collectors.toList());
        this.newLibraryItems.addAll(newItems);
        this.libraryItemDAO.create(newItems.toArray(new LibraryItem[newItems.size()]));

        // update the list
        this.displayedLibraries.add(libraryToAdd);
        this.lstLibraries.refresh();
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

        // if we didn't save the edited entities, we need to remove the new libraries that were created as well
        if (!this.shouldSave) {
            this.libraryItemDAO.delete(this.newLibraryItems.toArray(new LibraryItem[this.newLibraryItems.size()]));
            this.libraryDAO.delete(this.newLibraries.toArray(new Library[this.newLibraries.size()]));
            this.libraryDAO.create(this.removedLibraries.toArray(new Library[this.removedLibraries.size()]));
            this.libraryItemDAO.create(this.removedLibraryItems.toArray(new LibraryItem[this.removedLibraryItems.size()]));
        }
    }
}
