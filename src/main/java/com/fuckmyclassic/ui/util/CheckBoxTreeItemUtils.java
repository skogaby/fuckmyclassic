package com.fuckmyclassic.ui.util;

import com.fuckmyclassic.hibernate.dao.impl.LibraryItemDAO;
import com.fuckmyclassic.model.Folder;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.ui.component.UiPropertyContainer;
import com.fuckmyclassic.userconfig.UserConfiguration;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;

/**
 * Small helper class to setup listeners and event handlers on
 * CheckBoxTreeItems since we instantiate these from multiple places
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class CheckBoxTreeItemUtils {

    /**
     * Creates the checkbox listeners for the tree items that update the entity's
     * selection status and also update the UI's view of number of selected games.
     * @param item The item to set the listeners on
     * @param libraryItemDAO The DAO for persisting library updates
     * @param uiPropertyContainer The UI property container for showing number of selected games
     * @param userConfiguration Configuration about the current session
     */
    public static void setCheckListenerOnTreeItem(final CheckBoxTreeItem<LibraryItem> item,
                                                  final LibraryItemDAO libraryItemDAO,
                                                  final UiPropertyContainer uiPropertyContainer,
                                                  final UserConfiguration userConfiguration) {
        item.addEventHandler(CheckBoxTreeItem.<LibraryItem> checkBoxSelectionChangedEvent(), (event -> {
            final CheckBoxTreeItem<LibraryItem> treeItem = event.getTreeItem();
            final LibraryItem libraryItem = treeItem.getValue();
            boolean oldVal = libraryItem.isSelected();
            boolean newVal = treeItem.isSelected() || treeItem.isIndeterminate();

            if (newVal != oldVal) {
                libraryItem.setSelected(newVal);
                libraryItemDAO.update(libraryItem);

                // only update the counts for games, not folders
                if (!(libraryItem.getApplication() instanceof Folder)) {
                    if (newVal) {
                        uiPropertyContainer.numSelected.set(
                                uiPropertyContainer.numSelected.get() + 1L);
                    } else {
                        uiPropertyContainer.numSelected.set(
                                uiPropertyContainer.numSelected.get() - 1L);
                    }

                    long size = libraryItem.getApplication().getApplicationSize();

                    // update the parent file tree sizes
                    if (!newVal) {
                        size *= -1;
                    }

                    TreeItem<LibraryItem> parent = treeItem;

                    while (parent.getParent() != null) {
                        parent = parent.getParent();
                        parent.getValue().setTreeFilesize(parent.getValue().getTreeFilesize() + size);
                    }

                    // update the space usage
                    if (userConfiguration.getSelectedConsole().getSpaceForGames() != 0) {
                        uiPropertyContainer.gameSpaceUsed.setValue(
                                (double) parent.getValue().getTreeFilesize() /
                                (double) userConfiguration.getSelectedConsole().getSpaceForGames());
                    }
                }
            }
        }));
    }
}
