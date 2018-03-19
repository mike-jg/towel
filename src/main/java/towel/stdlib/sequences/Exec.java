package towel.stdlib.sequences;

import towel.Interpreter;
import towel.LibraryMetadata;
import towel.Namespace;
import towel.TowelFunction;
import towel.ast.Node;
import towel.ast.Sequence;

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
    public void call(Interpreter interpreter, Namespace namespace) {
        Sequence toExec = interpreter.stack.popSequence();

        for (Node node : toExec.getNodes()) {
            node.accept(interpreter);
        }
    }
}

