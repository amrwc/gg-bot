package dev.amrw.runner.util;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Collection of file/directory-related utilities.
 */
@Log4j2
public class FileUtil {

    private static FileUtil INSTANCE;

    private FileUtil() {
    }

    public static FileUtil getInstance() {
        return INSTANCE == null
                ? (INSTANCE = new FileUtil())
                : INSTANCE;
    }

    /**
     * Creates the given folder structure using {@link FileUtils#forceMkdir(File)}. Equivalent to {@code mkdir -p}.
     * @param path directory path
     * @throws IOException if the directory cannot be created or the file already exists but is not a directory
     */
    public void mkdir(final String path) throws IOException {
        final var directory = new File(path);
        try {
            FileUtils.forceMkdir(directory);
        } catch (final IOException exception) {
            log.error("Error creating directory (path={})", path, exception);
            throw exception;
        }
    }

    /**
     * Saves the given {@link InputStream} to a file on the given path.
     * @param stream {@link InputStream} with the file contents
     * @param destination path to the file
     * @throws IOException if <code>destination</code> is a directory
     * @throws IOException if <code>destination</code> cannot be written
     * @throws IOException if <code>destination</code> needs creating but can't be
     * @throws IOException if an IO error occurs during copying
     */
    public void toFile(final InputStream stream, final String destination) throws IOException {
        final var file = new File(destination);
        try {
            FileUtils.copyInputStreamToFile(stream, file);
        } catch (final IOException exception) {
            log.error("Error saving file (path={})", destination, exception);
            throw exception;
        }
    }
}
