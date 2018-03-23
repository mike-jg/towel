package towel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import towel.ast.*;

import java.util.ArrayList;
import java.util.Scanner;

public class InterpreterTest {

    private Program program;

    @BeforeEach
    public void createProgram() {
        program = new Program("", new ArrayList<Node>(), new ArrayList<Import>());
    }

    @Test
    public void testInterpreterThrowsForNonExistentIdentifier() {
        LoggingErrorReporter reporter = new LoggingErrorReporter();
        Interpreter interpreter = new Interpreter(program, new NativeNamespaceLoader(System.out, new Scanner(System.in)), new LoggingErrorReporter());

        org.junit.jupiter.api.Assertions.assertThrows(
                Interpreter.InterpreterError.class,
                () ->  interpreter.visit(new Identifier(new Token(Token.TokenType.IDENTIFIER, "blah", "blah", -1, -1, -1), null))
        );
    }

    @Test
    public void testInterpreterThrowsForDuplicateIdentifier() {
        LoggingErrorReporter reporter = new LoggingErrorReporter();
        Namespace env = new Namespace();
        env.definePrivateMember("blah", true);
        Interpreter interpreter = new Interpreter(program, new NativeNamespaceLoader(System.out, new Scanner(System.in)), new LoggingErrorReporter(), env);

        org.junit.jupiter.api.Assertions.assertThrows(
                Interpreter.InterpreterError.class,
                () ->  interpreter.visit(new Function(new Token(Token.TokenType.DEF, "blah", "blah", -1, -1, -1), true, new Node[0], new Class[0], new Class[0]))
        );
    }

    @Test
    public void testInterpreterThrowsForInvalidFunction() {
        Namespace env = new Namespace();
        env.definePrivateMember("blah", true);

        Interpreter interpreter = new Interpreter(program, new NativeNamespaceLoader(System.out, new Scanner(System.in)), new LoggingErrorReporter(), env);

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () ->  interpreter.visit(new Identifier(new Token(Token.TokenType.IDENTIFIER, "blah", "blah", -1, -1, -1), null))
        );
    }

    @Test
    public void testInterpreterThrowsForInvalidBinaryOperator() {

        Interpreter interpreter = new Interpreter(program, new NativeNamespaceLoader(System.out, new Scanner(System.in)), new LoggingErrorReporter());

        interpreter.getStack().push(5d);
        interpreter.getStack().push(5d);

        org.junit.jupiter.api.Assertions.assertThrows(
                Interpreter.InterpreterError.class,
                () ->  interpreter.visit(new BinaryOperator(new Token(Token.TokenType.COMMA, "*", "*", -1, -1, -1)))
        );
    }

    @Test
    public void testInterpreterThrowsForInvalidComparison() {

        Interpreter interpreter = new Interpreter(program, new NativeNamespaceLoader(System.out, new Scanner(System.in)), new LoggingErrorReporter());

        interpreter.getStack().push(5d);
        interpreter.getStack().push(5d);

        org.junit.jupiter.api.Assertions.assertThrows(
                Interpreter.InterpreterError.class,
                () ->  interpreter.visit(new Comparison(new Token(Token.TokenType.COMMA, "==", "==", -1, -1, -1)))
        );
    }

}
