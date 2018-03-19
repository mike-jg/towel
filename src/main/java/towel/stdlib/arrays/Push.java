package towel.stdlib.arrays;

import towel.*;

/**
 * Push a value from the top of the stack, onto the following array
 */
@LibraryMetadata(
        namespace = "arrays",
        name = "push"
)
public class Push implements TowelFunction {
    @Override
    public Class[] getPreConditions() {
        return new Class[]{
                Object.class,
                TowelArray.class
        };
    }

    @Override
    public Class[] getPostConditions() {
        return NO_STACK_CONDITION;
    }

    @Override
    public void call(Interpreter interpreter, Namespace namespace) {

        Object value = interpreter.stack.pop();
        TowelArray array = interpreter.stack.popArray();

        try {
            doPush(array, value, interpreter);
        } catch (InvalidArrayValueError error) {
            throw new FunctionExecutionError(error.getMessage(), error);
        }
    }

    private void doPush(TowelArray array, Object value, Interpreter interpreter) {
        if (array.isInitialized()) {
            array.push(value);
            interpreter.stack.push(array);
            return;
        }

        array = TowelArray.of(value);
        interpreter.stack.push(array);
    }
}
