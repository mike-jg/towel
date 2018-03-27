package towel.stdlib.maths;

import towel.LibraryMetadata;
import towel.interpreter.Interpreter;
import towel.interpreter.StackCondition;
import towel.interpreter.TowelFunction;

/**
 * Generate a random number
 */
@LibraryMetadata(
        name = "random",
        namespace = "maths"
)
public class Random implements TowelFunction {

    private java.util.Random random = new java.util.Random();

    @Override
    public StackCondition.PreCondition getPreCondition() {
        return StackCondition.preConditionFor(Double.class, Double.class);
    }

    @Override
    public StackCondition.PostCondition getPostCondition() {
        return StackCondition.postConditionFor();
    }

    @Override
    public void call(Interpreter interpreter) {
        double max = interpreter.getStack().popDouble();
        double min = interpreter.getStack().popDouble();
        interpreter.getStack().push(random.nextDouble() * (max - min) + min);
    }
}
