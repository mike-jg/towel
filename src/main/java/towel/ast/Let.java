package towel.ast;

import towel.Token;

public class Let extends BaseNode implements Renameable {

    private String lookupName = null;

    public Let(Token name) {
        super(name);
    }

    @Override
    public String getName() {
        return lookupName == null ? getOriginalName() : lookupName;
    }

    @Override
    public void setName(String lookupName) {
        this.lookupName = lookupName;
    }

    @Override
    public String getOriginalName() {
        return getLexeme();
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
