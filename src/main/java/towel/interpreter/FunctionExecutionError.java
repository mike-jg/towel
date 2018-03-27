package towel.interpreter;

/**
 * This is thrown during the execution of a function, if something goes wrong
 * <p>
 * e.g. stack state becomes invalid or an incompatible type is found
 */
public class FunctionExecutionError extends RuntimeException {
    public FunctionExecutionError(String message) {
        super(message);
    }

    public FunctionExecutionError(String message, Throwable cause) {
        super(message, cause);
    }
}
