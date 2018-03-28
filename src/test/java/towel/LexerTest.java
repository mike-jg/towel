package towel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import towel.ast.Token;
import towel.parser.Lexer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static towel.Assertions.assertStartsWith;
import static towel.LoggingErrorReporter.DEFAULT_LOG_NAME;

public class LexerTest {

    private Lexer createLexer(String source) {
        return Lexer.getFor(source, new ExceptionThrowingErrorReporter());
    }

    private Lexer createLexer(String source, ErrorReporter reporter) {
        return Lexer.getFor(source, reporter);
    }

    @ParameterizedTest
    @MethodSource("lexerProvider")
    public void testLexer(String code, List<Token.TokenType> expect) {
        Lexer lexer = createLexer(code);
        List<Token> tokens = lexer.tokenize();
        assertTokenTypes(expect.toArray(new Token.TokenType[0]), tokens.toArray(new Token[0]));
    }

    public static Stream<Arguments> lexerProvider() {
        return Stream.of(
                Arguments.of(" [  ] array ", Arrays.asList(Token.TokenType.LEFT_SQ_BRACKET, Token.TokenType.RIGHT_SQ_BRACKET, Token.TokenType.ARRAY, Token.TokenType.EOF)),
                Arguments.of(" . ", Arrays.asList(Token.TokenType.DOT, Token.TokenType.EOF)),
                Arguments.of(" public def ", Arrays.asList(Token.TokenType.PUBLIC, Token.TokenType.DEF, Token.TokenType.EOF))
        );
    }

    @ParameterizedTest
    @MethodSource("badLexerProvider")
    public void testLexerErrors(String expect, String code) {
        LoggingErrorReporter reporter = new LoggingErrorReporter();
        Lexer lexer = createLexer(code, reporter);
        List<Token> tokens = lexer.tokenize();

        assertStartsWith(expect, reporter.getErrors().get(DEFAULT_LOG_NAME).get(0).message);
    }

    public static Stream<Arguments> badLexerProvider() {
        return Stream.of(
                Arguments.of("Unexpected character '&'.", " & "),
                Arguments.of("Unexpected character '|'.", " | "),
                Arguments.of("Unexpected character '!'.", " ! ")
        );
    }

    @Test
    public void testEmpty() {
        Lexer lexer = createLexer("   ");

        List<Token> tokens = lexer.tokenize();
        assertEquals(1, tokens.size());
        assertEquals(Token.TokenType.EOF, tokens.get(0).getType());
    }

    @Test
    public void testUnterminatedCommentAtEof() {
        Lexer lexer = createLexer("/* *");

        List<Token> tokens = lexer.tokenize();
        assertEquals(1, tokens.size());
        assertEquals(Token.TokenType.EOF, tokens.get(0).getType());
    }

