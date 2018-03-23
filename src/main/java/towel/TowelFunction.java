package towel;

/**
 * Interface for all callables
 *
 * Post/pre-conditions should be returned as such:
 * 0 - top of the stack
 * 1 - 1 position from top of stack
 * 2 - 2 positions from top of stack
 * etc.
 *
 * For example, to assert the stack has any 'Object' on top, followed by an 'Array', the conditions would look at such:
 *
 * new Class[] {
 *     Object.class,
 *     TowelArray.class
 * };
 */
public interface TowelFunction {

    /**
     * For use when a function doesn't want to declare stack pre-conditions or post-conditions
     */
    public final static Class[] NO_STACK_CONDITION = new Class[0];

    /**
     * Pre-conditions are asserted on the stack before the function is executed
     */
    Class[] getPreConditions();

    /**
     * Post-conditions are asserted on the stack after the function has been executed
     */
    Class[] getPostConditions();

    /**
     * Execute the given function
     */
    void call(Interpreter interpreter);
}
