package towel.parser;

public class Array extends BaseNode {

    public final static Object[] EMPTY = new Object[0];
    private final Object[] contents;

    Array(Token token, Object[] initialContents) {
        super(token);
        contents = initialContents;
    }

    // @todo do something about this
    public static Array create(Token token, Object[] initialContents) {
        return new Array(token, initialContents);
    }

    public Object[] getContents() {
        return contents;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
