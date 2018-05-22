package com.fuckmyclassic.model;

import com.fuckmyclassic.util.FileUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import static com.fuckmyclassic.shared.SharedConstants.SIZE_DICT;

/**
 * Class to represent an item in a library. Each item belongs to
 * a particular library, has an application, and has a parent folder.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Entity
@Table(name = "library_items")
public class LibraryItem implements Externalizable {

    private static final long serialVersionUID = 1L;

    private LongProperty id;
    private Library library;
    private Application application;
    private Folder folder;
    private BooleanProperty selected;
    private int numNodes;
    private long treeFilesize;
    private StringProperty treeFilesizeString;

    public LibraryItem() {
        this.id = new SimpleLongProperty(this, "id");
        this.selected = new SimpleBooleanProperty(true);
        this.numNodes = 0;
        this.treeFilesize = 0;
        this.treeFilesizeString = new SimpleStringProperty(null);
    }

    public LibraryItem(final LibraryItem other) {
        this.id = new SimpleLongProperty(this, "id");
        this.library = other.library;
        this.application = other.application;
        this.folder = other.folder;
        this.selected = other.selected;
        this.numNodes = other.numNodes;
        this.treeFilesize = other.treeFilesize;
        this.treeFilesizeString = other.treeFilesizeString;
    }

    public LibraryItem(final Library library, final Application application, final Folder folder, final boolean selected,
                       final int numNodes, final long treeFilesize) {
        this.id = new SimpleLongProperty(this, "id");
        this.library = library;
        this.application = application;
        this.folder = folder;
        this.selected = new SimpleBooleanProperty(selected);
        this.numNodes = numNodes;
        this.treeFilesize = treeFilesize;
        this.treeFilesizeString = new SimpleStringProperty(FileUtils.convertToHumanReadable(treeFilesize));
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(getId());
        out.writeObject(getLibrary());
        out.writeObject(getApplication());
        out.writeObject(getFolder());
        out.writeBoolean(isSelected());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        setId(in.readLong());
        setLibrary((Library)in.readObject());
        setApplication((Application)in.readObject());
        setFolder((Folder)in.readObject());
        setSelected(in.readBoolean());
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    public long getId() {
        return id.get();
    }

    public LongProperty idProperty() {
        return id;
    }

    public LibraryItem setId(long id) {
        this.id.set(id);
        return this;
    }

    @ManyToOne
    @JoinColumn(name = "library_id")
    public Library getLibrary() {
        return library;
    }

    public LibraryItem setLibrary(Library library) {
        this.library = library;
        return this;
    }

    @ManyToOne
    @JoinColumn(name = "application_id")
    public Application getApplication() {
        return application;
    }

    public LibraryItem setApplication(Application application) {
        this.application = application;
        return this;
    }

    @ManyToOne
    @JoinColumn(name = "folder_id")
    public Folder getFolder() {
        return folder;
    }

    public LibraryItem setFolder(Folder folder) {
        this.folder = folder;
        return this;
    }

    @Column(name = "is_selected")
    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public LibraryItem setSelected(boolean selected) {
        this.selected.set(selected);
        return this;
    }

    @Transient
    public int getNumNodes() {
        return numNodes;
    }

    public LibraryItem setNumNodes(int numNodes) {
        this.numNodes = numNodes;
        return this;
    }

    @Transient
    public long getTreeFilesize() {
        return treeFilesize;
    }

    public LibraryItem setTreeFilesize(long treeFilesize) {
        this.treeFilesize = treeFilesize;
        this.treeFilesizeString.setValue(FileUtils.convertToHumanReadable(treeFilesize));

        return this;
    }

    @Transient
    public String getTreeFilesizeString() {
        return treeFilesizeString.get();
    }

    public StringProperty treeFilesizeStringProperty() {
        return treeFilesizeString;
    }

    @Override
    public String toString() {
        return this.application.getApplicationName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibraryItem that = (LibraryItem) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(library, that.library);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, library);
    }
}
