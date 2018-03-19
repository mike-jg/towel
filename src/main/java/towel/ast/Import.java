package towel.ast;

import towel.Token;

/**
 * An import
 *
 * Keyword is the import token itself
 * Program is where the import is coming from
 * Target is the target to import, e.g. print, println, *
 * Alias is the new namespace to use
 */
public class Import extends BaseNode {

    private final String namespace;
    private final String[] target;
    private final String alias;
    public final static String[] NO_TARGET = new String[0];

    public Import(Token keyword, String namespace, String targets[], String alias) {
        super(keyword);
        this.namespace = namespace;
        this.target = targets;
        this.alias = alias;
    }

    public boolean isImportingWholeNamespace() {
        return target.length == 0 && alias == null;
    }

    public String getNamespace() {
        return namespace;
    }

    public boolean isStarImport() {
        return target.length == 1 && target[0].equals("*");
    }

    public boolean isSingleImport() {
        return target.length == 1 && !isStarImport();
    }

    public String[] getTarget() {
        return target;
    }

    public String getAlias() {
        return alias;
    }

    public boolean isAliased() {
        return alias != null;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
