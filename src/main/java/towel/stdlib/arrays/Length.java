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
    public void call(Interpreter interpreter, Namespace namespace) {
        TowelArray array = interpreter.stack.popArray();
        interpreter.stack.push(array);
        interpreter.stack.push((double) array.size());
    }
}
