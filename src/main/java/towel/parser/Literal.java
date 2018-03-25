package towel.parser;

/**
 * A literal, e.g. a number, string or boolean
 *
 * "string literal"
 * 50
 * true
 */
public class Literal extends BaseNode {

    Literal(Token token) {
        super(token);
    }

    // @todo do something about this
    public static Literal create(Token token) {
        return new Literal(token);
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
