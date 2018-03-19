package towel.stdlib.strings;

import towel.Interpreter;
import towel.LibraryMetadata;
import towel.Namespace;
import towel.TowelFunction;

/**
 * Convert a string to lower case
 */
@LibraryMetadata(
        name = "lower",
        namespace = "strings"
)
public class Lower implements TowelFunction {

    @Override
    public void call(Interpreter interpreter, Namespace namespace) {
        String target = interpreter.stack.popString();
        interpreter.stack.push(target.toLowerCase());
    }

    @Override
    public Class[] getPreConditions() {
        return new Class[]{
                String.class
        };
    }

    @Override
    public Class[] getPostConditions() {
        return NO_STACK_CONDITION;
    }
}
