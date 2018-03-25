package towel.parser;

/**
 * Conditionally executes a sequence based on a boolean value
 */
public class Condition extends BaseNode {

    Condition(Token token) {
        super(token);
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
