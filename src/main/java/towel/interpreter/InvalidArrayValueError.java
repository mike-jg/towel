package towel.interpreter;

/**
 * Thrown for an invalid array value
 *
 * Arrays are typed, so cannot contain a mixture of types.
 *
 * Usually this is the cause of this error
 */
public class InvalidArrayValueError extends RuntimeException {
    public InvalidArrayValueError(String message) {
        super(message);
    }
}
