package towel;

import towel.ast.Program;
import towel.ast.Token;
import towel.interpreter.Interpreter;
import towel.interpreter.NamespaceLoader;
import towel.interpreter.NativeNamespaceLoader;
import towel.parser.Lexer;
import towel.parser.Parser;
import towel.pass.StaticPass;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static towel.LoggingErrorReporter.DEFAULT_LOG_NAME;

public class Assertions {

    public static Scanner scanner = new Scanner(System.in);

    public static void assertExecutesWithResult(Object expected, String code) {
        assertExecutesWithResult(expected, code, false);
    }

    public static void assertExecutesWithResultIgnoreNotices(Object expected, String code) {
        assertExecutesWithResult(expected, code, true);
    }

    private static void assertExecutesWithResult(Object expected, String code, boolean ignoreNotices) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        ErrorReporter reporter;
        if (ignoreNotices)        {
            reporter = new ExceptionThrowingErrorReporter.IgnoreNoticesErrorReporter();
        } else {
            reporter = new ExceptionThrowingErrorReporter();
        }

        List<Token> tokens = Lexer.getFor(code, reporter).tokenize();
        Program expr = Parser.getFor(tokens, reporter).parse();

        NamespaceLoader loader = new NativeNamespaceLoader(printStream, scanner);

        StaticPass.getDefaultPass(reporter).performAnalysis(expr);

        Object result = Interpreter.getFor(expr, loader, reporter).interpret();
        assertEquals(expected, result);
    }

    public static void setScannerInput(String input) throws UnsupportedEncodingException {
        setScanner(new Scanner(new ByteArrayInputStream(input.getBytes("utf-8"))));
    }

    public static void setScanner(Scanner scanner) {
        Assertions.scanner = scanner;
    }

    public static void assertExecutesWithOutput(Object expected, String code) {
        assertExecutesWithOutput(expected, code, false);
    }

    public static void assertExecutesWithOutputIgnoreNotices(Object expected, String code) {
        assertExecutesWithOutput(expected, code, true);
    }

    private static void assertExecutesWithOutput(Object expected, String code, boolean ignoreNotices) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        ErrorReporter reporter;
        if (ignoreNotices) {
            reporter = new ExceptionThrowingErrorReporter.IgnoreNoticesErrorReporter();
        } else {
            reporter = new ExceptionThrowingErrorReporter();
        }

        List<Token> tokens = Lexer.getFor(code, reporter).tokenize();
        Program expr = Parser.getFor(tokens, reporter).parse();

        NamespaceLoader loader = new NativeNamespaceLoader(printStream, scanner);

        StaticPass.getDefaultPass(reporter).performAnalysis(expr);

        Object result = Interpreter.getFor(expr, loader, reporter).interpret();
        assertEquals(expected, outputStream.toString());
    }

    public static void assertExecutesWithError(String expected, String code) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        LoggingErrorReporter reporter = new LoggingErrorReporter();

        List<Token> tokens = Lexer.getFor(code, reporter).tokenize();
        Program expr = Parser.getFor(tokens, reporter).parse();

        NamespaceLoader loader = new NativeNamespaceLoader(printStream, scanner);

        StaticPass.getDefaultPass(reporter).performAnalysis(expr);

        Object result = Interpreter.getFor(expr, loader, reporter).interpret();

        assertEquals(1, reporter.getErrors().get(DEFAULT_LOG_NAME).size());
        assertEquals(expected, reporter.getErrors().get(DEFAULT_LOG_NAME).get(0).message);
    }

    public static void assertParsesWithError(String expected, String code) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        LoggingErrorReporter reporter = new LoggingErrorReporter();

        List<Token> tokens = Lexer.getFor(code, reporter).tokenize();
        Program expr = Parser.getFor(tokens, reporter).parse();

        assertEquals(1, reporter.getErrors().get(DEFAULT_LOG_NAME).size(), "Expecting: " + expected);

        String message0 = reporter.getErrors().get(DEFAULT_LOG_NAME).get(0).message;

        assertStartsWith(expected, message0);
    }

    public static void assertStartsWith(String expect, String actual) {
        assertEquals(expect, actual.substring(0, Math.min(actual.length(), expect.length())));
    }

    public static void assertExecutesWithAnyOneError(String code) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        LoggingErrorReporter reporter = new LoggingErrorReporter();

        List<Token> tokens = Lexer.getFor(code, reporter).tokenize();
        Program expr = Parser.getFor(tokens, reporter).parse();

        NamespaceLoader loader = new NativeNamespaceLoader(printStream, scanner);

        StaticPass.getDefaultPass(reporter).performAnalysis(expr);

        Object result = Interpreter.getFor(expr, loader, reporter).interpret();

        assertEquals(1, reporter.getErrors().get(DEFAULT_LOG_NAME).size());
    }

    public static void assertAnalysisWithError(String code, String... expectedError) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        LoggingErrorReporter reporter = new LoggingErrorReporter();

        List<Token> tokens = Lexer.getFor(code, reporter).tokenize();
        Program expr = Parser.getFor(tokens, reporter).parse();
        NamespaceLoader loader = new NativeNamespaceLoader(printStream, scanner);
        StaticPass.getDefaultPass(reporter).performAnalysis(expr);

        assertEquals(expectedError.length, reporter.getErrors().get(DEFAULT_LOG_NAME).size());

        int i = 0;
        for (String error : expectedError) {
            assertEquals(error, reporter.getErrors().get(DEFAULT_LOG_NAME).get(i).message);
            i++;
        }
    }

    public static void assertAnalysisWithNotice(String code, String... expectedError) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        LoggingErrorReporter reporter = new LoggingErrorReporter();

        List<Token> tokens = Lexer.getFor(code, reporter).tokenize();
        Program expr = Parser.getFor(tokens, reporter).parse();
        NamespaceLoader loader = new NativeNamespaceLoader(printStream, scanner);
        StaticPass.getDefaultPass(reporter).performAnalysis(expr);

        assertEquals(expectedError.length, reporter.getNotices().get(DEFAULT_LOG_NAME).size());
        int i = 0;
        for (String error : expectedError) {
            assertEquals(error, reporter.getNotices().get(DEFAULT_LOG_NAME).get(i).message);
            i++;
        }
    }

}
