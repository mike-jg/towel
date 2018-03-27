package towel.interpreter;

/**
 * User-supplied namespace. Acts the same as an internal loader, but needs the
 * file suffix, this is supplied in the constructor
 */
class UserFileNamespaceLoader extends InternalFileNamespaceLoader {
    UserFileNamespaceLoader(String namespaceName, Namespace namespaceContents) {
        super(namespaceName + ".twl", namespaceContents);
    }
}