    @Test
    public void testUnterminatedCommentImmediateToken() {
        Lexer lexer = createLexer("/* */1");

        List<Token> tokens = lexer.tokenize();
        assertEquals(2, tokens.size());
        assertEquals(Token.TokenType.NUMBER_LITERAL, tokens.get(0).getType());
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testComment() {
        Lexer lexer = createLexer("/* test comment */");

        List<Token> tokens = lexer.tokenize();
        assertEquals(1, tokens.size());
        assertEquals(Token.TokenType.EOF, tokens.get(0).getType());
    }

    @Test
    public void testMultilineCommentIncrementsLineNumbers() {
        String source = "/**\n" +
                " *\n" +
                " * some nice documentation\n" +
                " *\n" +
                " */\n" +
                "true";

        int expect = 6;

        Lexer lexer = createLexer(source);
        List<Token> tokens = lexer.tokenize();
        assertEquals(expect, tokens.get(0).getLine());
        assertEquals(Token.TokenType.BOOLEAN_LITERAL, tokens.get(0).getType());
    }

    @Test
    public void testBooleanLiteral() {
        Lexer lexer = createLexer("true false\nfalse true");

        List<Token> tokens = lexer.tokenize();

        Token expect[] = {
                Token.create(Token.TokenType.BOOLEAN_LITERAL, "true", true, 1, 1, 0),
                Token.create(Token.TokenType.BOOLEAN_LITERAL, "false", false, 1, 6, 5),
                Token.create(Token.TokenType.BOOLEAN_LITERAL, "false", false, 2, 1, 11),
                Token.create(Token.TokenType.BOOLEAN_LITERAL, "true", true, 2, 7, 17),
                Token.create(Token.TokenType.EOF, "\0", "\0", 2, 11, 21),
        };

        assertTokens(expect, tokens.toArray(new Token[0]));
    }

    @Test
    public void testIntegerLiteral() {
        Lexer lexer = createLexer("5 100");

        List<Token> tokens = lexer.tokenize();

        Token expect[] = {
                Token.create(Token.TokenType.NUMBER_LITERAL, "5", 5d, 1, 1, 0),
                Token.create(Token.TokenType.NUMBER_LITERAL, "100", 100d, 1, 3, 2),
                Token.create(Token.TokenType.EOF, "\0", "\0", 1, 6, 5),
        };

        assertTokens(expect, tokens.toArray(new Token[0]));
    }

    @Test
    public void testIntegerLiteral2() {
        Lexer lexer = createLexer("-6");

        List<Token> tokens = lexer.tokenize();

        Token expect[] = {
                Token.create(Token.TokenType.NUMBER_LITERAL, "-6", -6d, 1, 1, 0),
                Token.create(Token.TokenType.EOF, "\0", "\0", 1, 3, 2),
        };

        assertTokens(expect, tokens.toArray(new Token[0]));
    }

    @Test
    public void testNumberLiteral() {
        Lexer lexer = createLexer("1.90\n" +
                "5");

        List<Token> tokens = lexer.tokenize();

        Token expect[] = {
                Token.create(Token.TokenType.NUMBER_LITERAL, "1.90", 1.9d, 1, 1, 0),
                Token.create(Token.TokenType.NUMBER_LITERAL, "5", 5d, 2, 1, 5),
                Token.create(Token.TokenType.EOF, "\0", "\0", 2, 2, 6),
        };

        assertTokens(expect, tokens.toArray(new Token[0]));
    }

    @Test
    public void testNumberLiteral2() {
        Lexer lexer = createLexer("1.5 534.33 -42.999 -80.0 5 50 -7");

        List<Token> tokens = lexer.tokenize();

        Token expect[] = {
                Token.create(Token.TokenType.NUMBER_LITERAL, "1.5", 1.5d, 0, 0, 0),
                Token.create(Token.TokenType.NUMBER_LITERAL, "534.33", 534.33d, 0, 0, 0),
                Token.create(Token.TokenType.NUMBER_LITERAL, "-42.999", -42.999d, 0, 0, 0),
                Token.create(Token.TokenType.NUMBER_LITERAL, "-80.0", -80d, 0, 0, 0),
                Token.create(Token.TokenType.NUMBER_LITERAL, "5", 5d, 0, 0, 0),
                Token.create(Token.TokenType.NUMBER_LITERAL, "50", 50d, 0, 0, 0),
                Token.create(Token.TokenType.NUMBER_LITERAL, "-7", -7d, 0, 0, 0),
                Token.create(Token.TokenType.EOF, "\0", "\0", 0, 0, 0),
        };

        assertTokenTypesAndLiterals(expect, tokens.toArray(new Token[0]));
    }

    @Test
    public void testStringLiteral() {
        Lexer lexer = createLexer("\"string literal\"");

        List<Token> tokens = lexer.tokenize();

        Token expect = Token.create(Token.TokenType.STRING_LITERAL, "\"string literal\"", "string literal", 1, 1, 0);

        assertEquals(2, tokens.size());
        assertToken(expect, tokens.get(0));
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testStringLiteral2() {
        Lexer lexer = createLexer(
                " \"string literal\"\n \"another\""
        );

        List<Token> tokens = lexer.tokenize();

        Token expect[] = {
                Token.create(Token.TokenType.STRING_LITERAL, "\"string literal\"", "string literal", 1, 2, 1),
                Token.create(Token.TokenType.STRING_LITERAL, "\"another\"", "another", 2, 2, 19),
                Token.create(Token.TokenType.EOF, "\0", "\0", 2, 11, 28),
        };

        assertTokens(expect, tokens.toArray(new Token[0]));
    }

    @Test
    public void testUnterminatedStringLiteral() {
        LoggingErrorReporter l = new LoggingErrorReporter();
        Lexer lexer = createLexer("\"string", l);
        lexer.tokenize();

        assertEquals(1, l.getErrors().size());
        assertEquals("Unterminated string literal. At line 1, character 8.", l.getErrors().get(DEFAULT_LOG_NAME).get(0).message);
    }

    @Test
    public void testBadChar() {
        LoggingErrorReporter l = new LoggingErrorReporter();
        Lexer lexer = createLexer("\"string\" 4 5 ` ", l);
        lexer.tokenize();

        assertEquals(1, l.getErrors().size());
        assertEquals("Unexpected character '`'. At line 1, character 15.", l.getErrors().get(DEFAULT_LOG_NAME).get(0).message);
    }

    @Test
    public void testSlash() {
        Lexer lexer = createLexer("/");

        Token expect = Token.create(Token.TokenType.SLASH, "/", "/", 1, 1, 0);

        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertToken(expect, tokens.get(0));
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testLeftBrace() {
        Lexer lexer = createLexer("{");

        Token expect = Token.create(Token.TokenType.LEFT_BRACE, "{", "{", 1, 1, 0);

        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertToken(expect, tokens.get(0));
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testRightBrace() {
        Lexer lexer = createLexer("}");

        Token expect = Token.create(Token.TokenType.RIGHT_BRACE, "}", "}", 1, 1, 0);

        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertToken(expect, tokens.get(0));
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testArrow() {
        Lexer lexer = createLexer("->");

        Token expect = Token.create(Token.TokenType.ARROW, "->", "->", 1, 1, 0);

        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertToken(expect, tokens.get(0));
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testMinus() {
        Lexer lexer = createLexer("-");

        Token expect = Token.create(Token.TokenType.MINUS, "-", "-", 1, 1, 0);

        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertToken(expect, tokens.get(0));
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testPlus() {
        Lexer lexer = createLexer("+");

        Token expect = Token.create(Token.TokenType.PLUS, "+", "+", 1, 1, 0);

        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertToken(expect, tokens.get(0));
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testStar() {
        Lexer lexer = createLexer("*");

        Token expect = Token.create(Token.TokenType.STAR, "*", "*", 1, 1, 0);

        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertToken(expect, tokens.get(0));
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testLessThan() {
        Lexer lexer = createLexer("<");

        Token expect = Token.create(Token.TokenType.LESS_THAN, "<", "<", 1, 1, 0);

        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertToken(expect, tokens.get(0));
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testLessThanEqual() {
        Lexer lexer = createLexer("<=");

        Token expect = Token.create(Token.TokenType.LESS_THAN_EQUAL, "<=", "<=", 1, 1, 0);

        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertToken(expect, tokens.get(0));
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testQuestionMark() {
        Lexer lexer = createLexer("?");

        Token expect = Token.create(Token.TokenType.QUESTION_MARK, "?", "?", 1, 1, 0);

        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertToken(expect, tokens.get(0));
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testGreaterThan() {
        Lexer lexer = createLexer(">");

        Token expect = Token.create(Token.TokenType.GREATER_THAN, ">", ">", 1, 1, 0);

        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertToken(expect, tokens.get(0));
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testGreaterThanEqual() {
        Lexer lexer = createLexer(">=");

        Token expect = Token.create(Token.TokenType.GREATER_THAN_EQUAL, ">=", ">=", 1, 1, 0);

        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertToken(expect, tokens.get(0));
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testEqual() {
        Lexer lexer = createLexer("=");

        Token expect = Token.create(Token.TokenType.EQUAL, "=", "=", 1, 1, 0);

        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertToken(expect, tokens.get(0));
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testEqualEqual() {
        Lexer lexer = createLexer("==");

        Token expect = Token.create(Token.TokenType.EQUAL_EQUAL, "==", "==", 1, 1, 0);

        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertToken(expect, tokens.get(0));
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testNotEqual() {
        Lexer lexer = createLexer("!=");

        Token expect = Token.create(Token.TokenType.NOT_EQUAL, "!=", "!=", 1, 1, 0);
        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertToken(expect, tokens.get(0));
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testImport() {
        Lexer lexer = createLexer("import");

        Token expect = Token.create(Token.TokenType.IMPORT, "import", "import", 1, 1, 0);
        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertToken(expect, tokens.get(0));
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testModCommaLet() {
        Lexer lexer = createLexer("%,let ??");
        List<Token> tokens = lexer.tokenize();
        assertEquals(5, tokens.size());

        assertEquals(Token.TokenType.MOD, tokens.get(0).getType());
        assertEquals(Token.TokenType.COMMA, tokens.get(1).getType());
        assertEquals(Token.TokenType.LET, tokens.get(2).getType());
        assertEquals(Token.TokenType.DOUBLE_QUESTION_MARK, tokens.get(3).getType());
        assertEquals(Token.TokenType.EOF, tokens.get(4).getType());
    }

    @Test
    public void testAndAndOr() {
        Lexer lexer = createLexer("|| &&");
        List<Token> tokens = lexer.tokenize();
        assertEquals(3, tokens.size());

        assertEquals(Token.TokenType.OR, tokens.get(0).getType());
        assertEquals(Token.TokenType.AND, tokens.get(1).getType());
        assertEquals(Token.TokenType.EOF, tokens.get(2).getType());
    }

    @Test
    public void testIntBoolStr() {
        Lexer lexer = createLexer("num bool str ( ) -> seq void any ");
        List<Token> tokens = lexer.tokenize();
        assertEquals(10, tokens.size());

        assertEquals(Token.TokenType.NUM, tokens.get(0).getType());
        assertEquals(Token.TokenType.BOOL, tokens.get(1).getType());
        assertEquals(Token.TokenType.STR, tokens.get(2).getType());
        assertEquals(Token.TokenType.LEFT_BRACKET, tokens.get(3).getType());
        assertEquals(Token.TokenType.RIGHT_BRACKET, tokens.get(4).getType());
        assertEquals(Token.TokenType.ARROW, tokens.get(5).getType());
        assertEquals(Token.TokenType.SEQ, tokens.get(6).getType());
        assertEquals(Token.TokenType.VOID, tokens.get(7).getType());
        assertEquals(Token.TokenType.ANY, tokens.get(8).getType());
        assertEquals(Token.TokenType.EOF, tokens.get(9).getType());
    }

    @Test
    public void testDef() {
        Lexer lexer = createLexer("def");

        Token expect = Token.create(Token.TokenType.DEF, "def", "def", 1, 1, 0);
        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertToken(expect, tokens.get(0));
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testIdentifier() {
        Lexer lexer = createLexer("some identifiers here underscore_too PascalCase dash-in-identifier ");

        List<Token> tokens = lexer.tokenize();

        Token expect[] = {
                Token.create(Token.TokenType.IDENTIFIER, "some", "some", 0, 0, 0),
                Token.create(Token.TokenType.IDENTIFIER, "identifiers", "identifiers", 0, 0, 0),
                Token.create(Token.TokenType.IDENTIFIER, "here", "here", 0, 0, 0),
                Token.create(Token.TokenType.IDENTIFIER, "underscore_too", "underscore_too", 0, 0, 0),
                Token.create(Token.TokenType.IDENTIFIER, "PascalCase", "PascalCase", 0, 0, 0),
                Token.create(Token.TokenType.IDENTIFIER, "dash-in-identifier", "dash-in-identifier", 0, 0, 0),
                Token.create(Token.TokenType.EOF, "\0", "\0", 0, 0, 0),
        };

        assertTokenTypesAndLiterals(expect, tokens.toArray(new Token[0]));
    }

    private void assertToken(Token expected, Token actual) {
        assertEquals(expected.toString(), actual.toString());
        assertEquals(expected.getCharacter(), actual.getCharacter());
        assertEquals(expected.getLine(), actual.getLine());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getLexeme(), actual.getLexeme());
        assertEquals(expected.getLiteral(), actual.getLiteral());
    }

    private void assertTokens(Token[] expected, Token[] actual) {
        assertEquals(expected.length, actual.length);

        for (int i = 0; i < expected.length; i++) {
            assertToken(expected[i], actual[i]);
        }
    }

    private void assertTokenTypes(Token.TokenType[] expected, Token[] actual) {
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i].getType());
        }
    }

    private void assertTokenTypesAndLiterals(Token[] expected, Token[] actual) {
        assertEquals(expected.length, actual.length);

        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].getLiteral(), actual[i].getLiteral());
            assertEquals(expected[i].getLexeme(), actual[i].getLexeme());
            assertEquals(expected[i].getType(), actual[i].getType());
        }
    }

}
