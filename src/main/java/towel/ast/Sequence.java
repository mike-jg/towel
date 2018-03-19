package towel.ast;

import towel.Token;

/**
 * Sequence of program to be executed later
 *
 * e.g. [ 1 5 3 + - print ]
 */
public class Sequence extends BaseNode {

    private final Node[] nodes;

    public Sequence(Token token, Node[] nodes) {
        super(token);
        this.nodes = nodes;
    }

    public Node[] getNodes() {
        return nodes;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
