package towel.interpreter;

/**
 * Interface for all callables
 */
public interface TowelFunction {

    /**
     * Pre-conditions are asserted on the stack before the function is executed
     */
    default StackCondition.PreCondition getPreCondition() {
        return StackCondition.preConditionFor();
    }

    /**
     * Post-conditions are asserted on the stack after the function has been executed
     */
    default StackCondition.PostCondition getPostCondition() {
        return StackCondition.postConditionFor();
    }

    /**
     * Execute the given function
     */
    void call(Interpreter interpreter);
}
