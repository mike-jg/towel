package towel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import towel.ast.Program;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static towel.Assertions.assertParsesWithError;

public class ParserTest {


    private Parser parser;
    private FileAwareLoggingErrorReporter reporter;

    private void createParser(String code) {
        reporter = new FileAwareLoggingErrorReporter();
        List<Token> tokens = new Lexer(code, reporter).tokenize();
        parser = new Parser(tokens, reporter);
    }

    private String parseAndReturnPrintedAst(String source) {
        createParser(source);
        Program expr = parser.parse();
        AstPrinter printer = new AstPrinter();
        return printer.print(expr);
    }

    @Test
    public void testEmpty() {
        String output = parseAndReturnPrintedAst("    ");

        assertEquals(0, output.length());
    }

    @Test
    public void testNumberLiterals() {
        String output = parseAndReturnPrintedAst("1 15 2.5    ");

        assertEquals("(NUMBER_LITERAL 1.0)\n(NUMBER_LITERAL 15.0)\n(NUMBER_LITERAL 2.5)", output);
    }

    @Test
    public void testBinary() {
        String output = parseAndReturnPrintedAst("+ *");

        assertEquals("(BINARY_OPERATOR PLUS +)\n(BINARY_OPERATOR STAR *)", output);
    }

    @Test
    public void testLet() {
        String output = parseAndReturnPrintedAst("let myvar");

        assertEquals("(LET myvar)", output);
    }

    @Test
    public void testLetFailsNoIdentifier() {
        assertParsesWithError("Expecting identifier after 'let'.", "let 5");
    }

    @Test
    public void testStringLiterals() {
        String output = parseAndReturnPrintedAst("\"my str\" \"another string\"");

        assertEquals("(STRING_LITERAL \"my str\")\n(STRING_LITERAL \"another string\")", output);
    }

    @Test
    public void testBooleanLiterals() {
        String output = parseAndReturnPrintedAst("true false");

        assertEquals("(BOOLEAN_LITERAL true)\n(BOOLEAN_LITERAL false)", output);
    }

    @Test
    public void testFunction() {
        String output = parseAndReturnPrintedAst("def myfunc { 1 2 }");
        assertEquals("(DEF\n" +
                "\t(IDENTIFIER myfunc)\n" +
                "\t\t(NUMBER_LITERAL 1.0)\n\t\t(NUMBER_LITERAL 2.0)\n" +
                ")", output);
    }

    @ParameterizedTest
    @MethodSource("illegalImportProvider")
    public void testCannotMakeIllegalImport(String _import, String expected) {
        assertParsesWithError(expected, _import);
    }

    public static Stream<Arguments> illegalImportProvider() {
        return Stream.of(
            Arguments.of("import <stack> as test", "Unexpected token: 'as' (AS)."),
            Arguments.of("import * from <stack> as nope", "Cannot alias whole namespace."),
            Arguments.of("import from <stack> as nope", "Expecting target after 'import'."),
            Arguments.of("import * from <stack> as", "Cannot alias whole namespace."),
            Arguments.of("import * from as", "Expecting <namespace> after import target."),
            Arguments.of("import * as", "Expecting 'from' after import target."),
            Arguments.of("import *", "Expecting 'from' after import target."),
            Arguments.of("import print, * from <io>", "Cannot mix specific identifiers and '*' in import."),
            Arguments.of("import", "Expecting target after 'import'."),
            Arguments.of("import *", "Expecting 'from' after import target."),
            Arguments.of("import fred", "Expecting 'from' after import target."),
            Arguments.of("import fred from", "Expecting <namespace> after import target."),
            Arguments.of("import fred as", "Expecting 'from' after import target.")
        );
    }

    @ParameterizedTest
    @MethodSource("malformedFunctionProvider")
    public void testMalformedFunction(String expect, String code) {
        assertParsesWithError(expect, code);
    }

