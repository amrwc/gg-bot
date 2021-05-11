package dev.amrw.runner.exception;

/**
 * Unchecked exception for cases when copying an archive from/to a Docker container fails.
 */
public class ContainerArchiveCopyingException extends RuntimeException {

    public ContainerArchiveCopyingException() {
        this("Error copying a Docker container archive");
    }

    public ContainerArchiveCopyingException(final String message) {
        super(message);
    }

    public ContainerArchiveCopyingException(final String from, final String to) {
        this(String.format("Error copying a Docker archive from '%s' to '%s'", from, to));
    }
}
