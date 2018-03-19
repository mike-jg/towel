package towel.ast;

import towel.Token;

public class FileImport extends BaseNode {
    private final Token file;

    public FileImport(Token token, Token file) {
        super(token);
        this.file = file;
    }

    public String getNamespace() {
        return getFile().replace(".twl", "");
    }

    public String getFile() {
        return file.getLiteral().toString();
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
