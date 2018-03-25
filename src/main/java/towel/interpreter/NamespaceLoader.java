package towel.interpreter;

/**
 * This represents an API to a namespace, so it can be queried
 */
public interface NamespaceLoader {
    boolean hasLibrary(String name);

    String[] getNamesInLibrary(String name);

    TowelFunction getFunction(String namespace, String functionName);

    boolean libraryContainsFunction(String library, String func);
}
