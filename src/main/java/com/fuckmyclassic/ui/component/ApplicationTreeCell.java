package com.fuckmyclassic.ui.component;

import com.fuckmyclassic.hibernate.dao.impl.ApplicationDAO;
import com.fuckmyclassic.hibernate.dao.impl.LibraryItemDAO;
import com.fuckmyclassic.management.AppImporter;
import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Folder;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.userconfig.PathConfiguration;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Class to encapsulate the logic for each cell in the TreeView
 * that shows the folders and applications.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class ApplicationTreeCell extends CheckBoxTreeCell<LibraryItem> {

    static Logger LOG = LogManager.getLogger(ApplicationTreeCell.class.getName());

    private static final String APP_OPEN_FOLDER_KEY = "MainWindow.appOpenInExplorer";
    private static final String APP_DELETE_FROM_LIBRARY_KEY = "MainWindow.appRemoveFromLibrary";
    private static final String APP_DELETE_KEY = "MainWindow.appDelete";
    private static final String CONFIRMATION_HEADER_KEY = "Confirmation.Header";
    private static final String REMOVE_CONFIRMATION_KEY = "MainWindow.removefromLibraryConfirmation";
    private static final String DELETE_CONFIRMATION_KEY = "MainWindow.deleteConfirmation";

    /** The path to the stylesheet for this cell */
    private static final String STYLESHEET_PATH = "css/MainWindow.css";
    /** CSS style class for a cell in the TreeView that's selected */
    private static final String SELECTED_CELL_STYLE_CLASS = "selected-cell";

    /** DAO for manipulating Applications */
    private final ApplicationDAO applicationDAO;
    /** DAO for manipulating LibraryItems */
    private final LibraryItemDAO libraryItemDAO;
    /** Helper class to import new games to the current library */
    private final AppImporter appImporter;
    /** Helper class to have file paths for opening in explorer, etc. */
    private final PathConfiguration pathConfiguration;
    /** Context menu for games and applications */
    private final ContextMenu appContextMenu;
    /** Localization */
    private final ResourceBundle resourceBundle;

    /**
     * Constructor. Sets up the event handlers.
     */
    public ApplicationTreeCell(final ApplicationDAO applicationDAO,
                               final LibraryItemDAO libraryItemDAO,
                               final AppImporter appImporter,
                               final PathConfiguration pathConfiguration) {
        this.applicationDAO = applicationDAO;
        this.libraryItemDAO = libraryItemDAO;
        this.appImporter = appImporter;
        this.pathConfiguration = pathConfiguration;
        this.appContextMenu = new ContextMenu();
        this.resourceBundle = ResourceBundle.getBundle("i18n/MainWindow");

        // set the event handlers for the cell
        setOnDragOver(event -> onDragOver(event));
        setOnDragDropped(event -> onDragDropped(event));
        setOnDragEntered(event -> onDragEntered(event));
        setOnDragExited(event -> onDragExited(event));

        // setup the context menus
        final ObservableList<MenuItem> appMenuItems = this.appContextMenu.getItems();
        final MenuItem openFolderMenuItem = new MenuItem(this.resourceBundle.getString(APP_OPEN_FOLDER_KEY));
        final MenuItem removeFromLibraryMenuItem = new MenuItem(this.resourceBundle.getString(APP_DELETE_FROM_LIBRARY_KEY));
        final MenuItem deleteAppMenuItem = new MenuItem(this.resourceBundle.getString(APP_DELETE_KEY));
        appMenuItems.add(openFolderMenuItem);
        appMenuItems.add(new SeparatorMenuItem());
        appMenuItems.add(removeFromLibraryMenuItem);
        appMenuItems.add(deleteAppMenuItem);

        openFolderMenuItem.setOnAction(event -> {
            try {
                onOpenFolderMenuItemClicked();
            } catch (IOException e) {
                LOG.error(e);
            }
        });

        removeFromLibraryMenuItem.setOnAction(event -> {
            try {
                onRemoveFromLibraryMenuItemClicked();
            } catch (IOException e) {
                LOG.error(e);
            }
        });

        deleteAppMenuItem.setOnAction(event -> {
            try {
                deleteAppMenuItem();
            } catch (IOException e) {
                LOG.error(e);
            }
        });

        // styling
        getStylesheets().add(ApplicationTreeCell.class.getClassLoader().getResource(STYLESHEET_PATH).toExternalForm());
    }

    /**
     * Called whenever this cell is recycled to show a new item.
     * @param item The item to show
     * @param empty Whether the cell is empty
     */
    @Override
    public void updateItem(final LibraryItem item, final boolean empty) {
        super.updateItem(item, empty);

        // handle the display
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.getApplication().getApplicationName());

            if (!(item.getApplication() instanceof Folder)) {
                setContextMenu(this.appContextMenu);
            }
        }
    }

    /**
     * Event handler for when something is dragged over this particular cell.
     * @param event
     */
    private void onDragOver(final DragEvent event) {
        final Dragboard db = event.getDragboard();

        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }

        event.consume();
    }

    /**
     * Event handler for when an item is dropped into this cell.
     * @param event
     */
    private void onDragDropped(final DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;

        if (db.hasFiles()) {
            success = true;

            try {
                this.appImporter.handleFileImportAttempt(db.getFiles(), (CheckBoxTreeItem) getTreeItem());
            } catch (IOException e) {
                LOG.error("Error in importing new app", e);
            }

            getTreeView().refresh();
        }

        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * Event handler for when a drag enters this cell. Used for styling.
     * @param event
     */
    private void onDragEntered(final DragEvent event) {
        final CheckBoxTreeItem<LibraryItem> item = (CheckBoxTreeItem) getTreeItem();
        final LibraryItem app = getItem();

        if (app != null) {
            if (app.getApplication() instanceof Folder) {
                this.getStyleClass().add(SELECTED_CELL_STYLE_CLASS);
            } else {
                // style the parent folder
                getCellForParentFolder(item).getStyleClass().add(SELECTED_CELL_STYLE_CLASS);
            }
        }

        event.consume();
    }

    /**
     * Event handler for when a drag leaves this cell. Used for styling.
     * @param event
     */
    private void onDragExited(final DragEvent event) {
        final CheckBoxTreeItem<LibraryItem> item = (CheckBoxTreeItem) getTreeItem();
        final LibraryItem app = getItem();

        if (app != null) {
            if (app.getApplication() instanceof Folder) {
                this.getStyleClass().removeAll(SELECTED_CELL_STYLE_CLASS);
            } else {
                // style the parent folder
                getCellForParentFolder(item).getStyleClass().removeAll(SELECTED_CELL_STYLE_CLASS);
            }
        }

        event.consume();
    }

    /**
     * Returns the TreeCell that represents the folder that the given app is inside of.
     * @param item The application to find the parent folder TreeCell for
     * @return The TreeCell for the folder the given application is inside of
     */
    private TreeCell<LibraryItem> getCellForParentFolder(final CheckBoxTreeItem<LibraryItem> item) {
        // we'll need to iterate through the children of the parent
        // node. fortunately, since TreeCells are recycled, there
        // should only be about 30 items to iterate through in
        // the worst case
        final LibraryItem parentFolder = item.getParent().getValue();
        final Group cellGroup = (Group) getStyleableParent();

        for (Node node : cellGroup.getChildren()) {
            if (((TreeCell<LibraryItem>) node).getTreeItem() != null &&
                    ((TreeCell<LibraryItem>) node).getTreeItem().getValue().equals(parentFolder)) {
                return (TreeCell<LibraryItem>) node;
            }
        }

        return null;
    }

    /**
     * Opens the selected application in the file explorer.
     */
    private void onOpenFolderMenuItemClicked() throws IOException {
        java.awt.Desktop.getDesktop().open(
                new File(
                        Paths.get(this.pathConfiguration.gamesDirectory,
                                getItem().getApplication().getApplicationId()).toString()));
    }

    /**
     * Removes the selected item from the current library and deletes the application
     * directory if no other LibraryItems reference the same application.
     */
    private void onRemoveFromLibraryMenuItemClicked() throws IOException {
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(null);
        alert.setHeaderText(this.resourceBundle.getString(CONFIRMATION_HEADER_KEY));
        alert.setContentText(this.resourceBundle.getString(REMOVE_CONFIRMATION_KEY));

        final Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            final TreeItem<LibraryItem> treeItem = getTreeItem();
            final LibraryItem libraryItem = getItem();
            final Application application = libraryItem.getApplication();

            // check if this application belongs to any other libraries or is a duplicate. if it is,
            // then we'll just delete the LibraryItem from the current library and call it good.
            // if it doesn't exist in any other library, we need to also delete the actual game data and Application
            final List<LibraryItem> libraryItemList = this.libraryItemDAO.getLibraryItemsForApplication(application);
            boolean isDuplicate = libraryItemList.size() > 1;

            // delete the current library item and remove it from the view
            getTreeItem().getParent().getChildren().removeAll(treeItem);
            libraryItemDAO.delete(libraryItem);

            if (!isDuplicate) {
                // remove the game data
                FileUtils.deleteDirectory(
                        Paths.get(pathConfiguration.gamesDirectory, application.getApplicationId()).toFile());

                // delete the application
                this.applicationDAO.delete(application);
            }
        }
    }

    /**
     * Removes the selected item from all libraries and deletes the application data.
     * @throws IOException
     */
    private void deleteAppMenuItem() throws IOException {
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(null);
        alert.setHeaderText(this.resourceBundle.getString(CONFIRMATION_HEADER_KEY));
        alert.setContentText(this.resourceBundle.getString(DELETE_CONFIRMATION_KEY));

        final Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            final TreeItem<LibraryItem> treeItem = getTreeItem();
            final LibraryItem libraryItem = getItem();
            final Application application = libraryItem.getApplication();
            final List<LibraryItem> libraryItemList = this.libraryItemDAO.getLibraryItemsForApplication(application);

            // delete the library items and remove it from the view
            getTreeItem().getParent().getChildren().removeAll(treeItem);
            libraryItemDAO.delete(libraryItemList.toArray(new LibraryItem[libraryItemList.size()]));

            // remove the game data
            FileUtils.deleteDirectory(
                    Paths.get(pathConfiguration.gamesDirectory, application.getApplicationId()).toFile());

            // delete the application
            this.applicationDAO.delete(application);
        }
    }
}
