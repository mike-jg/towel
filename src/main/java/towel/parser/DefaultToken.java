package towel.parser;

class DefaultToken implements Token {

    private final TokenType type;

    private String lexeme;

    private final Object literal;

    private final int line;

    private final int character;

    private final int position;

    /**
     * @param type      the type of the token
     * @param lexeme    as the token appears in the source, e.g., + "this is a string" [ ]
     * @param literal   the literal value of the lexeme, e.g. a String or a boolean
     * @param line      the 1-indexed line number this was found on
     * @param character the 1-indexed character number for the line this was on e.g. 5th character along the 10th line
     * @param position  the 0-indexed position in the source that this was found
     */
    DefaultToken(TokenType type, String lexeme, Object literal, int line, int character, int position) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.character = character;
        this.position = position;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public String getLexeme() {
        return lexeme;
    }

    @Override
    public Object getLiteral() {
        return literal;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getCharacter() {
        return character;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", lexeme='" + lexeme + '\'' +
                ", literal=" + literal +
                ", line=" + line +
                ", character=" + character +
                ", position=" + position +
                '}';
    }
}