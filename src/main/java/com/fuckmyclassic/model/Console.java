package com.fuckmyclassic.model;

import com.fuckmyclassic.shared.SharedConstants;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

/**
 * Simple model to represent a console we've seen before.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Entity
@Table(name = "consoles")
public class Console implements Externalizable {

    private static final long serialVersionUID = 1L;

    private LongProperty id;
    private StringProperty consoleSid;
    private ObjectProperty<ConsoleType> consoleType;
    private StringProperty consoleSyncPath;
    private StringProperty nickname;
    private StringProperty lastKnownAddress;
    private long spaceForGames;

    public Console() {
        this.id = new SimpleLongProperty(this, "id");
        this.consoleSid = new SimpleStringProperty(null);
        this.consoleType = new SimpleObjectProperty<>(null);
        this.consoleSyncPath = new SimpleStringProperty(null);
        this.nickname = new SimpleStringProperty(null);
        this.lastKnownAddress = new SimpleStringProperty(null);
        this.spaceForGames = 0L;
    }

    public Console(final String consoleSid,
                   final ConsoleType consoleType,
                   final String consoleSyncPath,
                   final String nickname,
                   final String lastKnownAddress) {
        this.id = new SimpleLongProperty(this, "id");
        this.consoleSid = new SimpleStringProperty(consoleSid);
        this.consoleType = new SimpleObjectProperty<>(consoleType);
        this.consoleSyncPath = new SimpleStringProperty(consoleSyncPath);
        this.nickname = new SimpleStringProperty(nickname);
        this.lastKnownAddress = new SimpleStringProperty(lastKnownAddress);
        this.spaceForGames = 0L;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(getId());
        out.writeUTF(getConsoleSid());
        out.writeObject(getConsoleType());
        out.writeUTF(getConsoleSyncPath());
        out.writeUTF(getNickname());
        out.writeUTF(getLastKnownAddress());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        setId(in.readLong());
        setConsoleSid(in.readUTF());
        setConsoleType((ConsoleType) in.readObject());
        setConsoleSyncPath(in.readUTF());
        setNickname(in.readUTF());
        setLastKnownAddress(in.readUTF());
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

    public Console setId(long id) {
        this.id.set(id);
        return this;
    }

    @NaturalId
    @Column(name = "console_sid", unique = true)
    public String getConsoleSid() {
        return consoleSid.get();
    }

    public StringProperty consoleSidProperty() {
        return consoleSid;
    }

    public Console setConsoleSid(String consoleSid) {
        this.consoleSid.set(consoleSid);
        return this;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "console_type")
    public ConsoleType getConsoleType() {
        return consoleType.get();
    }

    public ObjectProperty<ConsoleType> consoleTypeProperty() {
        return consoleType;
    }

    public Console setConsoleType(ConsoleType consoleType) {
        this.consoleType.set(consoleType);
        return this;
    }

    @Column(name = "console_sync_path")
    public String getConsoleSyncPath() {
        return consoleSyncPath.get();
    }

    public StringProperty consoleSyncPathProperty() {
        return consoleSyncPath;
    }

    public Console setConsoleSyncPath(String consoleSyncPath) {
        this.consoleSyncPath.set(consoleSyncPath);
        return this;
    }

    @Column(name = "console_nickname")
    public String getNickname() {
        return nickname.get();
    }

    public StringProperty nicknameProperty() {
        return nickname;
    }

    public Console setNickname(String nickname) {
        this.nickname.set(nickname);
        return this;
    }

    @Column(name = "last_known_address")
    public String getLastKnownAddress() {
        return lastKnownAddress.get();
    }

    public StringProperty lastKnownAddressProperty() {
        return lastKnownAddress;
    }

    public Console setLastKnownAddress(String lastKnownAddress) {
        this.lastKnownAddress.set(lastKnownAddress);
        return this;
    }

    @Transient
    public long getSpaceForGames() {
        return spaceForGames;
    }

    public Console setSpaceForGames(long spaceForGames) {
        this.spaceForGames = spaceForGames;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Console console = (Console) o;
        return consoleSid.equals(console.consoleSid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, consoleSid);
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getNickname(), getConsoleType() == null ?
                SharedConstants.DEFAULT_CONSOLE_SID : getConsoleType().getConsoleCode());
    }
}
