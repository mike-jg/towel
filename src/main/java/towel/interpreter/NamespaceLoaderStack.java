package towel.interpreter;

import java.util.*;

/**
 * A stack of loaders for namespaces, so multiple loaders can be treated as one
 */
public class NamespaceLoaderStack implements NamespaceLoader {

    private final Deque<NamespaceLoader> namespaceLoaders = new ArrayDeque<>();

    /**
     * Add a new loader
     *
     * @param item the loader to add
     */
    public void push(NamespaceLoader item) {
        namespaceLoaders.push(item);
    }

    @Override
    public boolean hasNamespace(String name) {

        for (NamespaceLoader loader : namespaceLoaders) {
            if (loader.hasNamespace(name)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String[] getPublicNamesInNamespace(String namespace) {

        List<String> names = new ArrayList<>();

        for (NamespaceLoader loader : namespaceLoaders) {
            // Don't return the first occurrence, gather all definitions from sub-loaders and return them all
            // This makes it easy to define namespaces across a mixture of .twl files and .java files
            if (loader.hasNamespace(namespace)) {
                names.addAll(Arrays.asList(loader.getPublicNamesInNamespace(namespace)));
            }
        }

        return names.toArray(new String[0]);
    }

    @Override
    public TowelFunction getFunction(String namespace, String functionName) {

        for (NamespaceLoader loader : namespaceLoaders) {
            if (loader.hasNamespace(namespace) && loader.namespaceContainsFunction(namespace, functionName)) {
                return loader.getFunction(namespace, functionName);
            }
        }

        return null;
    }

    @Override
    public boolean namespaceContainsFunction(String namespace, String functionName) {

        for (NamespaceLoader loader : namespaceLoaders) {
            if (loader.namespaceContainsFunction(namespace, functionName)) {
                return true;
            }
        }

        return false;
    }
}
