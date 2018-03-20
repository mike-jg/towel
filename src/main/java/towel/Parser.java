package towel;

import towel.ast.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Parse the tokens into an abstract syntax tree
 *
 * Need to consider lexerless parsing here, because the lexer has no context it adds
 * restrictions to the language. e.g. 'num' cannot be used as a function or let name because
 * it's used in the stack pre and post-condition checks and so it's a reserved word essentially.
 * The parser has all the context it needs to avoid this issue, unfortunately the lexer doesn't
 * and blindly lexes all 'num' lexemes as keywords.
 */
class Parser {

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

    Parser(List<Token> tokens, ErrorReporter reporter, String namespace) {
        this.tokens = Objects.requireNonNull(tokens);
        this.reporter = Objects.requireNonNull(reporter);
        this.namespace = Objects.requireNonNull(namespace);
    }

    Parser(List<Token> tokens, ErrorReporter reporter) {
        this(tokens, reporter, Program.DEFAULT_NAMESPACE);
    }

    /**
     * Run the parser and return the AST
     */
    Program parse() {
        while (true) {
            Token t = advance();
            if (is(t, Token.TokenType.EOF)) {
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
        // collect the import nodes when they are created in the parsing methods
        // rather than using streams on the full list of nodes
        List<Import> imports = nodes
                .stream()
                .filter(Import.class::isInstance)
                .map(Import.class::cast)
                .collect(Collectors.toCollection(ArrayList<Import>::new));

        List<FileImport> fileImports = nodes
                .stream()
                .filter(FileImport.class::isInstance)
                .map(FileImport.class::cast)
                .collect(Collectors.toCollection(ArrayList<FileImport>::new));

        return new Program(namespace, nodes, imports, fileImports);
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
        return is(tokens.get(pos), Token.TokenType.EOF);
    }

    /**
     * If the current (peek) token doesn't match the given token type, throw a parse exception
     *
     * Otherwise return the current token and advance
     */
    private Token expect(Token.TokenType token, String message) {
        if (peek().getType() != token) {
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

            if (!is(peek(), Token.TokenType.COMMA)) {
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
     * e.g.
     * def myfunc ( num, num, str -> void ) {  }
     */
    private Node function(Token token) {

        if (!is(token, Token.TokenType.DEF)) {
            return sequence(token);
        }

        List<Node> bodyNodes = new ArrayList<>();

        Token name = expect(Token.TokenType.IDENTIFIER, "Expecting function name after 'def'.");

        Class[] preConditions = TowelFunction.NO_STACK_CONDITION;
        Class[] postConditions = TowelFunction.NO_STACK_CONDITION;

        if (is(peek(), Token.TokenType.LEFT_BRACKET)) {
            advance();
            preConditions = gatherTypes("(");
            expect(Token.TokenType.ARROW, "Expecting '->' after pre-conditions.");
            postConditions = gatherTypes("->");
            expect(Token.TokenType.RIGHT_BRACKET, "Expecting closing bracket after pre-conditions and post-conditions.");
        }

        expect(Token.TokenType.LEFT_BRACE, "Expecting '{' after function signature definition.");

        while (!eof() && !is(peek(), Token.TokenType.RIGHT_BRACE)) {
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
                bodyNodes.toArray(new Node[0]),
                preConditions,
                postConditions
        );
    }

    /**
     * A sequence is just a list of things within curly braces, e.g.
     * { 4 5 + "in a sequence" }
     */
    private Node sequence(Token token) {
        if (!is(token, Token.TokenType.LEFT_BRACE)) {
            return literal(token);
        }

        List<Node> nodes = new ArrayList<>();

        while (!eof() && !is(peek(), Token.TokenType.RIGHT_BRACE)) {
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
                Token.TokenType.STRING_LITERAL,
                Token.TokenType.NUMBER_LITERAL,
                Token.TokenType.BOOLEAN_LITERAL)) {

            return new Literal(token);
        }

        return binaryOperator(token);
    }

    /**
     * Operators that require two inputs, e.g. +, /, -
     */
    private Node binaryOperator(Token token) {
        if (is(token,
                Token.TokenType.PLUS,
                Token.TokenType.MINUS,
                Token.TokenType.STAR,
                Token.TokenType.SLASH,
                Token.TokenType.MOD)) {
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
        if (is(token, Token.TokenType.QUESTION_MARK, Token.TokenType.DOUBLE_QUESTION_MARK)) {
            return new Condition(token);
        }

        return comparison(token);
    }

    /**
     * Comparisons, ==, =>, <, etc.
     */
    private Node comparison(Token token) {

        if (is(token,
                Token.TokenType.LESS_THAN,
                Token.TokenType.LESS_THAN_EQUAL,
                Token.TokenType.EQUAL_EQUAL,
                Token.TokenType.GREATER_THAN,
                Token.TokenType.GREATER_THAN_EQUAL,
                Token.TokenType.NOT_EQUAL,
                Token.TokenType.OR,
                Token.TokenType.AND)) {
            return new Comparison(token);
        }

        return identifier(token);
    }

    /**
     * An identifier, a function call or function name
     */
    private Node identifier(Token token) {
        if (is(token, Token.TokenType.IDENTIFIER)) {

            Token namespace = null;

            // check for a dot following an identifier, this means
            // that it's a reference to an identifier contained in a namespace

            if (is(peek(), Token.TokenType.DOT)) {
                advance();
                namespace = token;
                token = expect(Token.TokenType.IDENTIFIER, "Expecting identifier after '.'.");
            }

            return new Identifier(token, namespace);
        }

        if (is(token, Token.TokenType.IMPORT)) {
            return doImport(token);
        }

        return let(token);
    }

    /**
     * Let expects the keyword 'let' followed by an identifier
     */
    private Node let(Token token) {

        if (!is(token, Token.TokenType.LET)) {
            return array(token);
        }

        Token name = expect(Token.TokenType.IDENTIFIER, "Expecting identifier after 'let'.");

        return new Let(name);
    }

    /**
     * Array literal
     *
     * A list of values delimited by square brackets: [1,2,3]
     *
     * Dangling comma is allowed
     */
    private Node array(Token token) {
        if (!is(token, Token.TokenType.LEFT_SQ_BRACKET)) {
            unexpectedToken(token);
        }

        List<Object> initialContents = new ArrayList<>();
        Token next = advance();

        while (true) {
            if (!is(next, Token.TokenType.NUMBER_LITERAL, Token.TokenType.BOOLEAN_LITERAL, Token.TokenType.STRING_LITERAL)) {
                break;
            }
            initialContents.add(next.getLiteral());
            next = advance();

            if (is(next, Token.TokenType.COMMA)) {
                next = advance();
            } else {
                break;
            }
        }

        if (!is(next, Token.TokenType.RIGHT_SQ_BRACKET)) {
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

        if (is(peek(), Token.TokenType.STRING_LITERAL)) {
            return fileImport(token);
        }

        // import <io>
        if (is(peek(), Token.TokenType.LESS_THAN)) {
            advance();
            Token namespace = expect(Token.TokenType.IDENTIFIER, "Expecting namespace identifier after <.");
            expect(Token.TokenType.GREATER_THAN, "Expecting '>' after namespace identifier.");

            Import _import = new Import(token, namespace.getLexeme(), Import.NO_TARGET, null);

            return _import;
        }

        String[] target = getImportTarget();
        String alias = null;

        expect(Token.TokenType.FROM, "Expecting 'from' after import target.");
        expect(Token.TokenType.LESS_THAN, "Expecting <namespace> after import target.");
        Token namespace = expect(Token.TokenType.IDENTIFIER, "Expecting namespace identifier after <.");
        expect(Token.TokenType.GREATER_THAN, "Expecting '>' after namespace identifier.");

        // import print from <io> as myprint
        if (is(peek(), Token.TokenType.AS)) {
            if (target[0].equals("*")) {
                throw new ParseError("Cannot alias whole namespace.", peek());
            }

            advance();
            alias = expect(Token.TokenType.IDENTIFIER, "Expecting identifier to use as alias after 'as'.").getLexeme();
        }


        Import _import = new Import(token, namespace.getLexeme(), target, alias);

        return _import;
    }

    private Node fileImport(Token token) {
        Token file = expect(Token.TokenType.STRING_LITERAL, "Expecting string literal for file import.");
        String fileName = file.getLiteral().toString();

        if (!fileName.endsWith(".twl")) {
            throw new ParseError("External fileImports must have the file suffix '.twl'.", peek());
        }

        FileImport fileImport = new FileImport(token, file);
        return fileImport;
    }

    private String[] getImportTarget() {

        if (!is(peek(), Token.TokenType.STAR, Token.TokenType.IDENTIFIER)) {
            throw new ParseError("Expecting target after 'import'.", peek());
        }

        if (is(peek(), Token.TokenType.STAR)) {
            return new String[]{advance().getLexeme()};
        }

        List<String> targets = new ArrayList<>();
        // handle comma separated list
        // import print, println from <io>
        while (true) {

            if (is(peek(), Token.TokenType.STAR)) {
                throw new ParseError("Cannot mix specific identifiers and '*' in import.", peek());
            }

            if (is(peek(), Token.TokenType.IDENTIFIER)) {
                targets.add(advance().getLexeme());

                if (is(peek(), Token.TokenType.COMMA)) {
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
}
