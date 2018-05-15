package com.fuckmyclassic.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

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
}
