package towel.interpreter;

import towel.parser.Token;

/**
 * Thrown if the stack fails an assertion or pre/post-condition check
 */
public class StackAssertionError extends RuntimeException {
    public final Token token;

    public StackAssertionError(String message, Token token) {
        super(message);
        this.token = token;
    }
}
