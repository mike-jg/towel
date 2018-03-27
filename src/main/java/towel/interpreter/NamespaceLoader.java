package towel.interpreter;

/**
 * This represents an API to a namespace, so it can be queried
 */
public interface NamespaceLoader {

    /**
     *
     * @param name name of the library/namespace
     * @return should return true if this namespace loader contains the given library
     */
    boolean hasNamespace(String name);

    /**
     * Get all public names in the given namespace
     *
     * @param namespace a namespace name
     * @return a list of all the public names in the given namespace
     */
    String[] getPublicNamesInNamespace(String namespace);

    /**
     * Get the function specified
     *
     * @param namespace the namespace to look in
     * @param functionName the function to get
     * @return the given function
     */
    TowelFunction getFunction(String namespace, String functionName);

    /**
     * Check whether the given namespace contains a function
     *
     * @param namespace the namespace to look in
     * @param functionName the function to check
     * @return whether the namespace contains the given function name
     */
    boolean namespaceContainsFunction(String namespace, String functionName);
}
