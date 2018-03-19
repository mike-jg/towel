package towel.stdlib.io;

import towel.*;

import java.io.PrintStream;

/**
 * Print to the console followed by a newline
 */
@LibraryMetadata(
        name = "println",
        namespace = "io"
)
public class PrintLn implements TowelFunction, RequiresPrintStream {

    private PrintStream out;

    @Override
    public void setPrintStream(PrintStream stream) {
        out = stream;
    }

    @Override
    public void call(Interpreter interpreter, Namespace namespace) {
        out.print(interpreter.stack.pop());
        out.print('\n');
    }

    @Override
    public Class[] getPreConditions() {
        return new Class[]{
                Object.class
        };
    }

    @Override
    public Class[] getPostConditions() {
        return NO_STACK_CONDITION;
    }
}
