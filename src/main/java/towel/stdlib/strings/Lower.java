package towel.stdlib.strings;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.TowelFunction;

/**
 * Convert a string to lower case
 */
@LibraryMetadata(
        name = "lower",
        namespace = "strings"
)
public class Lower implements TowelFunction {

    @Override
    public void call(Interpreter interpreter) {
        String target = interpreter.getStack().popString();
        interpreter.getStack().push(target.toLowerCase());
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
