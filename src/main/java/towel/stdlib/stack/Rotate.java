package towel.stdlib.stack;

import towel.Interpreter;
import towel.LibraryMetadata;
import towel.Namespace;
import towel.TowelFunction;

/**
 * Rotate the top 3 stack items so the stack changes as such:
 *
 * A -> C
 * B -> A
 * C -> B
 * D -> D
 */
@LibraryMetadata(
        name = "rotate",
        namespace = "stack"
)
public class Rotate implements TowelFunction {
    @Override
    public Class[] getPreConditions() {
        return new Class[]{
                Object.class,
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
        Object c = interpreter.stack.pop();
        interpreter.stack.push(b);
        interpreter.stack.push(a);
        interpreter.stack.push(c);
    }
}
