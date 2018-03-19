package towel;

/**
 * For reporting errors during operations
 */
public interface ErrorReporter {

    void notice(String message);

    void notice(String message, int line, int character);

    void error(String message);

    void error(String message, int line, int character);
}
