package towel.stdlib.strings;

import towel.LibraryMetadata;
import towel.interpreter.FunctionExecutionError;
import towel.interpreter.Interpreter;
import towel.interpreter.StackCondition;
import towel.interpreter.TowelFunction;

/**
 * Format a string
 *
 * Format works like so:
 *
 * "My format string! {0} {1} {3} {2}"
 *
 * Numbers are taken from the top of the stack, one at a time and replace the placeholders
 * in the order {0} -> {1} -> {2} etc
 */
@LibraryMetadata(
        name = "sformat",
        namespace = "strings"
)
public class StringFormat implements TowelFunction {

    @Override
    public StackCondition.PreCondition getPreCondition() {
        return StackCondition.preConditionFor(String.class);
    }

    @Override
    public void call(Interpreter interpreter) {

        int replacementIndex = 0;
        String replacementString = String.format("{%d}", replacementIndex);

        if (interpreter.getStack().size() == 0) {
            throw new FunctionExecutionError("Format string error: stack is empty.");
        }

        String targetString = interpreter.getStack().pop().toString();

        while (targetString.contains(replacementString)) {

            if (interpreter.getStack().size() == 0) {
                throw new FunctionExecutionError("Format string error: stack is empty.");
            }

            Object insertionVal = interpreter.getStack().pop();
            targetString = targetString.replace(replacementString, insertionVal.toString());

            replacementIndex++;
            replacementString = String.format("{%d}", replacementIndex);
        }

        interpreter.getStack().push(targetString);
    }
}
