package towel.ast;

/**
 * @todo is this really necesary?
 */
public interface Token {

    int NO_OP_POSITION = -1;

    static Token create(TokenType type, String lexeme, Object literal, int line, int character, int position) {
        return new DefaultToken(type, lexeme, literal, line, character, position);
    }

    static Token create(TokenType type, String lexeme, Object literal) {
        return new DefaultToken(type, lexeme, literal, NO_OP_POSITION, NO_OP_POSITION, NO_OP_POSITION);
    }

    enum TokenType {

        LEFT_BRACE, RIGHT_BRACE,
        LEFT_BRACKET, RIGHT_BRACKET,
        LEFT_SQ_BRACKET, RIGHT_SQ_BRACKET,

        // comparison
        LESS_THAN, GREATER_THAN,
        EQUAL, QUESTION_MARK, DOUBLE_QUESTION_MARK,

        COMMA, DOT,

        // binary ops
        PLUS, MINUS, STAR, SLASH, MOD, AND, OR,
        ARROW, LESS_THAN_EQUAL, GREATER_THAN_EQUAL, EQUAL_EQUAL, NOT_EQUAL,

        IDENTIFIER, STRING_LITERAL, NUMBER_LITERAL, BOOLEAN_LITERAL,

        // importing
        IMPORT, FROM, AS,

        // function def
        PUBLIC, DEF, LET,

        // type definitions
        NUM, BOOL, STR, SEQ, VOID, ANY, ARRAY,

        EOF,
    }

    /**
     * The type of the token
     */
    TokenType getType();

    /**
     * How the token appears in the source
     *
     * e.g.:
     * <li> +
     * <li> "this is a string"
     * <li> [
     * <li> ]
     */
    String getLexeme();

    /**
     * Literal value of the lexeme, e.g. a String or a boolean value
     */
    Object getLiteral();

    /**
     * 1-indexed line number this was found on
     */
    int getLine();

    /**
     * 1-indexed character number for the line this was on e.g. 5th character along the 10th line
     */
    int getCharacter();

    /**
     * 0-indexed position in the source that this was found
     */
    int getPosition();
}
