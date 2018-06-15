package com.fuckmyclassic.model;

import com.fuckmyclassic.userconfig.PathConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * Class to represent an original game (one of the stock games from
 * one of the mini consoles)
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Entity
public class OriginalGame extends Application implements Serializable {

    /** Says whether or not the boxart has been modified -- if it hasn't, we can point to the squashfs boxart */
    private boolean boxartModified;
    /** Says whether or not this is a NES game -- if it is, we have a different squashfs path to point to */
    private boolean nesGame;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeBoolean(boxartModified);
        out.writeBoolean(nesGame);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        setBoxartModified(in.readBoolean());
        setNesGame(in.readBoolean());
    }

    /**
     * Return a string representing the .desktop file for this game.
     * @param gameStoragePath The path to replace instances of `/var/games` with, in the
     *                        case that we're doing a linked export.
     * @return
     */
    @Override
    @Transient
    public String getDesktopFile(final String gameStoragePath) {
        String execLine = StringUtils.isBlank(getCommandLine()) ? "" : getCommandLine();
        String iconPath;

        if (this.boxartModified) {
            iconPath = String.format("%s/%s/%s.png", PathConfiguration.CONSOLE_GAMES_DIR, getApplicationId(), getApplicationId());
        } else {
            iconPath = String.format("%s/%s/%s.png", nesGame ? PathConfiguration.NES_SQUASHFS_GAMES_PATH :
                    PathConfiguration.SNES_SQUASHFS_GAMES_PATH, getApplicationId(), getApplicationId());
        }

        if (!StringUtils.isBlank(gameStoragePath)) {
            execLine = execLine.replace(PathConfiguration.CONSOLE_GAMES_DIR, gameStoragePath);
            iconPath = iconPath.replace(PathConfiguration.CONSOLE_GAMES_DIR, gameStoragePath);
        }

        // TODO: make this a little more robust and handle the SNES-specific fields correctly,
        // also add a real copyright field
        final StrBuilder sb = new StrBuilder();
        sb.append("[Desktop Entry]\n");
        sb.append("Type=Application\n");
        sb.append(String.format("Exec=%s\n", execLine));
        sb.append(String.format("Path=%s//%s\n", PathConfiguration.CONSOLE_SAVES_DIR, getApplicationId()));
        sb.append(String.format("Name=%s\n", getApplicationName()));
        sb.append(String.format("Icon=%s\n\n", iconPath));
        sb.append(String.format("[X-CLOVER Game]\n"));
        sb.append(String.format("Code=%s\n", getApplicationId()));
        sb.append(String.format("TestID=%d\n", getTestId()));
        sb.append("Status=Completing-3\n"); // TODO: handle correctly
        sb.append(String.format("ID=%d\n", getCanoeId()));
        sb.append(String.format("Players=%d\n", isSinglePlayer() ? 1 : 2));
        sb.append(String.format("Simultaneous=%d\n", isSimultaneousMultiplayer() ? 1 : 0));
        sb.append(String.format("ReleaseDate=%s\n", getReleaseDate().toString()));
        sb.append(String.format("SaveCount=%d\n", getSaveCount()));
        sb.append(String.format("SortRawTitle=%s\n", getSortName()));
        sb.append(String.format("SortRawPublisher=%s\n", getPublisher()));
        sb.append("Copyright=fuckmyclassic 2018\n"); // TODO: handle correctly
        sb.append("MyPlayDemoTime=45\n"); // TODO: handle correctly

        return sb.toString();
    }

    @Column(name = "is_boxart_modified")
    public boolean isBoxartModified() {
        return boxartModified;
    }

    public OriginalGame setBoxartModified(boolean boxartModified) {
        this.boxartModified = boxartModified;
        return this;
    }

    @Column(name = "is_nes_game")
    public boolean isNesGame() {
        return nesGame;
    }

    public OriginalGame setNesGame(boolean nesGame) {
        this.nesGame = nesGame;
        return this;
    }

    @Transient
    public boolean isSnesGame() {
        return !nesGame;
    }

    public OriginalGame setSnesGame(boolean snesGame) {
        this.nesGame = !snesGame;
        return this;
    }
}
