package towel.parser;

import towel.ErrorReporter;
import towel.ast.Token;

import java.util.*;

import static towel.ast.Token.TokenType.*;

/**
 * Convert source code into a list of tokens
 */
class StringLexer implements Lexer {

    private String source;
    private List<Token> tokens;
    private int pointer = 0;
    private int line = 1;
    private int character = 1;
    private int startPointer = pointer;
    private int startLine = line;
    private int startCharacter = character;
    private final ErrorReporter reporter;

    private final static Map<String, Token.TokenType> singleCharTokens = new HashMap<>();
    private final static Map<String, Token.TokenType> keywords = new HashMap<>();

    static {
        keywords.put("import", IMPORT);
        keywords.put("from", FROM);
        keywords.put("as", AS);
        keywords.put("def", DEF);
        keywords.put("let", LET);
        keywords.put("num", NUM);
        keywords.put("bool", BOOL);
        keywords.put("str", STR);
        keywords.put("seq", SEQ);
        keywords.put("void", VOID);
        keywords.put("any", ANY);
        keywords.put("array", ARRAY);
        keywords.put("public", PUBLIC);
        singleCharTokens.put("{", LEFT_BRACE);
        singleCharTokens.put("}", RIGHT_BRACE);
        singleCharTokens.put("+", PLUS);
        singleCharTokens.put("-", MINUS);
        singleCharTokens.put("*", STAR);
        singleCharTokens.put("/", SLASH);
        singleCharTokens.put(",", COMMA);
        singleCharTokens.put("%", MOD);
        singleCharTokens.put("(", LEFT_BRACKET);
        singleCharTokens.put(")", RIGHT_BRACKET);
        singleCharTokens.put("[", LEFT_SQ_BRACKET);
        singleCharTokens.put("]", RIGHT_SQ_BRACKET);
        singleCharTokens.put(".", DOT);
    }

    StringLexer(String source, ErrorReporter reporter) {
        this.source = Objects.requireNonNull(source);
        this.reporter = Objects.requireNonNull(reporter);
        tokens = new ArrayList<>();
    }

    @Override
    public List<Token> tokenize() {

        while (!eof()) {
            // store positions for easily extracting the lexeme later
            startPointer = pointer;
            startLine = line;
            startCharacter = character;
            String c = advance();
            switch (c) {

                case "\n":
                    line++;
                    character = 1;
                    break;

                case " ":
                case "\r":
                case "\t":
                    break;

                case "?":
                    if (peekIs("?")) {
                        advance();
                        addToken(DOUBLE_QUESTION_MARK, "??");
                    } else {
                        addToken(QUESTION_MARK, "?");
                    }
                    break;

                case "&":
                    if (peekIs("&")) {
                        advance();
                        addToken(AND, "&&");
                    } else {
                        syntaxError("Unexpected character '" + c + "'.");
                    }
                    break;

                case "|":
                    if (peekIs("|")) {
                        advance();
                        addToken(OR, "||");
                    } else {
                        syntaxError("Unexpected character '" + c + "'.");
                    }
                    break;

                case "-":
                    if (peekIs(">")) {
                        advance();
                        addToken(ARROW, "->");
                    } else if (numeric(peek())) {
                        parseNumber();
                    } else {
                        addToken(MINUS);
                    }
                    break;

                case "<":
                    if (peekIs("=")) {
                        advance();
                        addToken(LESS_THAN_EQUAL, "<=");
                    } else {
                        addToken(LESS_THAN);
                    }
                    break;

                case ">":
                    if (peekIs("=")) {
                        advance();
                        addToken(GREATER_THAN_EQUAL, ">=");
                    } else {
                        addToken(GREATER_THAN);
                    }
                    break;

                case "=":
                    if (peekIs("=")) {
                        advance();
                        addToken(EQUAL_EQUAL, "==");
                    } else {
                        addToken(EQUAL);
                    }
                    break;

                case "!":
                    if (peekIs("=")) {
                        advance();
                        addToken(NOT_EQUAL, "!=");
                    } else {
                        syntaxError("Unexpected character '" + c + "'.");
                    }
                    break;

                case "/":
                    commentOrSlash(c);
                    break;

                case "\"":
                    parseString();
                    break;

                default:
                    if (singleCharTokens.containsKey(c)) {
                        addToken(singleCharTokens.get(c));
                    } else if (validIdentifierStart(c)) {
                        identifierOrKeyword();
                    } else if (numeric(c)) {
                        parseNumber();
                    } else {
                        syntaxError("Unexpected character '" + c + "'.");
                    }
            }
        }

        tokens.add(Token.create(EOF, "\0", "\0", line, character, pointer));

        return tokens;
    }

