package com.fuckmyclassic.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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

    public LibraryItem() {
        this.id = new SimpleLongProperty(this, "id");
    }

    public LibraryItem(final Library library, final Application application, final Folder folder) {
        this.id = new SimpleLongProperty(this, "id");
        this.library = library;
        this.application = application;
        this.folder = folder;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(getId());
        out.writeObject(getLibrary());
        out.writeObject(getApplication());
        out.writeObject(getFolder());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        setId(in.readLong());
        setLibrary((Library)in.readObject());
        setApplication((Application)in.readObject());
        setFolder((Folder)in.readObject());
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
}
