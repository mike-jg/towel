package towel.ast;

public class Identifier extends BaseNode implements Renameable {

    private String lookupName = null;
    private final Token namespace;

    public Identifier(Token token, Token namespace) {
        super(token);
        this.namespace = namespace;
    }

    public Token getNamespaceToken() {
        return namespace;
    }

    public boolean isNamespaced() {
        return namespace != null;
    }

    public String getNamespace() {
        return namespace.getLexeme();
    }

    @Override
    public String getName() {
        return lookupName == null ? getOriginalName() : lookupName;
    }

    @Override
    public void setName(String lookupName) {
        this.lookupName = lookupName;
    }

    @Override
    public String getOriginalName() {
        return getLexeme();
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
