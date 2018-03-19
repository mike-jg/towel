package towel.ast;

import towel.Token;

/**
 * Operators such as - + / *
 */
public class BinaryOperator extends BaseNode {

    public BinaryOperator(Token token) {
        super(token);
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
