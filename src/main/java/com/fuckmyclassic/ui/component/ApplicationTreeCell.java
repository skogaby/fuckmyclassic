package com.fuckmyclassic.ui.component;

import com.fuckmyclassic.management.AppImporter;
import com.fuckmyclassic.model.Folder;
import com.fuckmyclassic.model.LibraryItem;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;


/**
 * Class to encapsulate the logic for each cell in the TreeView
 * that shows the folders and applications.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class ApplicationTreeCell extends CheckBoxTreeCell<LibraryItem> {

    static Logger LOG = LogManager.getLogger(ApplicationTreeCell.class.getName());

    /** The path to the stylesheet for this cell */
    private static final String styleSheetPath = String.format("%s%c%s", "css", File.separatorChar, "MainWindow.css");
    /** CSS style class for a cell in the TreeView that's selected */
    private static final String SELECTED_CELL_STYLE_CLASS = "selected-cell";

    /** Helper class to import new games to the current library */
    private final AppImporter appImporter;

    /**
     * Constructor. Sets up the event handlers.
     */
    public ApplicationTreeCell(final AppImporter appImporter) {
        this.appImporter = appImporter;

        // set the event handlers for the cell
        setOnDragOver(event -> onDragOver(event));
        setOnDragDropped(event -> onDragDropped(event));
        setOnDragEntered(event -> onDragEntered(event));
        setOnDragExited(event -> onDragExited(event));

        // styling
        getStylesheets().add(styleSheetPath);
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
            if (((TreeCell<LibraryItem>) node).getTreeItem().getValue().equals(parentFolder)) {
                return (TreeCell<LibraryItem>) node;
            }
        }

        return null;
    }
}
