package towel;

public class Token {

    public static enum TokenType {

        LEFT_BRACE, RIGHT_BRACE,
        LEFT_BRACKET, RIGHT_BRACKET,
        LEFT_SQ_BRACKET, RIGHT_SQ_BRACKET,
        LESS_THAN, GREATER_THAN,
        EQUAL, QUESTION_MARK, DOUBLE_QUESTION_MARK,
        PLUS, MINUS, STAR, SLASH, MOD, AND, OR,
        COMMA, LET, DOT,

        ARROW, LESS_THAN_EQUAL, GREATER_THAN_EQUAL, EQUAL_EQUAL, NOT_EQUAL,

        IDENTIFIER, STRING_LITERAL, NUMBER_LITERAL, BOOLEAN_LITERAL,

        IMPORT, FROM, AS, DEF,

        // type defs
        NUM, BOOL, STR, SEQ, VOID, ANY, ARRAY,

        EOF,
    }

    /**
     * The type of the token
     */
    private final TokenType type;

    /**
     * How the token appears in the source
     * <p>
     * e.g
     * +
     * "this is a string"
     * [
     * ]
     */
    private String lexeme;

    /**
     * Literal value of the lexeme, e.g. a String or a boolean value
     */
    private final Object literal;

    /**
     * 1-indexed line number this was found on
     */
    private final int line;

    /**
     * 1-indexed character number for the line this was on e.g. 5th character along the 10th line
     */
    private final int character;

    /**
     * 0-indexed position in the source that this was found
     */
    private final int position;

    /**
     * @param type      the type of the token
     * @param lexeme    as the token appears in the source, e.g., + "this is a string" [ ]
     * @param literal   the literal value of the lexeme, e.g. a String or a boolean
     * @param line      the 1-indexed line number this was found on
     * @param character the 1-indexed character number for the line this was on e.g. 5th character along the 10th line
     * @param position  the 0-indexed position in the source that this was found
     */
    public Token(TokenType type, String lexeme, Object literal, int line, int character, int position) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.character = character;
        this.position = position;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public Object getLiteral() {
        return literal;
    }

    public int getLine() {
        return line;
    }

    public int getCharacter() {
        return character;
    }

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
