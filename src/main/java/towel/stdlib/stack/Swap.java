package towel.stdlib.stack;

import towel.Interpreter;
import towel.LibraryMetadata;
import towel.Namespace;
import towel.TowelFunction;

/**
 * Swap the top two values on the stack, so the stack changes as such:
 *
 * A -> B
 * B -> A
 * C -> C
 * D -> D
 */
@LibraryMetadata(
        name = "swap",
        namespace = "stack"
)
public class Swap implements TowelFunction {

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

    @Override
    public void call(Interpreter interpreter, Namespace namespace) {
        Object a = interpreter.stack.pop();
        Object b = interpreter.stack.pop();
        interpreter.stack.push(a);
        interpreter.stack.push(b);
    }
}
