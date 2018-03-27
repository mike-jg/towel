package towel.stdlib.stack;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.StackCondition;
import towel.interpreter.TowelFunction;

/**
 * Duplicate the top two items on the stack
 */
@LibraryMetadata(
        name = "dup2",
        namespace = "stack"
)
public class Duplicate2 implements TowelFunction {

    @Override
    public void call(Interpreter interpreter) {
        Object top = interpreter.getStack().pop();
        Object second = interpreter.getStack().pop();
        interpreter.getStack().push(second);
        interpreter.getStack().push(top);
        interpreter.getStack().push(second);
        interpreter.getStack().push(top);
    }

    @Override
    public StackCondition.PreCondition getPreCondition() {
        return StackCondition.preConditionFor(Object.class, Object.class);
    }
}
