package towel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static towel.Assertions.assertParsesWithError;

public class ParserTest {


    private Parser parser;
    private LoggingErrorReporter reporter;

    private void createParser(String code) {
        reporter = new LoggingErrorReporter();
        List<Token> tokens = new Lexer(code, reporter).tokenize();
        parser = new Parser(tokens, reporter);
    }


    @Test
    public void testLetFailsNoIdentifier() {
        assertParsesWithError("Expecting identifier after 'let'.", "let 5");
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
            Arguments.of("import * from as", "Expecting namespace after 'from'."),
            Arguments.of("import * as", "Expecting 'from' after import target."),
            Arguments.of("import *", "Expecting 'from' after import target."),
            Arguments.of("import print, * from <io>", "Cannot mix specific identifiers and '*' in import."),
            Arguments.of("import", "Expecting target after 'import'."),
            Arguments.of("import *", "Expecting 'from' after import target."),
            Arguments.of("import fred", "Expecting 'from' after import target."),
            Arguments.of("import fred from", "Expecting namespace after 'from'."),
            Arguments.of("import fred as", "Expecting 'from' after import target."),
            Arguments.of("import \"bad\"", "External file imports must end with '.twl'."),
            Arguments.of("import <bad.twl>", "Expecting '>' after namespace identifier.")
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

}
