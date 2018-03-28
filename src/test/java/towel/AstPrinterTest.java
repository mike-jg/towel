package towel;

import org.junit.jupiter.api.Test;
import towel.ast.Program;
import towel.ast.Token;
import towel.parser.Lexer;
import towel.parser.Parser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AstPrinterTest {

    private Parser parser;
    private LoggingErrorReporter reporter;

    private void createParser(String code) {
        reporter = new LoggingErrorReporter();
        List<Token> tokens = Lexer.getFor(code, reporter).tokenize();
        parser = Parser.getFor(tokens, reporter);
    }

    private String parseAndReturnPrintedAst(String source) {
        createParser(source);
        Program expr = parser.parse();
        AstPrinter printer = new AstPrinter();
        return printer.print(expr);
    }

    @Test
    public void testArray() {
        String output = parseAndReturnPrintedAst("[1,2,3]");

        assertEquals("(ARRAY 1.0, 2.0, 3.0)", output);
    }

    @Test
    public void testTabChar() {

        createParser("def myfunc { 1 2 }");
        Program expr = parser.parse();
        AstPrinter printer = new AstPrinter("    ");

        String output = printer.print(expr);

        assertEquals("(DEF\n" +
                "    (IDENTIFIER myfunc)\n" +
                "        (NUMBER_LITERAL 1.0)\n        (NUMBER_LITERAL 2.0)\n" +
                ")", output);
    }

    @Test
    public void testNonDefaultProgram() {

        reporter = new LoggingErrorReporter();
        List<Token> tokens = Lexer.getFor("def myfunc { 1 2 }", reporter).tokenize();
        parser = Parser.getFor(tokens, reporter, "mytest");

        Program expr = parser.parse();
        AstPrinter printer = new AstPrinter();

        String output = printer.print(expr);

        assertEquals("(PROGRAM mytest\n" +
                "\t(DEF\n" +
                "\t\t(IDENTIFIER myfunc)\n" +
                "\t\t\t(NUMBER_LITERAL 1.0)\n" +
                "\t\t\t(NUMBER_LITERAL 2.0)\n" +
                "\t)\n" +
                ")", output);
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
