package towel.stdlib.stack;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.TowelFunction;

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
    public void call(Interpreter interpreter) {
        Object a = interpreter.getStack().pop();
        Object b = interpreter.getStack().pop();
        interpreter.getStack().push(a);
        interpreter.getStack().push(b);
    }
}
