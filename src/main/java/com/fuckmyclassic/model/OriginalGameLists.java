package com.fuckmyclassic.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple class to get the list of original games for each console type.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class OriginalGameLists {

    /**
     * The list of default games for the NES Classic Mini.
     */
    public static final String[] defaultNesGames = new String[] {
            "CLV-P-NAAAE", "CLV-P-NAACE", "CLV-P-NAADE", "CLV-P-NAAEE", "CLV-P-NAAFE", "CLV-P-NAAHE",
            "CLV-P-NAANE", "CLV-P-NAAPE", "CLV-P-NAAQE", "CLV-P-NAARE", "CLV-P-NAASE", "CLV-P-NAATE",
            "CLV-P-NAAUE", "CLV-P-NAAVE", "CLV-P-NAAWE", "CLV-P-NAAXE", "CLV-P-NAAZE", "CLV-P-NABBE",
            "CLV-P-NABCE", "CLV-P-NABJE", "CLV-P-NABKE", "CLV-P-NABME", "CLV-P-NABNE", "CLV-P-NABQE",
            "CLV-P-NABRE", "CLV-P-NABVE", "CLV-P-NABXE", "CLV-P-NACBE", "CLV-P-NACDE", "CLV-P-NACHE"
    };

    /**
     * The list of default games for the NES Classic Mini.
     */
    public static final String[] defaultFamicomGames = new String[] {
            "CLV-P-HAAAJ", "CLV-P-HAACJ", "CLV-P-HAADJ", "CLV-P-HAAEJ", "CLV-P-HAAHJ", "CLV-P-HAAMJ",
            "CLV-P-HAANJ", "CLV-P-HAAPJ", "CLV-P-HAAQJ", "CLV-P-HAARJ", "CLV-P-HAASJ", "CLV-P-HAAUJ",
            "CLV-P-HAAWJ", "CLV-P-HAAXJ", "CLV-P-HABBJ", "CLV-P-HABCJ", "CLV-P-HABLJ", "CLV-P-HABMJ",
            "CLV-P-HABNJ", "CLV-P-HABQJ", "CLV-P-HABRJ", "CLV-P-HABVJ", "CLV-P-HACAJ", "CLV-P-HACBJ",
            "CLV-P-HACCJ", "CLV-P-HACEJ", "CLV-P-HACHJ", "CLV-P-HACJJ", "CLV-P-HACLJ", "CLV-P-HACPJ"
    };

    /**
     * The list of default games for the SNES Classic Mini.
     */
    public static final String[] defaultSnesGames = new String[] {
            "CLV-P-SAAAE", "CLV-P-SAABE", "CLV-P-SAAEE", "CLV-P-SAAFE", "CLV-P-SAAHE", "CLV-P-SAAJE",
            "CLV-P-SAAKE", "CLV-P-SAALE", "CLV-P-SAAQE", "CLV-P-SAAXE", "CLV-P-SABCE", "CLV-P-SABDE",
            "CLV-P-SABHE", "CLV-P-SABQE", "CLV-P-SABRE", "CLV-P-SABTE", "CLV-P-SACBE", "CLV-P-SACCE",
            "CLV-P-SADGE", "CLV-P-SADJE", "CLV-P-SADKE"
    };

    /**
     * The list of default games for the SFC Classic Mini.
     */
    public static final String[] defaultSuperFamicomGames = new String[] {
            "CLV-P-VAAAJ", "CLV-P-VAABJ", "CLV-P-VAAEJ", "CLV-P-VAAFJ", "CLV-P-VAAGJ", "CLV-P-VAAHJ",
            "CLV-P-VAALJ", "CLV-P-VAAQJ", "CLV-P-VABBJ", "CLV-P-VABCJ", "CLV-P-VABDJ", "CLV-P-VABQJ",
            "CLV-P-VABRJ", "CLV-P-VABTJ", "CLV-P-VACCJ", "CLV-P-VACDJ", "CLV-P-VADFJ", "CLV-P-VADGJ",
            "CLV-P-VADJJ", "CLV-P-VADKJ", "CLV-P-VADZJ"
    };

    /**
     * A mapping of console types to original games.
     */
    public static final Map<ConsoleType, String[]> gameCodeMappings;

    static {
        gameCodeMappings = new HashMap<>();

        gameCodeMappings.put(ConsoleType.NES_USA, defaultNesGames);
        gameCodeMappings.put(ConsoleType.NES_JPN, defaultFamicomGames);
        gameCodeMappings.put(ConsoleType.SNES_USA, defaultSnesGames);
        gameCodeMappings.put(ConsoleType.SNES_EUR, defaultSnesGames);
        gameCodeMappings.put(ConsoleType.SNES_JPN, defaultSuperFamicomGames);
    }

    /**
     * Gets the list of original games for the given ConsoleType.
     * @param consoleType The type of console to get the games for
     * @return The original games for the given console type
     */
    public static String[] getGamesForConsoleType(final ConsoleType consoleType) {
        return gameCodeMappings.get(consoleType);
    }
}
