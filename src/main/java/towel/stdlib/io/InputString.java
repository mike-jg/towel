package towel.stdlib.io;

import towel.*;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * Take input from the console and place it onto the stack, the input is parsed into a String
 */
@LibraryMetadata(
        name = "input_str",
        namespace = "io"
)
public class InputString implements TowelFunction, RequiresPrintStream, RequiresScanner {

    private Scanner scanner;
    private PrintStream outputStream;

    @Override
    public Class[] getPreConditions() {
        return new Class[] {
                String.class
        };
    }

    @Override
    public Class[] getPostConditions() {
        return NO_STACK_CONDITION;
    }

    @Override
    public void call(Interpreter interpreter) {
        outputStream.print(interpreter.getStack().popString());
        interpreter.getStack().push(scanner.next());
    }

    @Override
    public void setPrintStream(PrintStream stream) {
        this.outputStream = stream;
    }

    @Override
    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }
}
