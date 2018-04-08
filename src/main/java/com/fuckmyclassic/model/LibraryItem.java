package com.fuckmyclassic.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.CascadeType;
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
 * Class to represent an item in a library. Each library is tied to a particular
 * console (hardware ID) and can contain multiple applications and folders.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Entity
@Table(name = "libraries")
public class LibraryItem implements Externalizable {

    private static final long serialVersionUID = 1L;

    private LongProperty id;
    private StringProperty consoleSid;
    private IntegerProperty libraryId;
    private Application application;
    private Folder folder;

    public LibraryItem() {
        this.id = new SimpleLongProperty(this, "id");
        this.consoleSid = new SimpleStringProperty(null);
        this.libraryId = new SimpleIntegerProperty(0);
    }

    public LibraryItem(final String consoleSid, final int libraryId, final Application application,
                       final Folder folder) {
        this.id = new SimpleLongProperty(this, "id");
        this.consoleSid = new SimpleStringProperty(consoleSid);
        this.libraryId = new SimpleIntegerProperty(libraryId);
        this.application = application;
        this.folder = folder;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(getId());
        out.writeUTF(getConsoleSid());
        out.writeInt(getLibraryId());
        out.writeObject(getApplication());
        out.writeObject(getFolder());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        setId(in.readLong());
        setConsoleSid(in.readUTF());
        setLibraryId(in.readInt());
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

    @Column(name = "console_sid")
    public String getConsoleSid() {
        return consoleSid.get();
    }

    public StringProperty consoleSidProperty() {
        return consoleSid;
    }

    public LibraryItem setConsoleSid(String consoleSid) {
        this.consoleSid.set(consoleSid);
        return this;
    }

    @Column(name = "library_id")
    public int getLibraryId() {
        return libraryId.get();
    }

    public IntegerProperty libraryIdProperty() {
        return libraryId;
    }

    public LibraryItem setLibraryId(int libraryId) {
        this.libraryId.set(libraryId);
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
