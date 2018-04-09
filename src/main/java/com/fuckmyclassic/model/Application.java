package com.fuckmyclassic.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.LocalDate;

/**
 * Class to represent a game or app that runs on the NES/SNES Mini.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "games_and_folders")
public class Application implements Externalizable {

    private static final long serialVersionUID = 1L;

    private LongProperty id;
    private StringProperty commandLine;
    private StringProperty savePath;
    private StringProperty applicationName;
    private StringProperty boxArtPath;
    private StringProperty applicationId;
    private IntegerProperty testId;
    private IntegerProperty canoeId;
    private BooleanProperty singlePlayer;
    private BooleanProperty nonSimultaneousMultiplayer;
    private BooleanProperty simultaneousMultiplayer;
    private ObjectProperty<LocalDate> releaseDate;
    private IntegerProperty saveCount;
    private StringProperty sortName;
    private StringProperty publisher;
    private StringProperty copyright;
    private LongProperty applicationSize;
    private BooleanProperty compressed;

    public Application() {
        this.id = new SimpleLongProperty(this, "id");
        this.commandLine = new SimpleStringProperty(null);
        this.savePath = new SimpleStringProperty(null);
        this.applicationName = new SimpleStringProperty(null);
        this.boxArtPath = new SimpleStringProperty(null);
        this.applicationId = new SimpleStringProperty(null);
        this.testId = new SimpleIntegerProperty(0);
        this.canoeId = new SimpleIntegerProperty(0);
        this.singlePlayer = new SimpleBooleanProperty(true);
        this.nonSimultaneousMultiplayer = new SimpleBooleanProperty(false);
        this.simultaneousMultiplayer = new SimpleBooleanProperty(false);
        this.releaseDate = new SimpleObjectProperty<>(LocalDate.of(2017, 9, 29));
        this.saveCount = new SimpleIntegerProperty(0);
        this.sortName = new SimpleStringProperty(null);
        this.publisher = new SimpleStringProperty("fuckmyclassic 2018");
        this.copyright = new SimpleStringProperty("fuckmyclassic 2018");
        this.applicationSize = new SimpleLongProperty(0);
        this.compressed = new SimpleBooleanProperty(false);
    }

    public Application(final String applicationId, final String applicationName, final String commandLine,
                       final String boxArtPath, final String savePath, final int testId, final int canoeId,
                       final int numPlayers, final boolean hasSimultaneousMultiplayer, final LocalDate releaseDate,
                       final int saveCount, final String sortName, final String publisher, final String copyright,
                       final long applicationSize, final boolean isCompressed) {
        this.id = new SimpleLongProperty(this, "id");
        this.commandLine = new SimpleStringProperty(commandLine);
        this.savePath = new SimpleStringProperty(savePath);
        this.applicationName = new SimpleStringProperty(applicationName);
        this.boxArtPath = new SimpleStringProperty(boxArtPath);
        this.applicationId = new SimpleStringProperty(applicationId);
        this.testId = new SimpleIntegerProperty(testId);
        this.canoeId = new SimpleIntegerProperty(canoeId);
        this.singlePlayer = new SimpleBooleanProperty(numPlayers <= 1);
        this.nonSimultaneousMultiplayer = new SimpleBooleanProperty(numPlayers > 1 && !hasSimultaneousMultiplayer);
        this.simultaneousMultiplayer = new SimpleBooleanProperty(numPlayers > 1 && hasSimultaneousMultiplayer);
        this.releaseDate = new SimpleObjectProperty<>(releaseDate);
        this.saveCount = new SimpleIntegerProperty(saveCount);
        this.sortName = new SimpleStringProperty(sortName);
        this.publisher = new SimpleStringProperty(publisher);
        this.copyright = new SimpleStringProperty(copyright);
        this.applicationSize = new SimpleLongProperty(applicationSize);
        this.compressed = new SimpleBooleanProperty(isCompressed);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(getId());
        out.writeUTF(getCommandLine());
        out.writeUTF(getSavePath());
        out.writeUTF(getApplicationName());
        out.writeUTF(getBoxArtPath());
        out.writeUTF(getApplicationId());
        out.writeInt(getTestId());
        out.writeInt(getCanoeId());
        out.writeBoolean(isSinglePlayer());
        out.writeBoolean(getNonSimultaneousMultiplayer());
        out.writeBoolean(isSimultaneousMultiplayer());
        out.writeObject(getReleaseDate());
        out.writeInt(getSaveCount());
        out.writeUTF(getSortName());
        out.writeUTF(getPublisher());
        out.writeUTF(getCopyright());
        out.writeLong(getApplicationSize());
        out.writeBoolean(isCompressed());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        setId(in.readLong());
        setCommandLine(in.readUTF());
        setSavePath(in.readUTF());
        setApplicationName(in.readUTF());
        setBoxArtPath(in.readUTF());
        setApplicationId(in.readUTF());
        setTestId(in.readInt());
        setCanoeId(in.readInt());
        setSinglePlayer(in.readBoolean());
        setNonSimultaneousMultiplayer(in.readBoolean());
        setSimultaneousMultiplayer(in.readBoolean());
        setReleaseDate((LocalDate)in.readObject());
        setSaveCount(in.readInt());
        setSortName(in.readUTF());
        setPublisher(in.readUTF());
        setCopyright(in.readUTF());
        setApplicationSize(in.readLong());
        setCompressed(in.readBoolean());
    }

    public String toString() {
        return this.applicationName.get();
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

    public void setId(long id) {
        this.id.set(id);
    }

    @NaturalId
    @Column(name = "application_id", unique = true)
    public String getApplicationId() {
        return applicationId.get();
    }

    public StringProperty applicationIdProperty() {
        return applicationId;
    }

    public Application setApplicationId(String applicationId) {
        this.applicationId.set(applicationId);
        return this;
    }

    @Column(name = "command_line")
    public String getCommandLine() {
        return commandLine.get();
    }

    public StringProperty commandLineProperty() {
        return commandLine;
    }

    public Application setCommandLine(String commandLine) {
        this.commandLine.set(commandLine);
        return this;
    }

    @Column(name = "save_path")
    public String getSavePath() {
        return savePath.get();
    }

    public StringProperty savePathProperty() {
        return savePath;
    }

    public Application setSavePath(String savePath) {
        this.savePath.set(savePath);
        return this;
    }

    @Column(name = "application_name")
    public String getApplicationName() {
        return applicationName.get();
    }

    public StringProperty applicationNameProperty() {
        return applicationName;
    }

    public Application setApplicationName(String applicationName) {
        this.applicationName.set(applicationName);
        return this;
    }

    @Column(name = "boxart_path")
    public String getBoxArtPath() {
        return boxArtPath.get();
    }

    public StringProperty boxArtPathProperty() {
        return boxArtPath;
    }

    public Application setBoxArtPath(String boxArtPath) {
        this.boxArtPath.set(boxArtPath);
        return this;
    }

    @Column(name = "test_id")
    public int getTestId() {
        return testId.get();
    }

    public IntegerProperty testIdProperty() {
        return testId;
    }

    public Application setTestId(int testId) {
        this.testId.set(testId);
        return this;
    }

    @Column(name = "canoe_id")
    public int getCanoeId() {
        return canoeId.get();
    }

    public IntegerProperty canoeIdProperty() {
        return canoeId;
    }

    public void setCanoeId(int canoeId) {
        this.canoeId.set(canoeId);
    }

    @Column(name = "is_single_player")
    public boolean isSinglePlayer() {
        return singlePlayer.get();
    }

    public BooleanProperty singlePlayerProperty() {
        return singlePlayer;
    }

    public Application setSinglePlayer(boolean singlePlayer) {
        this.singlePlayer.set(singlePlayer);
        return this;
    }

    @Column(name = "is_non_simul_multiplayer")
    public boolean getNonSimultaneousMultiplayer() {
        return nonSimultaneousMultiplayer.get();
    }

    public BooleanProperty nonSimultaneousMultiplayerProperty() {
        return nonSimultaneousMultiplayer;
    }

    public Application setNonSimultaneousMultiplayer(boolean nonSimultaneousMultiplayer) {
        this.nonSimultaneousMultiplayer.set(nonSimultaneousMultiplayer);
        return this;
    }

    @Column(name = "is_simul_multiplayer")
    public boolean isSimultaneousMultiplayer() {
        return simultaneousMultiplayer.get();
    }

    public BooleanProperty simultaneousMultiplayerProperty() {
        return simultaneousMultiplayer;
    }

    public Application setSimultaneousMultiplayer(boolean simultaneousMultiplayer) {
        this.simultaneousMultiplayer.set(simultaneousMultiplayer);
        return this;
    }

    @Column(name = "release_date")
    public LocalDate getReleaseDate() {
        return releaseDate.get();
    }

    public ObjectProperty<LocalDate> releaseDateProperty() {
        return releaseDate;
    }

    public Application setReleaseDate(LocalDate releaseDate) {
        this.releaseDate.set(releaseDate);
        return this;
    }

    @Column(name = "save_count")
    public int getSaveCount() {
        return saveCount.get();
    }

    public IntegerProperty saveCountProperty() {
        return saveCount;
    }

    public Application setSaveCount(int saveCount) {
        this.saveCount.set(saveCount);
        return this;
    }

    @Column(name = "application_sort_name")
    public String getSortName() {
        return sortName.get();
    }

    public StringProperty sortNameProperty() {
        return sortName;
    }

    public Application setSortName(String sortName) {
        this.sortName.set(sortName);
        return this;
    }

    @Column(name = "publisher")
    public String getPublisher() {
        return publisher.get();
    }

    public StringProperty publisherProperty() {
        return publisher;
    }

    public Application setPublisher(String publisher) {
        this.publisher.set(publisher);
        return this;
    }

    @Column(name = "copyright")
    public String getCopyright() {
        return copyright.get();
    }

    public StringProperty copyrightProperty() {
        return copyright;
    }

    public Application setCopyright(String copyright) {
        this.copyright.set(copyright);
        return this;
    }

    @Column(name = "application_size")
    public long getApplicationSize() {
        return applicationSize.get();
    }

    public LongProperty applicationSizeProperty() {
        return applicationSize;
    }

    public Application setApplicationSize(long applicationSize) {
        this.applicationSize.set(applicationSize);
        return this;
    }

    @Column(name = "is_compressed")
    public boolean isCompressed() {
        return compressed.get();
    }

    public BooleanProperty compressedProperty() {
        return compressed;
    }

    public Application setCompressed(boolean compressed) {
        this.compressed.set(compressed);
        return this;
    }
}