    public static Stream<Arguments> malformedFunctionProvider() {
        return Stream.of(
                Arguments.of("Expecting function name after 'def'.", "def "),
                Arguments.of("Expecting function name after 'def'.", "def  {"),
                Arguments.of("Unterminated function body.", "def myfunc {"),
                Arguments.of("Expecting type after '('.", "def myfunc ( {"),
                Arguments.of("Expecting type after '('.", "def myfunc () {"),
                Arguments.of("Expecting type after '('.", "def myfunc () { }"),
                Arguments.of("Expecting '{' after function signature definition.", "def myfunc (num -> void)"),
                Arguments.of("Expecting '{' after function signature definition.", "def myfunc"),
                Arguments.of("Expecting type after '->'.", "def myfunc (num -> ) {}"),
                Arguments.of("Expecting '->' after pre-conditions.", "def myfunc (num) {} "),
                Arguments.of("Expecting closing bracket after pre-conditions and post-conditions.", "def myfunc (num -> void {} "),
                Arguments.of("Cannot specify 'void' with multiple types.", "def myfunc (num -> str, void) {} "),
                Arguments.of("Cannot specify 'void' with multiple types.", "def myfunc (num, void -> str) {} ")
        );
    }

    @Test
    public void testThrowsExceptionForUnterminatedSequence() {
        assertParsesWithError("Unterminated sequence.", "2 3 + { 3 4 1 42");
    }

    @Test
    public void testThrowsExceptionForUnterminatedSequence2() {
        assertParsesWithError("Unterminated sequence.", "{");
    }

    @Test
    public void testFailsForUnexpectedDot() {
        assertParsesWithError("Unexpected token: '.' (DOT).", "5.16.");
    }

    @Test
    public void testSequence() {
        String output = parseAndReturnPrintedAst("{ 5 true }");
        assertEquals("(SEQUENCE LEFT_BRACE\n" +
                "\t(NUMBER_LITERAL 5.0)\n" +
                "\t(BOOLEAN_LITERAL true)\n" +
                ")", output);
    }

    @Test
    public void testComparison() {
        String output = parseAndReturnPrintedAst("15 20 ==");
        assertEquals("(NUMBER_LITERAL 15.0)\n(NUMBER_LITERAL 20.0)\n(COMPARISON (EQUAL_EQUAL ==))", output);
    }

    @Test
    public void testCondition() {
        String output = parseAndReturnPrintedAst("?");
        assertEquals("(CONDITION QUESTION_MARK)", output);
    }

    @Test
    public void testImport() {
        String output = parseAndReturnPrintedAst("import fred from <io> as barney");
        assertEquals("(IMPORT\n" +
                "\t(TARGET fred)\n" +
                "\t(FROM io)\n" +
                "\t(AS barney)\n" +
                ")", output);
    }

    @Test
    public void testIdentifier() {
        String output = parseAndReturnPrintedAst("fred");
        assertEquals("(IDENTIFIER fred)", output);
    }

    @Test
    public void testFunctionWithManyComments() {
        String expect = "(BOOLEAN_LITERAL false)\n" +
                "(SEQUENCE LEFT_BRACE\n" +
                "\t(NUMBER_LITERAL 4.0)\n" +
                "\t(BOOLEAN_LITERAL true)\n" +
                "\t(NUMBER_LITERAL 5.4)\n" +
                "\t(STRING_LITERAL \"it was true\")\n" +
                ")\n" +
                "(SEQUENCE LEFT_BRACE\n" +
                "\t(STRING_LITERAL \"it was false\")\n" +
                ")\n" +
                "(CONDITION QUESTION_MARK)\n" +
                "(DEF\n" +
                "\t(IDENTIFIER my_func)\n" +
                "\t\t(SEQUENCE LEFT_BRACE\n" +
                "\t\t\t(STRING_LITERAL \"you passed true\")\n" +
                "\t\t)\n" +
                "\t\t(SEQUENCE LEFT_BRACE\n" +
                "\t\t\t(STRING_LITERAL \"you passed false\")\n" +
                "\t\t)\n" +
                "\t\t(CONDITION QUESTION_MARK)\n" +
                ")";

        String result = parseAndReturnPrintedAst("false\n" +
                "{ 4 true 5.4 \"it was true\" }\n" +
                "{ \"it was false\" }\n" +
                "?\n" +
                "\n" +
                "def my_func {\n" +
                "    { \"you passed true\" } /* pushes this list of instructions onto the stack aka a sequence */\n" +
                "    { \"you passed false\" } /* as above */\n" +
                "    ? /* takes 3 args, a boolean and two listings,\n" +
                "         then conditional executes the first or the second based on the bool */\n" +
                "}");

        assertEquals(expect, result);
    }
}
