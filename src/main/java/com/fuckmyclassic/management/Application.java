package com.fuckmyclassic.management;

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

import java.time.LocalDate;

/**
 * Class to represent a game or app that runs on the NES/SNES Mini.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class Application {

    private StringProperty commandLine;
    private StringProperty savePath;
    private StringProperty applicationName;
    private StringProperty boxArtPath;
    private StringProperty applicationId;
    private IntegerProperty testId;
    private IntegerProperty id;
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
        this.commandLine = new SimpleStringProperty(null);
        this.savePath = new SimpleStringProperty(null);
        this.applicationName = new SimpleStringProperty(null);
        this.boxArtPath = new SimpleStringProperty(null);
        this.applicationId = new SimpleStringProperty(null);
        this.testId = new SimpleIntegerProperty(0);
        this.id = new SimpleIntegerProperty(0);
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
                       final String boxArtPath, final String savePath, final int testId, final int id,
                       final int numPlayers, final boolean hasSimultaneousMultiplayer, final LocalDate releaseDate,
                       final int saveCount, final String sortName, final String publisher, final String copyright,
                       final long applicationSize, final boolean isCompressed) {
        this.commandLine = new SimpleStringProperty(commandLine);
        this.savePath = new SimpleStringProperty(savePath);
        this.applicationName = new SimpleStringProperty(applicationName);
        this.boxArtPath = new SimpleStringProperty(boxArtPath);
        this.applicationId = new SimpleStringProperty(applicationId);
        this.testId = new SimpleIntegerProperty(testId);
        this.id = new SimpleIntegerProperty(id);
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

    public String toString() {
        return this.applicationName.get();
    }

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

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public Application setId(int id) {
        this.id.set(id);
        return this;
    }

    public boolean isSinglePlayer() {
        return singlePlayer.get();
    }

    public BooleanProperty singlePlayerProperty() {
        return singlePlayer;
    }

    public Application setSinglePlayer(boolean singlePlayer) {
        this.singlePlayer.set(singlePlayer);
        this.simultaneousMultiplayer.set(false);
        this.nonSimultaneousMultiplayer.set(false);
        return this;
    }

    public boolean getNonSimultaneousMultiplayer() {
        return nonSimultaneousMultiplayer.get();
    }

    public BooleanProperty nonSimultaneousMultiplayerProperty() {
        return nonSimultaneousMultiplayer;
    }

    public Application setNonSimultaneousMultiplayer(boolean nonSimultaneousMultiplayer) {
        this.singlePlayer.set(false);
        this.simultaneousMultiplayer.set(false);
        this.nonSimultaneousMultiplayer.set(nonSimultaneousMultiplayer);
        return this;
    }

    public boolean isSimultaneousMultiplayer() {
        return simultaneousMultiplayer.get();
    }

    public BooleanProperty simultaneousMultiplayerProperty() {
        return simultaneousMultiplayer;
    }

    public Application setSimultaneousMultiplayer(boolean simultaneousMultiplayer) {
        this.singlePlayer.set(false);
        this.simultaneousMultiplayer.set(simultaneousMultiplayer);
        this.nonSimultaneousMultiplayer.set(false);
        return this;
    }

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
