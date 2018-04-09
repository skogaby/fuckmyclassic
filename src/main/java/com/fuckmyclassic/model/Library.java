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
import javax.persistence.Table;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Class to represent the metadata about a library.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Entity
@Table(name = "libraries")
public class Library implements Externalizable {

    private static final long serialVersionUID = 1L;

    private LongProperty id;
    private StringProperty consoleSid;
    private IntegerProperty libraryId;
    private StringProperty libraryName;

    public Library() {
        this.id = new SimpleLongProperty(this, "id");
        this.consoleSid = new SimpleStringProperty(null);
        this.libraryId = new SimpleIntegerProperty(-1);
        this.libraryName = new SimpleStringProperty(null);
    }

    public Library(final String consoleSid, final int libraryId, final String libraryName) {
        this.id = new SimpleLongProperty(this, "id");
        this.consoleSid = new SimpleStringProperty(consoleSid);
        this.libraryId = new SimpleIntegerProperty(libraryId);
        this.libraryName = new SimpleStringProperty(libraryName);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(getId());
        out.writeUTF(getConsoleSid());
        out.writeInt(getLibraryId());
        out.writeUTF(getLibraryName());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        setId(in.readLong());
        setConsoleSid(in.readUTF());
        setLibraryId(in.readInt());
        setLibraryName(in.readUTF());
    }

    @Override
    public String toString() {
        return getLibraryName();
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

    public Library setId(long id) {
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

    public Library setConsoleSid(String consoleSid) {
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

    public Library setLibraryId(int libraryId) {
        this.libraryId.set(libraryId);
        return this;
    }

    @Column(name = "library_name")
    public String getLibraryName() {
        return libraryName.get();
    }

    public StringProperty libraryNameProperty() {
        return libraryName;
    }

    public Library setLibraryName(String libraryName) {
        this.libraryName.set(libraryName);
        return this;
    }
}
