package towel.stdlib.stack;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.StackCondition;
import towel.interpreter.TowelFunction;

/**
 * Duplicate the top item of the stack
 */
@LibraryMetadata(
        name = "dup",
        namespace = "stack"
)
public class Duplicate implements TowelFunction {

    @Override
    public void call(Interpreter interpreter) {
        Object top = interpreter.getStack().pop();
        interpreter.getStack().push(top);
        interpreter.getStack().push(top);
    }

    @Override
    public StackCondition.PreCondition getPreCondition() {
        return StackCondition.preConditionFor(Object.class);
    }
}