    private void addToken(Token.TokenType type) {
        addToken(type, source.substring(startPointer, pointer));
    }

    private void addToken(Token.TokenType type, String lexeme) {
        addToken(type, lexeme, lexeme);
    }

    private void addToken(Token.TokenType type, String lexeme, Object literal) {
        tokens.add(Token.create(type, lexeme, literal, startLine, startCharacter, startPointer));
    }

    /**
     * An error is only logged, the Lexer continues to tokenize the rest of the input
     *
     * This makes for a better user experience as you'll get a full list of errors instead of one at a time
     */
    private void syntaxError(String message) {
        reporter.error(
                message + String.format(" At line %d, character %d.", line, character)
        );
    }

    /**
     * This is either a comment or a slash
     */
    private void commentOrSlash(String c) {
        // single line comment
        // keep going until finding a newline or EOF
        if (peekIs("/")) {
            advance();
            while (!eof()) {
                if (peekIs("\n")) {
                    character = 1;
                    line++;
                    break;
                }
                advance();
            }
        }
        // multiline comment
        // keep going until finding a */ which denotes the end of a multiline comment
        // OR until finding an EOF, which just means there's an unterminated comment which doesn't really matter
        // (though it could be user error)

        else if (peekIs("*")) {
            advance();
            while (!eof()) {
                if (peekIs("\n")) {
                    character = 1;
                    line++;
                    advance();
                }
                if (peekIs("*") && lookAhead().equals("/")) {
                    advance();
                    advance();
                    break;
                }
                if (!eof()) {
                    advance();
                }
            }
        } else {
            addToken(SLASH, "/");
        }
    }

    /**
     * A numeric character
     */
    private boolean numeric(String c) {
        return "1234567890".contains("" + c);
    }

    /**
     * A valid character or using as the start of an identifier
     */
    private boolean validIdentifierStart(String c) {
        return "abcdefghijklmnopqrstuvwxyz".contains(c.toLowerCase());
    }

    /**
     * A valid character for use within an identifier
     */
    private boolean validIdentifierChar(String c) {
        return validIdentifierStart(c) || numeric(c) || c.equals("_") || c.equals("-");
    }

    /**
     * Try to parse an identifier or a token
     */
    private void identifierOrKeyword() {
        while (!eof() && validIdentifierChar(peek())) {
            advance();
        }

        String identifier = source.substring(startPointer, pointer);

        if (identifier.equals("true") || identifier.equals("false")) {
            addToken(BOOLEAN_LITERAL, identifier, identifier.equals("true"));
            return;
        }

        Token.TokenType type = keywords.getOrDefault(identifier, IDENTIFIER);
        addToken(type, identifier);
    }

    /**
     * Parse a number
     *
     * e.g. 5 -6 1.5
     */
    private void parseNumber() {
        boolean foundDot = false;
        while (!eof() && numeric(peek()) || peekIs(".")) {
            if (peekIs(".")) {
                if (foundDot) {
                    break;
                }
                foundDot = true;
            }
            advance();
        }

        String lexeme = source.substring(startPointer, pointer);

        addToken(NUMBER_LITERAL, lexeme, Double.parseDouble(lexeme));
    }

    /**
     * Get the current character
     */
    private String peek() {
        if (eof()) {
            return "\0";
        }
        return source.substring(pointer, pointer + 1);
    }

    /**
     * Does the current character match the given character?
     */
    private boolean peekIs(String character) {
        if (eof()) {
            return false;
        }
        return peek().equals(character);
    }

    /**
     * Get the character one position ahead
     */
    private String lookAhead() {
        if (pointer + 2 < source.length()) {
            return source.substring(pointer + 1, pointer + 2);
        }
        return "\0";
    }

    /**
     * Advance the pointer and return the next character
     */
    private String advance() {
        character++;
        pointer++;
        return source.substring(pointer - 1, pointer);
    }

    /**
     * Has the end been reached?
     */
    private boolean eof() {
        return pointer >= source.length();
    }

    /**
     * Parse a quote-delimited string
     *
     * e.g. "this is a string"
     */
    private void parseString() {
        // search until we find a closing "
        while (!eof() && !peekIs("\"")) {
            advance();
        }

        if (eof()) {
            syntaxError("Unterminated string literal.");
            return;
        }

        // remove closing "
        advance();
        String lexeme = source.substring(startPointer, pointer);
        String literal = source.substring(startPointer + 1, pointer - 1);

        addToken(STRING_LITERAL, lexeme, canonicalizeString(literal));
    }

    /**
     * Replace literals with special escape characters so tabs, newlines etc. work correctly
     */
    private String canonicalizeString(String str) {
        return str.replace("\\t", "\t")
                .replace("\\n", "\n")
                .replace("\\r", "\r");
    }

}