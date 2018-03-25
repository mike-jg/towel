package towel.stdlib.io;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.RequiresPrintStream;
import towel.interpreter.TowelFunction;

import java.io.PrintStream;

/**
 * Print to the console
 */
@LibraryMetadata(
        name = "print",
        namespace = "io"
)
public class Print implements TowelFunction, RequiresPrintStream {

    private PrintStream out;

    @Override
    public void setPrintStream(PrintStream stream) {
        out = stream;
    }

    @Override
    public void call(Interpreter interpreter) {
        out.print(interpreter.getStack().pop());
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
