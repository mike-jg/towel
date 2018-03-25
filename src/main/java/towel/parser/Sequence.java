package towel.parser;

/**
 * Sequence of program to be executed later
 *
 * e.g. [ 1 5 3 + - print ]
 */
public class Sequence extends BaseNode {

    private final Node[] nodes;

    Sequence(Token token, Node[] nodes) {
        super(token);
        this.nodes = nodes;
    }

    // @todo do something about this
    public static Sequence create(Token token, Node[] nodes) {
        return new Sequence(token, nodes);
    }

    public Node[] getNodes() {
        return nodes;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
