package towel.stdlib.sequences;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.StackCondition;
import towel.interpreter.TowelFunction;
import towel.parser.Sequence;

/**
 * Exec executes a sequence, interpreting each of its program in turn
 */
@LibraryMetadata(
        name = "exec",
        namespace = "sequences"
)
public class Exec implements TowelFunction {
    @Override
    public StackCondition.PreCondition getPreCondition() {
        return StackCondition.preConditionFor(Sequence.class);
    }

    @Override
    public void call(Interpreter interpreter) {
        Sequence toExec = interpreter.getStack().popSequence();
        interpreter.interpret(toExec.getNodes());
    }
}

