package com.fuckmyclassic.model;

/**
 * Very simple class to hold the name and game code for an original game.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class DefaultGame {

    /** The application ID for the game */
    public final String gameCode;
    /** The name of the game */
    public final String gameName;

    public DefaultGame(final String gameCode,
                       final String gameName) {
        this.gameCode = gameCode;
        this.gameName = gameName;
    }
}
