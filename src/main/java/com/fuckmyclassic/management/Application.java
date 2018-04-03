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
    private IntegerProperty numPlayers;
    private BooleanProperty hasSimultaneousMultiplayer;
    private ObjectProperty<LocalDate> releaseDate;
    private IntegerProperty saveCount;
    private StringProperty sortName;
    private StringProperty publisher;
    private StringProperty copyright;
    private LongProperty applicationSize;
    private BooleanProperty isCompressed;

    public Application() {
        this.commandLine = new SimpleStringProperty(null);
        this.savePath = new SimpleStringProperty(null);
        this.applicationName = new SimpleStringProperty(null);
        this.boxArtPath = new SimpleStringProperty(null);
        this.applicationId = new SimpleStringProperty(null);
        this.testId = new SimpleIntegerProperty(0);
        this.id = new SimpleIntegerProperty(0);
        this.numPlayers = new SimpleIntegerProperty(1);
        this.hasSimultaneousMultiplayer = new SimpleBooleanProperty(false);
        this.releaseDate = new SimpleObjectProperty<>(LocalDate.of(2017, 9, 29));
        this.saveCount = new SimpleIntegerProperty(0);
        this.sortName = new SimpleStringProperty(null);
        this.publisher = new SimpleStringProperty("fuckmyclassic 2018");
        this.copyright = new SimpleStringProperty("fuckmyclassic 2018");
        this.applicationSize = new SimpleLongProperty(0);
        this.isCompressed = new SimpleBooleanProperty(false);
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
        this.numPlayers = new SimpleIntegerProperty(numPlayers);
        this.hasSimultaneousMultiplayer = new SimpleBooleanProperty(hasSimultaneousMultiplayer);
        this.releaseDate = new SimpleObjectProperty<>(releaseDate);
        this.saveCount = new SimpleIntegerProperty(saveCount);
        this.sortName = new SimpleStringProperty(sortName);
        this.publisher = new SimpleStringProperty(publisher);
        this.copyright = new SimpleStringProperty(copyright);
        this.applicationSize = new SimpleLongProperty(applicationSize);
        this.isCompressed = new SimpleBooleanProperty(isCompressed);
    }

    public String getCommandLine() {
        return commandLine.get();
    }

    public Application setCommandLine(String commandLine) {
        this.commandLine = new SimpleStringProperty(commandLine);
        return this;
    }

    public String getSavePath() {
        return savePath.get();
    }

    public Application setSavePath(String savePath) {
        this.savePath = new SimpleStringProperty(savePath);
        return this;
    }

    public String getApplicationName() {
        return applicationName.get();
    }

    public Application setApplicationName(String applicationName) {
        this.applicationName = new SimpleStringProperty(applicationName);
        return this;
    }

    public String getBoxArtPath() {
        return boxArtPath.get();
    }

    public Application setBoxArtPath(String boxArtPath) {
        this.boxArtPath = new SimpleStringProperty(boxArtPath);
        return this;
    }

    public String getApplicationId() {
        return applicationId.get();
    }

    public Application setApplicationId(String applicationId) {
        this.applicationId = new SimpleStringProperty(applicationId);
        return this;
    }

    public int getTestId() {
        return testId.get();
    }

    public Application setTestId(int testId) {
        this.testId = new SimpleIntegerProperty(testId);
        return this;
    }

    public int getId() {
        return id.get();
    }

    public Application setId(int id) {
        this.id = new SimpleIntegerProperty(id);
        return this;
    }

    public int getNumPlayers() {
        return numPlayers.get();
    }

    public Application setNumPlayers(int numPlayers) {
        this.numPlayers = new SimpleIntegerProperty(numPlayers);
        return this;
    }

    public boolean isHasSimultaneousMultiplayer() {
        return hasSimultaneousMultiplayer.get();
    }

    public Application setHasSimultaneousMultiplayer(boolean hasSimultaneousMultiplayer) {
        this.hasSimultaneousMultiplayer = new SimpleBooleanProperty(hasSimultaneousMultiplayer);
        return this;
    }

    public LocalDate getReleaseDate() {
        return releaseDate.get();
    }

    public Application setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = new SimpleObjectProperty<>(releaseDate);
        return this;
    }

    public int getSaveCount() {
        return saveCount.get();
    }

    public Application setSaveCount(int saveCount) {
        this.saveCount = new SimpleIntegerProperty(saveCount);
        return this;
    }

    public String getSortName() {
        return sortName.get();
    }

    public Application setSortName(String sortName) {
        this.sortName = new SimpleStringProperty(sortName);
        return this;
    }

    public String getPublisher() {
        return publisher.get();
    }

    public Application setPublisher(String publisher) {
        this.publisher = new SimpleStringProperty(publisher);
        return this;
    }

    public String getCopyright() {
        return copyright.get();
    }

    public Application setCopyright(String copyright) {
        this.copyright = new SimpleStringProperty(copyright);
        return this;
    }

    public long getApplicationSize() {
        return applicationSize.get();
    }

    public Application setApplicationSize(long applicationSize) {
        this.applicationSize = new SimpleLongProperty(applicationSize);
        return this;
    }

    public boolean isCompressed() {
        return isCompressed.get();
    }

    public Application setCompressed(boolean compressed) {
        isCompressed = new SimpleBooleanProperty(compressed);
        return this;
    }

    public String toString() {
        return this.applicationName.get();
    }

    public StringProperty commandLineProperty() {
        return commandLine;
    }

    public StringProperty savePathProperty() {
        return savePath;
    }

    public StringProperty applicationNameProperty() {
        return applicationName;
    }

    public StringProperty boxArtPathProperty() {
        return boxArtPath;
    }

    public StringProperty applicationIdProperty() {
        return applicationId;
    }

    public IntegerProperty testIdProperty() {
        return testId;
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public IntegerProperty numPlayersProperty() {
        return numPlayers;
    }

    public BooleanProperty hasSimultaneousMultiplayerProperty() {
        return hasSimultaneousMultiplayer;
    }

    public ObjectProperty<LocalDate> releaseDateProperty() {
        return releaseDate;
    }

    public IntegerProperty saveCountProperty() {
        return saveCount;
    }

    public StringProperty sortNameProperty() {
        return sortName;
    }

    public StringProperty publisherProperty() {
        return publisher;
    }

    public StringProperty copyrightProperty() {
        return copyright;
    }

    public LongProperty applicationSizeProperty() {
        return applicationSize;
    }

    public boolean isIsCompressed() {
        return isCompressed.get();
    }

    public BooleanProperty isCompressedProperty() {
        return isCompressed;
    }
}
