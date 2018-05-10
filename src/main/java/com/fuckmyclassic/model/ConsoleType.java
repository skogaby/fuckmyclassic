package com.fuckmyclassic.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple enum for specifying a console type.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public enum ConsoleType {

    /** NES Classic Mini (USA and Europe) */
    NES_USA("nes-usa"),
    /** Famicom Classic Mini (Japan) */
    NES_JPN("nes-jpn"),
    /** SNES Classic Mini (USA) */
    SNES_USA("snes-usa"),
    /** SNES Classic Mini (Europe) */
    SNES_EUR("snes-eur"),
    /** Super Famicom Classic Mini (Japan) */
    SNES_JPN("snes-jpn");

    private static final Map<String, ConsoleType> codeToEnum;

    static {
        Map<String, ConsoleType> tmpMap = new HashMap<>();

        for (ConsoleType type : values()) {
            tmpMap.put(type.getConsoleCode(), type);
        }

        codeToEnum = Collections.unmodifiableMap(tmpMap);
    }

    private final String consoleCode;

    ConsoleType(final String consoleCode) {
        this.consoleCode = consoleCode;
    }

    public String getConsoleCode() {
        return consoleCode;
    }

    public static ConsoleType fromCode(final String consoleCode) {
        if (codeToEnum.containsKey(consoleCode)) {
            return codeToEnum.get(consoleCode);
        } else {
            throw new IllegalArgumentException(String.format("Invalid system code '%s'", consoleCode));
        }
    }
}
