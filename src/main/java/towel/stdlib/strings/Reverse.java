package towel.stdlib.strings;

import towel.Interpreter;
import towel.LibraryMetadata;
import towel.Namespace;
import towel.TowelFunction;

/**
 * Reverse a string
 */
@LibraryMetadata(
        name = "reverse",
        namespace = "strings"
)
public class Reverse implements TowelFunction {

    @Override
    public void call(Interpreter interpreter, Namespace namespace) {
        String target = interpreter.stack.popString();
        char[] newStr = new char[target.length()];

        for (int i = target.length() - 1, c = 0; i >= c; i--, c++) {
            newStr[c] = target.charAt(i);
            newStr[i] = target.charAt(c);
        }

        interpreter.stack.push(new String(newStr));
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

