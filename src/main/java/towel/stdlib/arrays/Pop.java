package towel.stdlib.arrays;

import towel.LibraryMetadata;
import towel.interpreter.FunctionExecutionError;
import towel.interpreter.Interpreter;
import towel.interpreter.TowelArray;
import towel.interpreter.TowelFunction;

/**
 * Pop a value off an array, leaving it on top of the stack
 */
@LibraryMetadata(
        namespace = "arrays",
        name = "pop"
)
public class Pop implements TowelFunction {
    @Override
    public Class[] getPreConditions() {
        return new Class[]{
                TowelArray.class
        };
    }

    @Override
    public Class[] getPostConditions() {
        return NO_STACK_CONDITION;
    }

    @Override
    public void call(Interpreter interpreter) {
        TowelArray array = interpreter.getStack().popArray();
        interpreter.getStack().push(array);

        if (array.isEmpty()) {
            throw new FunctionExecutionError("Cannot pop array of size 0.");
        }
        interpreter.getStack().push(array.pop());
    }
}

