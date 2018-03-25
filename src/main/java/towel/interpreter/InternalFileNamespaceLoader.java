package towel.interpreter;

import java.util.Objects;

/**
 * Loads names from a namespace, created from importing a physical file
 */
public class InternalFileNamespaceLoader implements NamespaceLoader {

    private final String fileNamespace;
    private final Namespace namespaceContents;

    public InternalFileNamespaceLoader(String namespaceName, Namespace namespaceContents) {
        fileNamespace = Objects.requireNonNull(namespaceName);
        this.namespaceContents = Objects.requireNonNull(namespaceContents);
    }

    @Override
    public boolean hasLibrary(String name) {
        return fileNamespace.equals(name);
    }

    @Override
    public String[] getNamesInLibrary(String name) {
        if (hasLibrary(name)) {
            return namespaceContents.getPublicMembers();
        }
        return new String[0];
    }

    @Override
    public TowelFunction getFunction(String namespace, String functionName) {
        if (namespace.equals(fileNamespace)) {
            return (TowelFunction) namespaceContents.get(functionName);
        }
        return null;
    }

    @Override
    public boolean libraryContainsFunction(String library, String func) {
        return fileNamespace.equals(library) && namespaceContents.isDefined(func);
    }
}
