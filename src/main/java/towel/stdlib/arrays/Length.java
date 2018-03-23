package towel.stdlib.arrays;

import towel.*;

/**
 * Calculate the length of an array, and leave the length on top of the stack
 */
@LibraryMetadata(
        namespace = "arrays",
        name = "len"
)
public class Length implements TowelFunction {
    @Override
    public Class[] getPreConditions() {
        return new Class[]{
                TowelArray.class
        };
    }

    @Override
    public Class[] getPostConditions() {
        return NO_STACK_CONDITION;
    }

    @Override
    public void call(Interpreter interpreter) {
        TowelArray array = interpreter.getStack().popArray();
        interpreter.getStack().push(array);
        interpreter.getStack().push((double) array.size());
    }
}
