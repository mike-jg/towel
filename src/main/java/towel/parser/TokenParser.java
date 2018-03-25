package towel.parser;

import towel.ErrorReporter;
import towel.interpreter.TowelArray;
import towel.interpreter.TowelFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static towel.parser.Token.TokenType.*;

/**
 * Parse the tokens into an abstract syntax tree
 *
 * Need to consider lexerless parser here, because the lexer has no context it adds
 * restrictions to the language. e.g. 'num' cannot be used as a function or let name because
 * it's used in the stack pre and post-condition checks and so it's a reserved word essentially.
 * The parser has all the context it needs to avoid this issue, unfortunately the lexer doesn't
 * and blindly lexes all 'num' lexemes as keywords.
 */
class TokenParser implements Parser {

    private static class ParseError extends RuntimeException {

        private final Token token;

        ParseError(String message, Token token) {
            super(message);
            this.token = token;
        }
    }

    /**
     * Unparsed tokens
     */
    private final List<Token> tokens;

    /**
     * Parsed AST
     */
    private final List<Node> nodes = new ArrayList<>();
    private int pos = 0;
    private final ErrorReporter reporter;
    private final String namespace;

    TokenParser(List<Token> tokens, ErrorReporter reporter, String namespace) {
        this.tokens = Objects.requireNonNull(tokens);
        this.reporter = Objects.requireNonNull(reporter);
        this.namespace = Objects.requireNonNull(namespace);
    }

    /**
     * Run the parser and return the AST
     */
    @Override
    public Program parse() {
        while (true) {
            Token t = advance();
            if (is(t, EOF)) {
                break;
            }

            try {
                Node node = doParse(t);
                nodes.add(node);
            } catch (ParseError e) {
                reporter.error(e.getMessage(), e.token.getLine(), e.token.getCharacter());
                return null;
            }
        }

        // Extract all Import and FileImport nodes from the node list

        // It would probably be better to change this at some point to
        // collect the import nodes when they are created in the parser methods
        // rather than using streams on the full list of nodes
        List<Import> imports = nodes
                .stream()
                .filter(Import.class::isInstance)
                .map(Import.class::cast)
                .collect(Collectors.toCollection(ArrayList<Import>::new));

        return new ProgramNode(namespace, nodes, imports);
    }

    private Node doParse(Token t) {
        return function(t);
    }

