package towel.stdlib.debug;

import towel.*;

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
    public Class[] getPreConditions() {
        return NO_STACK_CONDITION;
    }

    @Override
    public Class[] getPostConditions() {
        return NO_STACK_CONDITION;
    }

    @Override
    public void call(Interpreter interpreter, Namespace namespace) {
        ps.print(interpreter.stack.toString() + "\n");
    }

    @Override
    public void setPrintStream(PrintStream stream) {
        ps = stream;
    }
}
