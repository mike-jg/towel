package towel.parser;

import java.util.Objects;

public abstract class BaseNode implements Node {

    private final Token token;

    public BaseNode(Token token) {
        this.token = Objects.requireNonNull(token);
    }

    public String getLexeme() {
        return token.getLexeme();
    }

    public Token getToken() {
        return token;
    }

    public Token.TokenType getTokenType() {
        return token.getType();
    }

    public String getLiteralAsString() {
        return token.getLiteral().toString();
    }

    @Override
    abstract public <T> T accept(NodeVisitor<T> visitor);
}
