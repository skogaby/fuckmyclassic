package com.fuckmyclassic.controller.util;

import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Folder;
import javafx.scene.control.TreeCell;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Class to encapsulate the logic for each cell in the TreeView
 * that shows the folders and applications.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class ApplicationTreeCell extends TreeCell<Application> {

    static Logger LOG = LogManager.getLogger(ApplicationTreeCell.class.getName());

    /**
     * Helper class to import new games to the current library
     */
    private final AppImporter appImporter;

    /**
     * Constructor. Sets up the event handlers.
     */
    public ApplicationTreeCell(final AppImporter appImporter) {
        this.appImporter = appImporter;

        // this event is fired if something is dragged over the cell
        setOnDragOver(event -> {
            Dragboard db = event.getDragboard();

            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                event.consume();
            }
        });

        // this event is fired if something is dropped onto the cell
        setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                success = true;
                String filePath;

                //final TreeItem<Application> item = getTreeItem();
                final Application app = getItem();
                //final TreeItem<Application> parentItem = (app instanceof Folder) ? item : item.getParent();

                // if they're dragging files onto a folder, create a new app
                if (app instanceof Folder) {
                    final Application newApp = this.appImporter.createAndImportApplicationFromFiles(db.getFiles(), getTreeItem(), this);
                } else {
                // else if they're dragging files onto an existing game, just add
                // the files to the game (in case there are multiple files they need to add)
                // need to prompt them to make sure this is the desired behavior
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    /**
     * Called whenever this cell is recycled to show a new item.
     * @param item The item to show
     * @param empty Whether the cell is empty
     */
    @Override
    protected void updateItem(Application item, boolean empty) {
        super.updateItem(item, empty);

        // handle the display
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.getApplicationName());
        }
    }
}
