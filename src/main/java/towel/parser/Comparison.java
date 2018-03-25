package towel.parser;

/**
 * Operators such as == >= < >
 */
public class Comparison extends BaseNode {

    Comparison(Token token) {
        super(token);
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
