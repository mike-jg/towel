package towel.parser;

public class Let extends BaseNode implements Renameable {

    private String lookupName = null;
    private final boolean isPublic;

    Let(Token name, boolean isPublic) {
        super(name);
        this.isPublic = isPublic;
    }

    public boolean isPublic() {
        return isPublic;
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
