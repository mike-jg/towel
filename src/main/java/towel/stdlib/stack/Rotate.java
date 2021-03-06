package towel.stdlib.stack;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.StackCondition;
import towel.interpreter.TowelFunction;

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
    public StackCondition.PreCondition getPreCondition() {
        return StackCondition.preConditionFor(Object.class, Object.class, Object.class);
    }

    @Override
    public void call(Interpreter interpreter) {
        Object a = interpreter.getStack().pop();
        Object b = interpreter.getStack().pop();
        Object c = interpreter.getStack().pop();
        interpreter.getStack().push(b);
        interpreter.getStack().push(a);
        interpreter.getStack().push(c);
    }
}
