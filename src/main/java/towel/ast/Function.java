package towel.ast;

/**
 * A definition is a named sequence that must find and leave the stack in a pre-agreed form
 */
public class Function extends BaseNode {

    private final Node[] body;
    private final Class[] preConditions;
    private final Class[] postConditions;
    private final boolean isPublic;

    public Function(Token token, boolean isPublic, Node[] body, Class[] preConditions, Class[] postConditions) {
        super(token);
        this.isPublic = isPublic;
        this.body = body;
        this.preConditions = preConditions;
        this.postConditions = postConditions;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public Node[] getBody() {
        return body;
    }

    public Class[] getPreConditions() {
        return preConditions;
    }

    public Class[] getPostConditions() {
        return postConditions;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
