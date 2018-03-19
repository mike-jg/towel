package towel.ast;

import towel.Token;

/**
 * A definition is a named sequence that must find and leave the stack in a pre-agreed form
 */
public class Function extends BaseNode {

    private final Node[] body;
    private final Class[] preConditions;
    private final Class[] postConditions;

    public Function(Token token, Node[] body, Class[] preConditions, Class[] postConditions) {
        super(token);
        this.body = body;
        this.preConditions = preConditions;
        this.postConditions = postConditions;
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
