package com.fuckmyclassic.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.fuckmyclassic.shared.SharedConstants.SIZE_DICT;

/**
 * Class to help with common filesystem operations.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class FileUtils {

    /**
     * Returns the number of files in the given directory.
     * @param directory The directory to check the number of files in
     * @return The number of files in the given directory
     * @throws IOException
     */
    public static int numFilesInDirectory(final Path directory) throws IOException {
        int count = 0;
        final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory);

        for (Path path : directoryStream) {
            count++;
        }

        return count;
    }

    /**
     * Converts a long byte value into a human readable form.
     * @param size The number of bytes to display
     * @return The size in human readable form
     */
    public static String convertToHumanReadable(double size) {
        int index;

        for (index = 0; index < SIZE_DICT.length; index++) {
            if (size < 1024) {
                break;
            }

            size = size / 1024;
        }

        return String.format("%.2f %s", size, SIZE_DICT[index]);
    }
}
