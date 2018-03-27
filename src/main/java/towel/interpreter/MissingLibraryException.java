package towel.interpreter;

/**
 * Thrown if an import request is made for a missing library/namespace
 * <p>
 * Should never really get thrown as the LibraryLoader has methods to check if a library exists
 * </p>
 */
public class MissingLibraryException extends RuntimeException {
    public MissingLibraryException(String message) {
        super(message);
    }
}
