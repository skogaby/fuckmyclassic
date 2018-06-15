package com.fuckmyclassic.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to help with common string operations.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class StringUtils {

    /**
     * Gets all the tokens of the given string, delimited by spaces (ignoring escaped spaces
     * or those in quotation marks).
     * @param str The string to tokenize
     * @return The list of tokens in the string
     */
    public static String[] tokenizeString(final String str) {
        final List<String> tokens = new ArrayList<>();
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        boolean endOfToken = false;
        int startIndex = 0;
        int endIndex = 1;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (i == str.length() - 1) {
                endOfToken = true;
                endIndex = i + 1;
            } else if (c == '\'') {
                singleQuoted = !singleQuoted;
            } else if (c == '"') {
                doubleQuoted = !doubleQuoted;
            } else if (c == ' ' &&
                    i > 0 &&
                    !(str.charAt(i - 1) == '\\' ||
                            singleQuoted ||
                            doubleQuoted)) {
                endOfToken = true;
                endIndex = i;
            }

            if (endOfToken) {
                tokens.add(str.substring(startIndex, endIndex));
                singleQuoted = false;
                doubleQuoted = false;
                endOfToken = false;
                startIndex = i + 1;
                endIndex = i + 2;
            }
        }

        return tokens.toArray(new String[] { });
    }
}
