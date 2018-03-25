package towel.parser;

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

    Import(Token keyword, String namespace, String targets[], String alias) {
        super(keyword);
        this.namespace = namespace;
        this.target = targets;
        this.alias = alias;
    }

    public String getNamespace() {
        return namespace;
    }

    public String[] getTarget() {
        return target;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
