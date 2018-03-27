package towel.interpreter;

/**
 * Thrown for an invalid array value
 * <p>
 * Arrays are typed, so cannot contain a mixture of types, usually that is the cause of this error
 * </p>
 */
public class InvalidArrayValueError extends RuntimeException {
    public InvalidArrayValueError(String message) {
        super(message);
    }
}
