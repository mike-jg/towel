package towel.stdlib.io;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.RequiresPrintStream;
import towel.interpreter.StackCondition;
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
    public StackCondition.PreCondition getPreCondition() {
        return StackCondition.preConditionFor(Object.class);
    }

    @Override
    public StackCondition.PostCondition getPostCondition() {
        return StackCondition.postConditionFor();
    }
}
