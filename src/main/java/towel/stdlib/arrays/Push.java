package towel.stdlib.arrays;

import towel.LibraryMetadata;
import towel.interpreter.*;

/**
 * Push a value from the top of the stack, onto the following array
 */
@LibraryMetadata(
        namespace = "arrays",
        name = "push"
)
public class Push implements TowelFunction {
    @Override
    public StackCondition.PreCondition getPreCondition() {
        return StackCondition.preConditionFor(Object.class, TowelArray.class);
    }

    @Override
    public void call(Interpreter interpreter) {

        Object value = interpreter.getStack().pop();
        TowelArray array = interpreter.getStack().popArray();

        try {
            doPush(array, value, interpreter);
        } catch (InvalidArrayValueError error) {
            throw new FunctionExecutionError(error.getMessage(), error);
        }
    }

    private void doPush(TowelArray array, Object value, Interpreter interpreter) {
        if (array.isInitialized()) {
            array.push(value);
            interpreter.getStack().push(array);
            return;
        }

        array = TowelArray.of(value);
        interpreter.getStack().push(array);
    }
}

