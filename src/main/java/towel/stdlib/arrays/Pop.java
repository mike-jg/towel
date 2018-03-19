package towel.stdlib.arrays;

import towel.*;

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
    public void call(Interpreter interpreter, Namespace namespace) {
        TowelArray array = interpreter.stack.popArray();
        interpreter.stack.push(array);

        if (array.isEmpty()) {
            throw new FunctionExecutionError("Cannot pop array of size 0.");
        }
        interpreter.stack.push(array.pop());
    }
}

