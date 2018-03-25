package towel.stdlib.debug;


import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.RequiresPrintStream;
import towel.interpreter.TowelArray;
import towel.interpreter.TowelFunction;

import java.io.PrintStream;

/**
 * Print an array in a readable form
 */
@LibraryMetadata(
        name = "print_array",
        namespace = "debug"
)
public class PrintArray implements TowelFunction, RequiresPrintStream {

    private PrintStream ps;

    @Override
    public Class[] getPreConditions() {
        return new Class[] {
            TowelArray.class
        };
    }

    @Override
    public Class[] getPostConditions() {
        return NO_STACK_CONDITION;
    }

    @Override
    public void call(Interpreter interpreter) {
        TowelArray arr = interpreter.getStack().popArray();
        interpreter.getStack().push(arr);

        ps.print(arr.toString());
    }

    @Override
    public void setPrintStream(PrintStream stream) {
        ps = stream;
    }
}

