package towel.stdlib.arrays;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.StackCondition;
import towel.interpreter.TowelArray;
import towel.interpreter.TowelFunction;

/**
 * Calculate the length of an array, and leave the length on top of the stack
 */
@LibraryMetadata(
        namespace = "arrays",
        name = "len"
)
public class Length implements TowelFunction {
    @Override
    public StackCondition.PreCondition getPreCondition() {
        return StackCondition.preConditionFor(TowelArray.class);
    }

    @Override
    public void call(Interpreter interpreter) {
        TowelArray array = interpreter.getStack().popArray();
        interpreter.getStack().push(array);
        interpreter.getStack().push((double) array.size());
    }
}
