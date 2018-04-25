package com.fuckmyclassic.ui.util;

import com.fuckmyclassic.hibernate.HibernateManager;
import com.fuckmyclassic.model.Folder;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.ui.component.UiPropertyContainer;
import javafx.scene.control.CheckBoxTreeItem;

/**
 * Small helper class to setup listeners and event handlers on
 * CheckBoxTreeItems since we instantiate these from multiple places
 */
public class CheckBoxTreeItemUtils {

    /**
     * Creates the checkbox listeners for the tree items that update the entity's
     * selection status and also update the UI's view of number of selected games.
     * @param item The item to set the listeners on
     * @param hibernateManager The HibernateManager for persisting entity updates
     * @param uiPropertyContainer The UI property container for showing number of selected games
     */
    public static void setCheckListenerOnTreeItem(final CheckBoxTreeItem<LibraryItem> item,
                                                  final HibernateManager hibernateManager,
                                                  final UiPropertyContainer uiPropertyContainer) {
        item.addEventHandler(CheckBoxTreeItem.<LibraryItem> checkBoxSelectionChangedEvent(), (event -> {
            final CheckBoxTreeItem<LibraryItem> treeItem = event.getTreeItem();
            final LibraryItem libraryItem = treeItem.getValue();
            boolean oldVal = libraryItem.isSelected();
            boolean newVal = treeItem.isSelected() || treeItem.isIndeterminate();

            if (newVal != oldVal) {
                libraryItem.setSelected(newVal);
                hibernateManager.updateEntity(libraryItem);

                // only update the counts for games, not folders
                if (!(libraryItem.getApplication() instanceof Folder)) {
                    if (newVal) {
                        uiPropertyContainer.numSelected.set(
                                uiPropertyContainer.numSelected.get() + 1L);
                    } else {
                        uiPropertyContainer.numSelected.set(
                                uiPropertyContainer.numSelected.get() - 1L);
                    }
                }
            }
        }));
    }
}
