package towel;

/**
 * Interface that signifies that this function should be executed in its defining context by the interpreter
 *
 * This is used for user-defined functions imported from other files, they require their original context so they
 * have access to the imports scoped to their originating namespaces
 */
public interface ExecuteInOriginalContext {
    public Namespace getOriginalContext();
}
