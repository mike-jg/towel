package towel.interpreter;

/**
 * Thrown if an import request is made for a missing library/namespace
 *
 * Should never really get thrown as the LibraryLoader has methods to check if a library exists
 */
public class MissingLibraryException extends RuntimeException {
    public MissingLibraryException(String message) {
        super(message);
    }
}
