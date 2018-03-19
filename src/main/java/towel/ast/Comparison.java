package towel.ast;

import towel.Token;

/**
 * Operators such as == >= < >
 */
public class Comparison extends BaseNode {

    public Comparison(Token token) {
        super(token);
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
