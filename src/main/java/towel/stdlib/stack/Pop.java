package towel.stdlib.stack;

import towel.Interpreter;
import towel.LibraryMetadata;
import towel.Namespace;
import towel.TowelFunction;

/**
 * Pop the stack and discard the value
 */
@LibraryMetadata(
        name = "pop",
        namespace = "stack"
)
public class Pop implements TowelFunction {

    @Override
    public void call(Interpreter interpreter, Namespace namespace) {
        interpreter.stack.pop();
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
