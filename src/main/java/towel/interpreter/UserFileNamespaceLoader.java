package towel.interpreter;

/**
 * User-supplied namespace. Acts the same as an internal loader, but needs the
 * file suffix, this is supplied in the constructor
 */
public class UserFileNamespaceLoader extends InternalFileNamespaceLoader {
    public UserFileNamespaceLoader(String namespaceName, Namespace namespaceContents) {
        super(namespaceName + ".twl", namespaceContents);
    }
}
