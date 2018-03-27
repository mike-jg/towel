package towel.parser;

/**
 * Operators such as - + / *
 */
public class BinaryOperator extends BaseNode {

    BinaryOperator(Token token) {
        super(token);
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
