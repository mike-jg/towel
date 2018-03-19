package towel.ast;

import towel.Token;

public class Array extends BaseNode {

    public final static Object[] EMPTY = new Object[0];
    private final Object[] contents;

    public Array(Token token, Object[] initialContents) {
        super(token);
        contents = initialContents;
    }

    public Object[] getContents() {
        return contents;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
