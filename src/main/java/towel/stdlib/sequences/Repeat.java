package towel.stdlib.sequences;

import towel.Interpreter;
import towel.LibraryMetadata;
import towel.Namespace;
import towel.TowelFunction;
import towel.ast.Node;
import towel.ast.Sequence;

/**
 * Repeat will repeatedly execute a sequence for a given number of iterations
 */
@LibraryMetadata(
        name = "repeat",
        namespace = "sequences"
)
public class Repeat implements TowelFunction {

    @Override
    public void call(Interpreter interpreter, Namespace namespace) {
        Sequence sequence = interpreter.stack.popSequence();
        double times = interpreter.stack.popDouble();

        while (times-- > 0) {
            for (Node e : sequence.getNodes()) {
                e.accept(interpreter);
            }
        }
    }

    @Override
    public Class[] getPreConditions() {
        return new Class[]{
                Sequence.class,
                Double.class,
        };
    }

    @Override
    public Class[] getPostConditions() {
        return NO_STACK_CONDITION;
    }
}
