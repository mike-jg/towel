package towel.stdlib.io;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.RequiresPrintStream;
import towel.interpreter.StackCondition;
import towel.interpreter.TowelFunction;

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
    public void call(Interpreter interpreter) {
        out.print(interpreter.getStack().pop());
        out.print('\n');
    }

    @Override
    public StackCondition.PreCondition getPreCondition() {
        return StackCondition.preConditionFor(Object.class);
    }
}
