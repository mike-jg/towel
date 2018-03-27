package towel.stdlib.stack;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.StackCondition;
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
    public StackCondition.PreCondition getPreCondition() {
        return StackCondition.preConditionFor(Object.class);
    }
}
