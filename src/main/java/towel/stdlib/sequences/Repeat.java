package towel.stdlib.sequences;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.StackCondition;
import towel.interpreter.TowelFunction;
import towel.parser.Sequence;

/**
 * Repeat will repeatedly execute a sequence for a given number of iterations
 */
@LibraryMetadata(
        name = "repeat",
        namespace = "sequences"
)
public class Repeat implements TowelFunction {

    @Override
    public void call(Interpreter interpreter) {
        Sequence sequence = interpreter.getStack().popSequence();
        double times = interpreter.getStack().popDouble();

        while (times-- > 0) {
            interpreter.interpret(sequence.getNodes());
        }
    }

    @Override
    public StackCondition.PreCondition getPreCondition() {
        return StackCondition.preConditionFor(Sequence.class, Double.class);
    }
}
