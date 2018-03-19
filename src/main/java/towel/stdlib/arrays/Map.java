package towel.stdlib.arrays;

import towel.*;
import towel.ast.Node;
import towel.ast.Sequence;

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
    public void call(Interpreter interpreter, Namespace namespace) {
        Sequence sequence = interpreter.stack.popSequence();
        TowelArray array = interpreter.stack.popArray();

        // one by one, push each value of the array onto the stack
        // execute the whole sequence against it, and the overwrite
        // the original value in the array

        for (int i = 0; i < array.size(); i++) {
            Object val = array.get(i);
            interpreter.stack.push(val);
            for (Node node : sequence.getNodes()) {
                node.accept(interpreter);
            }
            array.set(i, interpreter.stack.popAsType(val.getClass()));
        }

        interpreter.stack.push(array);
    }
}