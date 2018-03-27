package towel.interpreter;
/**
 * Post/pre-conditions should created as such:
 * <pre>
 * 0 - top of the stack
 * 1 - 1 position from top of stack
 * 2 - 2 positions from top of stack
 * etc.
 * </pre>
 *
 * For example, to assert the stack has any 'Object' on top, followed by an 'Array', the conditions would look as such:
 * <pre>
 * StackCondition.preConditionFor(new Class[] {
 *      Object.class,
 *      TowelArray.class
 * });
 * // OR
 * StackCondition.preConditionFor(Object.class, TowelArray.class);
 * </pre>
 *
 * @todo rethink the heirachy in here
 * @todo consider caching an empty postcondition
 */
public abstract class StackCondition {

    public static PreCondition preConditionFor(Class... condition) {
        return new PreCondition(condition);
    }

    public static PostCondition postConditionFor(Class... condition) {
        return new PostCondition(condition);
    }

    private Class[] conditions;
    private boolean isPre;

    private StackCondition(Class[] conditions, boolean isPre) {
        this.conditions = conditions;
        this.isPre = isPre;
    }

    public Class[] condition() {
        return conditions;
    }

    public int length() {
        return conditions.length;
    }

    public boolean isPreCondition() {
        return isPre;
    }

    public boolean isPostCondition() {
        return !isPre;
    }

    public static class PreCondition extends StackCondition {
        private PreCondition(Class[] conditions) {
            super(conditions, true);
        }
    }

    public static class PostCondition extends StackCondition {
        private PostCondition(Class[] conditions) {
            super(conditions, false);
        }
    }
}
