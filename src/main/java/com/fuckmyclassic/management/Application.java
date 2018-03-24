package com.fuckmyclassic.management;

import java.time.LocalDate;

/**
 * Class to represent a game or app that runs on the NES/SNES Mini.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class Application {

    /**
     * The actual command to run this application on the console.
     */
    private String commandLine;

    /**
     * The path on the console where the saves for this application live.
     */
    private String savePath;

    /**
     * The name of the application that gets displayed on the console.
     */
    private String applicationName;

    /**
     * The path (locally) to the box art. This will get transformed as appropriate in the
     * generated <code>.desktop</code> file that gets generated during game sync.
     */
    private String boxArtPath;

    /**
     * The application ID that we generate for the folder name. Example: <code>CLV-S-IANKC</code>
     */
    private String applicationId;

    /**
     * The TestID value in the <code>.desktop</code> file.
     */
    private int testId;

    /**
     * The ID value in the <code>.desktop</code> file.
     */
    private int id;

    /**
     * The number of players this game supports (1-4).
     */
    private int numPlayers;

    /**
     * Whether or not the game supports simultaneous multiplayer.
     */
    private boolean hasSimultaneousMultiplayer;

    /**
     * The release date for the game.
     */
    private LocalDate releaseDate;

    /**
     * The number of save states present for the application.
     */
    private int saveCount;

    /**
     * The string used to sort this game in the main menu.
     */
    private String sortName;

    /**
     * The publisher for the application.
     */
    private String publisher;

    /**
     * The size of the application.
     */
    private long applicationSize;

    /**
     * Whether or not to use compression for the application.
     */
    private boolean isCompressed;

    public Application() {
        this.testId = 0;
        this.id = 0;
        this.numPlayers = 1;
        this.hasSimultaneousMultiplayer = false;
        this.releaseDate = LocalDate.of(2017, 9, 29);
        this.saveCount = 0;
        this.publisher = "fuckmyclassic 2018";
        this.isCompressed = false;
    }

    public Application(final String commandLine, final String savePath, final String applicationName,
                       final String boxArtPath, final String applicationId, final int testId, final int id,
                       final int numPlayers, final boolean hasSimultaneousMultiplayer, final LocalDate releaseDate,
                       final int saveCount, final String sortName, final String publisher, final long applicationSize,
                       final boolean isCompressed) {
        this.commandLine = commandLine;
        this.savePath = savePath;
        this.applicationName = applicationName;
        this.boxArtPath = boxArtPath;
        this.applicationId = applicationId;
        this.testId = testId;
        this.id = id;
        this.numPlayers = numPlayers;
        this.hasSimultaneousMultiplayer = hasSimultaneousMultiplayer;
        this.releaseDate = releaseDate;
        this.saveCount = saveCount;
        this.sortName = sortName;
        this.publisher = publisher;
        this.applicationSize = applicationSize;
        this.isCompressed = isCompressed;
    }

    public String getCommandLine() {
        return commandLine;
    }

    public Application setCommandLine(String commandLine) {
        this.commandLine = commandLine;
        return this;
    }

    public String getSavePath() {
        return savePath;
    }

    public Application setSavePath(String savePath) {
        this.savePath = savePath;
        return this;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public Application setApplicationName(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    public String getBoxArtPath() {
        return boxArtPath;
    }

    public Application setBoxArtPath(String boxArtPath) {
        this.boxArtPath = boxArtPath;
        return this;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public Application setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public int getTestId() {
        return testId;
    }

    public Application setTestId(int testId) {
        this.testId = testId;
        return this;
    }

    public int getId() {
        return id;
    }

    public Application setId(int id) {
        this.id = id;
        return this;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public Application setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
        return this;
    }

    public boolean isHasSimultaneousMultiplayer() {
        return hasSimultaneousMultiplayer;
    }

    public Application setHasSimultaneousMultiplayer(boolean hasSimultaneousMultiplayer) {
        this.hasSimultaneousMultiplayer = hasSimultaneousMultiplayer;
        return this;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public Application setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
        return this;
    }

    public int getSaveCount() {
        return saveCount;
    }

    public Application setSaveCount(int saveCount) {
        this.saveCount = saveCount;
        return this;
    }

    public String getSortName() {
        return sortName;
    }

    public Application setSortName(String sortName) {
        this.sortName = sortName;
        return this;
    }

    public String getPublisher() {
        return publisher;
    }

    public Application setPublisher(String publisher) {
        this.publisher = publisher;
        return this;
    }

    public long getApplicationSize() {
        return applicationSize;
    }

    public Application setApplicationSize(long applicationSize) {
        this.applicationSize = applicationSize;
        return this;
    }

    public boolean isCompressed() {
        return isCompressed;
    }

    public Application setCompressed(boolean compressed) {
        isCompressed = compressed;
        return this;
    }
}
