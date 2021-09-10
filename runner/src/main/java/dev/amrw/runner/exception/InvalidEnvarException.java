package dev.amrw.runner.exception;

/**
 * Unchecked exception for cases when one or more envars are missing, or have invalid values.
 */
public class InvalidEnvarException extends IllegalStateException {

    public InvalidEnvarException() {
        this("Invalid envar(s)");
    }

    public InvalidEnvarException(final String message) {
        super(message);
    }
}
