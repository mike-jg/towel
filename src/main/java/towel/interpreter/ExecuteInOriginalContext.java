package towel.interpreter;

/**
 * Interface that signifies that this function should be executed in its defining context by the interpreter
 * <p>
 * This is used for user-defined functions imported from other files, they require their original context so they
 * have access to the imports scoped to their originating namespaces
 */
interface ExecuteInOriginalContext {
    Namespace getOriginalContext();
}
