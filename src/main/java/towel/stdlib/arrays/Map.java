package towel.stdlib.arrays;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.TowelArray;
import towel.interpreter.TowelFunction;
import towel.parser.Sequence;

/**
 * Map a sequence against an array, modifying it in-place
 */
@LibraryMetadata(
        namespace = "arrays",
        name = "map"
)
public class Map implements TowelFunction {
    @Override
    public Class[] getPreConditions() {
        return new Class[]{
                Sequence.class,
                TowelArray.class
        };
    }

    @Override
    public Class[] getPostConditions() {
        return NO_STACK_CONDITION;
    }

    @Override
    public void call(Interpreter interpreter) {
        Sequence sequence = interpreter.getStack().popSequence();
        TowelArray array = interpreter.getStack().popArray();

        // one by one, push each value of the array onto the stack
        // execute the whole sequence against it, and the overwrite
        // the original value in the array

        for (int i = 0; i < array.size(); i++) {
            Object val = array.get(i);
            interpreter.getStack().push(val);
            interpreter.interpret(sequence.getNodes());
            array.set(i, interpreter.getStack().pop());
        }

        interpreter.getStack().push(array);
    }
}
