package towel.stdlib.io;

import towel.LibraryMetadata;
import towel.interpreter.*;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * Take input from the console and place it onto the stack, the input is parsed into a Double
 */
@LibraryMetadata(
        name = "input_num",
        namespace = "io"
)
public class InputNumber implements TowelFunction, RequiresScanner, RequiresPrintStream {

    private Scanner scanner;
    private PrintStream outputStream;

    @Override
    public StackCondition.PreCondition getPreCondition() {
        return StackCondition.preConditionFor(String.class);
    }

    @Override
    public StackCondition.PostCondition getPostCondition() {
        return StackCondition.postConditionFor();
    }

    @Override
    public void call(Interpreter interpreter) {
        outputStream.print(interpreter.getStack().popString());
        interpreter.getStack().push(scanner.nextDouble());
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
