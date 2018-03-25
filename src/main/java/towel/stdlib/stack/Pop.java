package towel.stdlib.stack;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.TowelFunction;

/**
 * Pop the stack and discard the value
 */
@LibraryMetadata(
        name = "pop",
        namespace = "stack"
)
public class Pop implements TowelFunction {

    @Override
    public void call(Interpreter interpreter) {
        interpreter.getStack().pop();
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
