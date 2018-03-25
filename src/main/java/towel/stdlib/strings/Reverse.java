package towel.stdlib.strings;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.TowelFunction;

/**
 * Reverse a string
 */
@LibraryMetadata(
        name = "reverse",
        namespace = "strings"
)
public class Reverse implements TowelFunction {

    @Override
    public void call(Interpreter interpreter) {
        String target = interpreter.getStack().popString();
        char[] newStr = new char[target.length()];

        for (int i = target.length() - 1, c = 0; i >= c; i--, c++) {
            newStr[c] = target.charAt(i);
            newStr[i] = target.charAt(c);
        }

        interpreter.getStack().push(new String(newStr));
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

