package com.fuckmyclassic.controller.util;

import com.fuckmyclassic.model.Application;
import javafx.scene.control.TreeCell;

/**
 * Class to encapsulate the logic for each cell in the TreeView
 * that shows the folders and applications.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class ApplicationTreeCell extends TreeCell<Application> {

    public ApplicationTreeCell() {

    }

    @Override
    protected void updateItem(Application item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.getApplicationName());
        }
    }
}
