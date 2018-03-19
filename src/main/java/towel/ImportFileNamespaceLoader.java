package towel;

/**
 * Loads names from a namespace, created from importing a physical file
 */
public class ImportFileNamespaceLoader implements NamespaceLoader {

    private final String namespaceName;
    private final Namespace namespaceContents;

    public ImportFileNamespaceLoader(String namespaceName, Namespace namespaceContents) {
        this.namespaceName = namespaceName;
        this.namespaceContents = namespaceContents;
    }

    @Override
    public boolean hasLibrary(String name) {
        return namespaceName.equals(name);
    }

    @Override
    public String[] getNamesInLibrary(String name) {
        return namespaceContents.getNames();
    }

    @Override
    public TowelFunction getFunction(String namespace, String functionName) {
        return (TowelFunction) namespaceContents.get(functionName);
    }

    @Override
    public boolean libraryContainsFunction(String library, String func) {
        return namespaceContents.isDefined(func);
    }
}
