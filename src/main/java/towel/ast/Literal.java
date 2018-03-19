package towel.ast;

import towel.Token;

/**
 * A literal, e.g. a number, string or boolean
 *
 * "string literal"
 * 50
 * true
 */
public class Literal extends BaseNode {

    public Literal(Token token) {
        super(token);
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
