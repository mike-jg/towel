package towel.stdlib.strings;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.StackCondition;
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
    public StackCondition.PreCondition getPreCondition() {
        return StackCondition.preConditionFor(String.class);
    }
}
