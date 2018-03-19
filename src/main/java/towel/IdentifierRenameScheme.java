package towel;

import towel.ast.Renameable;

/**
 * Rename an identifier, an easy way to resolve scope in closures
 */
public class IdentifierRenameScheme {

    /**
     * Adds a dollar sign between the 'scope' e.g. function definition and the identifier name
     *
     * This ensures it can't clash with user-land code, as a dollar sign is not valid there
     */
    private static final String IDENTIFIER_RENAME_SCHEME = "%s$%s";

    public void rename(Renameable identifier, String scope) {
        identifier.setName(String.format(IDENTIFIER_RENAME_SCHEME, scope, identifier.getOriginalName()));
    }

}
