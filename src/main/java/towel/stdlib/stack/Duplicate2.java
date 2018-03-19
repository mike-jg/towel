package towel.stdlib.stack;

import towel.Interpreter;
import towel.LibraryMetadata;
import towel.Namespace;
import towel.TowelFunction;

/**
 * Duplicate the top two items on the stack
 */
@LibraryMetadata(
        name = "dup2",
        namespace = "stack"
)
public class Duplicate2 implements TowelFunction {

    @Override
    public void call(Interpreter interpreter, Namespace namespace) {
        Object top = interpreter.stack.pop();
        Object second = interpreter.stack.pop();
        interpreter.stack.push(second);
        interpreter.stack.push(top);
        interpreter.stack.push(second);
        interpreter.stack.push(top);
    }

    @Override
    public Class[] getPreConditions() {
        return new Class[]{
                Object.class,
                Object.class
        };
    }

    @Override
    public Class[] getPostConditions() {
        return NO_STACK_CONDITION;
    }
}
