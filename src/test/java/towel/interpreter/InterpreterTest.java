package towel.interpreter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import towel.LoggingErrorReporter;
import towel.parser.*;

import java.util.Scanner;

import static org.mockito.Mockito.*;


public class InterpreterTest {

    private Program program;

    @BeforeEach
    public void createProgram() {
        program = mock(Program.class);
    }

    @Test
    public void testInterpreterThrowsForNonExistentIdentifier() {
        Interpreter interpreter = Interpreter.getFor(program, new NativeNamespaceLoader(System.out, new Scanner(System.in)), new LoggingErrorReporter());

        Identifier ident = mock(Identifier.class);

        when(ident.isNamespaced()).thenReturn(false);
        when(ident.getName()).thenReturn("blah");
        when(ident.getOriginalName()).thenReturn("blah");
        when(ident.accept(any())).thenCallRealMethod();

        org.junit.jupiter.api.Assertions.assertThrows(
                ProgramInterpreter.InterpreterError.class,
                () ->  interpreter.interpret(new Node[]{ ident })
        );
    }

    @Test
    public void testInterpreterThrowsForDuplicateIdentifier() {
        Namespace env = new Namespace();
        env.definePrivateMember("blah", true);
        Interpreter interpreter = Interpreter.getFor(program, new NativeNamespaceLoader(System.out, new Scanner(System.in)), new LoggingErrorReporter(), env);

        Token token = mock(Token.class);
        when(token.getLexeme()).thenReturn("blah");
        Function func = mock(Function.class);
        when(func.getToken()).thenReturn(token);
        when(func.accept(any())).thenCallRealMethod();

        org.junit.jupiter.api.Assertions.assertThrows(
                ProgramInterpreter.InterpreterError.class,
                () ->  interpreter.interpret(new Node[]{ func })
        );
    }

    @Test
    public void testInterpreterThrowsForInvalidFunction() {
        Namespace env = new Namespace();
        env.definePrivateMember("blah", true);

        Interpreter interpreter = Interpreter.getFor(program, new NativeNamespaceLoader(System.out, new Scanner(System.in)), new LoggingErrorReporter(), env);

        Identifier ident = mock(Identifier.class);
        when(ident.isNamespaced()).thenReturn(false);
        when(ident.getName()).thenReturn("blah");
        when(ident.getOriginalName()).thenReturn("blah");
        when(ident.accept(any())).thenCallRealMethod();

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () ->  interpreter.interpret(new Node[]{ ident })
        );
    }

    @Test
    public void testInterpreterThrowsForInvalidBinaryOperator() {

        Interpreter interpreter = Interpreter.getFor(program, new NativeNamespaceLoader(System.out, new Scanner(System.in)), new LoggingErrorReporter());

        interpreter.getStack().push(5d);
        interpreter.getStack().push(5d);

        Token comma = Token.create(Token.TokenType.COMMA, "*", "*", -1, -1, -1);

        BinaryOperator ident = mock(BinaryOperator.class);
        when(ident.getLexeme()).thenReturn("*");
        when(ident.getLiteralAsString()).thenReturn("*");
        when(ident.getTokenType()).thenReturn(comma.getType());
        when(ident.getToken()).thenReturn(comma);
        when(ident.accept(any())).thenCallRealMethod();

        org.junit.jupiter.api.Assertions.assertThrows(
                ProgramInterpreter.InterpreterError.class,
                () -> interpreter.interpret(new Node[]{ ident })
        );
    }

    @Test
    public void testInterpreterThrowsForInvalidComparison() {

        Interpreter interpreter = Interpreter.getFor(program, new NativeNamespaceLoader(System.out, new Scanner(System.in)), new LoggingErrorReporter());

        interpreter.getStack().push(5d);
        interpreter.getStack().push(5d);

        Token token = mock(Token.class);
        when(token.getLexeme()).thenReturn("blah");
        Comparison comp = mock(Comparison.class);
        when(comp.getToken()).thenReturn(token);
        when(comp.getTokenType()).thenReturn(Token.TokenType.STRING_LITERAL);
        when(comp.accept(any())).thenCallRealMethod();

        org.junit.jupiter.api.Assertions.assertThrows(
                ProgramInterpreter.InterpreterError.class,
                () ->  interpreter.interpret(new Node[]{ comp })
        );
    }

}