    private Token advance() {
        pos++;
        return tokens.get(pos - 1);
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private boolean eof() {
        return is(tokens.get(pos), EOF);
    }

    /**
     * If the current (peek) token doesn't match the given token type, throw a parse exception
     *
     * Otherwise return the current token and advance
     */
    private Token expect(Token.TokenType token, String message) {
        if (!isPeek(token)) {
            throw new ParseError(message, peek());
        }
        return advance();
    }

    /**
     * Gather a comma separated list of types into an array
     *
     * e.g. num, str, bool, str
     */
    private Class[] gatherTypes(String token) {
        List<Class> conditions = new ArrayList<>();
        boolean hasVoid = false;
        while (true) {
            switch (advance().getType()) {
                case NUM:
                    conditions.add(Double.class);
                    break;
                case BOOL:
                    conditions.add(Boolean.class);
                    break;
                case STR:
                    conditions.add(String.class);
                    break;
                case SEQ:
                    conditions.add(Sequence.class);
                    break;
                case ARRAY:
                    conditions.add(TowelArray.class);
                    break;
                case ANY:
                    conditions.add(Object.class);
                    break;
                case VOID:
                    hasVoid = true;
                    conditions.add(Void.class);
                    break;
                default:
                    throw new ParseError(String.format("Expecting type after '%s'.", token), peek());
            }

            if (!isPeek(COMMA)) {
                break;
            }
            advance();
        }

        // Void basically means 'no checks', so it makes no sense to have a void and other types
        // 'any' can be used as a placeholder for 'any type'

        if (conditions.size() > 1 && hasVoid) {
            throw new ParseError("Cannot specify 'void' with multiple types.", peek());
        } else if (hasVoid) {
            return TowelFunction.NO_STACK_CONDITION;
        }

        // Reverse the conditions
        // Conditions are defined left to right in the source code, with the right-most condition
        // being the top of the stack. e.g. 'str, bool, num' would assert that the stack contains:
        // conditions[0]  num    (top of stack)
        // conditions[1]  bool
        // conditions[2]  str    (bottom of stack)

        // In short, reversing the condition makes it so that condition N represents an assertion N positions
        // from the top of the stack

        // this makes it a bit easier to reason about it later on when the assertion takes place
        // without this reversal, it would be the other way around

        Class[] reversed = new Class[conditions.size()];
        for (int i = 0, c = conditions.size() - 1; i < conditions.size(); i++, c--) {
            reversed[i] = conditions.get(c);
        }

        return reversed;
    }

    /**
     * Functions are declared using the 'def' keyword, followed by a function name,
     * optional stack pre and post-conditions, then a mandatory body enclosed within curly braces
     *
     * Optional prefix of 'public' to make things public
     *
     * e.g.
     * def myfunc ( num, num, str -> void ) {  }
     */
    private Node function(Token token) {

        if (!(is(token, PUBLIC) && isPeek(DEF)) && !is(token, DEF)) {
            return let(token);
        }

        boolean isPublic = false;
        if (is(token, PUBLIC)) {
            isPublic = true;
            advance();
        }

        List<Node> bodyNodes = new ArrayList<>();

        Token name = expect(IDENTIFIER, "Expecting function name after 'def'.");

        Class[] preConditions = TowelFunction.NO_STACK_CONDITION;
        Class[] postConditions = TowelFunction.NO_STACK_CONDITION;

        if (isPeek(LEFT_BRACKET)) {
            advance();
            preConditions = gatherTypes("(");
            expect(ARROW, "Expecting '->' after pre-conditions.");
            postConditions = gatherTypes("->");
            expect(RIGHT_BRACKET, "Expecting closing bracket after pre-conditions and post-conditions.");
        }

        expect(LEFT_BRACE, "Expecting '{' after function signature definition.");

        while (!eof() && !isPeek(RIGHT_BRACE)) {
            Node node = doParse(advance());
            bodyNodes.add(node);
        }

        if (eof()) {
            throw new ParseError("Unterminated function body.", peek());
        }

        // throw away closing brace
        advance();

        return new Function(
                name,
                isPublic,
                bodyNodes.toArray(new Node[0]),
                preConditions,
                postConditions
        );
    }

    /**
     * Let expects the optional 'public' keyword followed by 'let' followed by an identifier
     */
    private Node let(Token token) {

        if (!(is(token, PUBLIC) && isPeek(LET)) && !is(token, LET)) {
            return sequence(token);
        }

        boolean isPublic = false;
        if (is(token, PUBLIC)) {
            isPublic = true;
            advance();
        }

        Token name = expect(IDENTIFIER, "Expecting identifier after 'let'.");

        return new Let(name, isPublic);
    }

    /**
     * A sequence is just a list of things within curly braces, e.g.
     * { 4 5 + "in a sequence" }
     */
    private Node sequence(Token token) {
        if (!is(token, LEFT_BRACE)) {
            return literal(token);
        }

        List<Node> nodes = new ArrayList<>();

        while (!eof() && !isPeek(RIGHT_BRACE)) {
            Node node = doParse(advance());
            nodes.add(node);
        }

        if (eof()) {
            throw new ParseError("Unterminated sequence.", peek());
        }

        // throw away the closing brace after determining whether this is unterminated or not
        advance();

        return new Sequence(token, nodes.toArray(new Node[0]));
    }

    /**
     * Literal value, e.g. 5, true, "a string"
     */
    private Node literal(Token token) {
        if (is(token,
                STRING_LITERAL,
                NUMBER_LITERAL,
                BOOLEAN_LITERAL)) {

            return new Literal(token);
        }

        return binaryOperator(token);
    }

    /**
     * Operators that require two inputs, e.g. +, /, -
     */
    private Node binaryOperator(Token token) {
        if (is(token,
                PLUS,
                MINUS,
                STAR,
                SLASH,
                MOD)) {
            return new BinaryOperator(token);
        }

        return condition(token);
    }

    /**
     * Conditions are either two question marks or a single question mark
     *
     * Equivalent to the classic 'if' and 'if then'
     */
    private Node condition(Token token) {
        if (is(token, QUESTION_MARK, DOUBLE_QUESTION_MARK)) {
            return new Condition(token);
        }

        return comparison(token);
    }

    /**
     * Comparisons, ==, =>, <, etc.
     */
    private Node comparison(Token token) {

        if (is(token,
                LESS_THAN,
                LESS_THAN_EQUAL,
                EQUAL_EQUAL,
                GREATER_THAN,
                GREATER_THAN_EQUAL,
                NOT_EQUAL,
                OR,
                AND)) {
            return new Comparison(token);
        }

        return identifier(token);
    }

    /**
     * An identifier, a function call or function name
     */
    private Node identifier(Token token) {
        if (is(token, IDENTIFIER)) {

            Token namespace = null;

            // check for a dot following an identifier, this means
            // that it's a reference to an identifier contained in a namespace

            if (isPeek(DOT)) {
                advance();
                namespace = token;
                token = expect(IDENTIFIER, "Expecting identifier after '.'.");
            }

            return new Identifier(token, namespace);
        }

        if (is(token, IMPORT)) {
            return doImport(token);
        }

        return array(token);
    }

    /**
     * Array literal
     *
     * A list of values delimited by square brackets: [1,2,3]
     *
     * Dangling comma is allowed
     */
    private Node array(Token token) {
        if (!is(token, LEFT_SQ_BRACKET)) {
            unexpectedToken(token);
        }

        List<Object> initialContents = new ArrayList<>();
        Token next = advance();

        while (true) {
            if (!is(next, NUMBER_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL)) {
                break;
            }
            initialContents.add(next.getLiteral());
            next = advance();

            if (is(next, COMMA)) {
                next = advance();
            } else {
                break;
            }
        }

        if (!is(next, RIGHT_SQ_BRACKET)) {
            throw new ParseError("Expecting closing ']' after array definition.", next);
        }

        return new Array(token, initialContents.isEmpty() ? Array.EMPTY : initialContents.toArray());
    }

    private void unexpectedToken(Token token) {
        throw new ParseError(String.format("Unexpected token: '%s' (%s).", token.getLexeme(), token.getType()), token);
    }

    /**
     * Parsing the forms:
     *
     * import print from <io>
     * import * from <io>
     * import print from <io> as myprint
     * import print, println from <io>
     * import <io>
     *
     * import * from <io> as myio <<--- NO
     */
    private Node doImport(Token token) {

        // import <io>
        if (isPeek(LESS_THAN, STRING_LITERAL)) {
            return new Import(token, getImportNamespace(), Import.NO_TARGET, null);
        }

        String[] target = getImportTarget();
        String alias = null;

        expect(FROM, "Expecting 'from' after import target.");

        String namespace;
        if (isPeek(LESS_THAN, STRING_LITERAL)) {
            namespace = getImportNamespace();
        } else {
            throw new ParseError("Expecting namespace after 'from'.", peek());
        }

        // import print from <io> as myprint
        if (isPeek(AS)) {
            if (target[0].equals("*")) {
                throw new ParseError("Cannot alias whole namespace.", peek());
            }

            advance();
            alias = expect(IDENTIFIER, "Expecting identifier to use as alias after 'as'.").getLexeme();
        }

        return new Import(token, namespace, target, alias);
    }

    private String getImportNamespace() {
        String importNamespace;
        boolean internalImport = isPeek(LESS_THAN);

        if (internalImport) {
            advance();
            importNamespace = advance().getLexeme();
            expect(GREATER_THAN, "Expecting '>' after namespace identifier.");
        } else {
            importNamespace = advance().getLiteral().toString();
            assertValidImportFile(importNamespace);
        }

        return importNamespace;
    }

    private void assertValidImportFile(String file) {
        if (!file.endsWith(".twl")) {
            throw new ParseError("External file imports must end with '.twl'.", peek());
        }
        final String validChars = "abcdefghijklmnopqrstuvwxyz0123456789-_.:";

        for (int i = 0; i < file.length(); i++) {
            char character = file.charAt(i);
            if (validChars.indexOf(character) < 0) {
                throw new ParseError(String.format("Forbidden character in external file import: '%s' -> '%s'", file, character), peek());
            }
        }
    }

    private String[] getImportTarget() {

        if (!isPeek(STAR, IDENTIFIER)) {
            throw new ParseError("Expecting target after 'import'.", peek());
        }

        if (isPeek(STAR)) {
            return new String[]{advance().getLexeme()};
        }

        List<String> targets = new ArrayList<>();
        // handle comma separated list
        // import print, println from <io>
        while (true) {

            if (isPeek(STAR)) {
                throw new ParseError("Cannot mix specific identifiers and '*' in import.", peek());
            }

            if (isPeek(IDENTIFIER)) {
                targets.add(advance().getLexeme());

                if (isPeek(COMMA)) {
                    advance();
                    continue;
                }
            }

            break;
        }

        return targets.toArray(new String[0]);
    }

    private boolean is(Token token, Token.TokenType... types) {
        for (Token.TokenType type : types) {
            if (token.getType() == type) {
                return true;
            }
        }
        return false;
    }

    private boolean isPeek(Token.TokenType... types) {
        return is(peek(), types);
    }
}
