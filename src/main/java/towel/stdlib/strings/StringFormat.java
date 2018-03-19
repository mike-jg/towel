package towel.stdlib.strings;

import towel.*;

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
    public Class[] getPreConditions() {
        return new Class[]{
                String.class
        };
    }

    @Override
    public Class[] getPostConditions() {
        return NO_STACK_CONDITION;
    }

    @Override
    public void call(Interpreter interpreter, Namespace namespace) {

        int replacementIndex = 0;
        String replacementString = String.format("{%d}", replacementIndex);

        if (interpreter.stack.size() == 0) {
            throw new FunctionExecutionError("Format string error: stack is empty.");
        }

        String targetString = interpreter.stack.pop().toString();

        while (targetString.contains(replacementString)) {

            if (interpreter.stack.size() == 0) {
                throw new FunctionExecutionError("Format string error: stack is empty.");
            }

            Object insertionVal = interpreter.stack.pop();
            targetString = targetString.replace(replacementString, insertionVal.toString());

            replacementIndex++;
            replacementString = String.format("{%d}", replacementIndex);
        }

        interpreter.stack.push(targetString);
    }
}
