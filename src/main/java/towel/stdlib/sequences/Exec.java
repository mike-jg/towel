package towel.stdlib.sequences;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
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
    public Class[] getPreConditions() {
        return new Class[]{
                Sequence.class
        };
    }

    @Override
    public Class[] getPostConditions() {
        return NO_STACK_CONDITION;
    }

    @Override
    public void call(Interpreter interpreter) {
        Sequence toExec = interpreter.getStack().popSequence();
        interpreter.interpret(toExec.getNodes());
    }
}

