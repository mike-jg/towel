package towel.stdlib.debug;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.RequiresPrintStream;
import towel.interpreter.TowelFunction;

import java.io.PrintStream;

/**
 * Print the current stack
 */
@LibraryMetadata(
        name = "print_stack",
        namespace = "debug"
)
public class PrintStack implements TowelFunction, RequiresPrintStream {

    private PrintStream ps;

    @Override
    public void call(Interpreter interpreter) {
        ps.print(interpreter.getStack().toString() + "\n");
    }

    @Override
    public void setPrintStream(PrintStream stream) {
        ps = stream;
    }
}
