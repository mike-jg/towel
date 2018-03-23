package towel;

import java.util.*;

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

        List<String> names = new ArrayList<>();

        for (NamespaceLoader loader : namespaceLoaders) {
            // Don't return the first occurrence, gather all definitions from sub-loaders and return them all
            // This makes it easy to definePrivateMember namespaces across a mixture of .twl files and .java files
            if (loader.hasLibrary(name)) {
                names.addAll(Arrays.asList(loader.getNamesInLibrary(name)));
            }
        }

        return names.toArray(new String[names.size()]);
    }

    @Override
    public TowelFunction getFunction(String namespace, String functionName) {

        for (NamespaceLoader loader : namespaceLoaders) {
            if (loader.hasLibrary(namespace) && loader.libraryContainsFunction(namespace, functionName)) {
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
