package towel.stdlib.stack;

import towel.Interpreter;
import towel.LibraryMetadata;
import towel.Namespace;
import towel.TowelFunction;

/**
 * Duplicate the top item of the stack
 */
@LibraryMetadata(
        name = "dup",
        namespace = "stack"
)
public class Duplicate implements TowelFunction {

    @Override
    public void call(Interpreter interpreter, Namespace namespace) {
        Object top = interpreter.stack.pop();
        interpreter.stack.push(top);
        interpreter.stack.push(top);
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
