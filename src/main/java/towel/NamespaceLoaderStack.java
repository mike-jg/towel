package towel;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A stack of loaders for namespaces, so multiple loaders can be treated as one
 */
public class NamespaceLoaderStack implements NamespaceLoader {

    private final Deque<NamespaceLoader> namespaceLoaders = new ArrayDeque<>();

    public void push(NamespaceLoader item) {
        namespaceLoaders.push(item);
    }

    public NamespaceLoader pop() {
        return namespaceLoaders.pop();
    }

    @Override
    public boolean hasLibrary(String name) {

        for (NamespaceLoader loader : namespaceLoaders) {
            if (loader.hasLibrary(name)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String[] getNamesInLibrary(String name) {

        for (NamespaceLoader loader : namespaceLoaders) {
            if (loader.hasLibrary(name)) {
                return loader.getNamesInLibrary(name);
            }
        }

        return new String[0];
    }

    @Override
    public TowelFunction getFunction(String namespace, String functionName) {

        for (NamespaceLoader loader : namespaceLoaders) {
            if (loader.hasLibrary(namespace)) {
                return loader.getFunction(namespace, functionName);
            }
        }

        return null;
    }

    @Override
    public boolean libraryContainsFunction(String library, String func) {

        for (NamespaceLoader loader : namespaceLoaders) {
            if (loader.libraryContainsFunction(library, func)) {
                return true;
            }
        }

        return false;
    }
}
