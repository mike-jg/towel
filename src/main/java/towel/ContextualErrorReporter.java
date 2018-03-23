package towel;

/**
 * An error reporter that allows setting a context for subsequent logs
 *
 * <p>
 *     For example, if wanting to log file info
 * </p>
 */
public interface ContextualErrorReporter extends ErrorReporter {

    /**
     * Calls to {@code notice} and {@code error} that are made after
     * setting the context, should include this context in the log information
     *
     * @param context new context
     */
    void setContext(String context);
}
