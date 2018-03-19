package towel;

/**
 * This is used for 'let' statements, its simply a function which pushes
 * the value of the 'let' onto the stack
 */
public class LetFunction implements TowelFunction, ExecuteInOriginalContext {

    private final Object value;
    private Namespace context;

    public LetFunction(Object value, Namespace context) {
        this.value = value;
        this.context = context;
    }

    @Override
    public Class[] getPreConditions() {
        return NO_STACK_CONDITION;
    }

    @Override
    public Class[] getPostConditions() {
        return NO_STACK_CONDITION;
    }

    @Override
    public void call(Interpreter interpreter, Namespace namespace) {
        interpreter.stack.push(value);
    }

    @Override
    public Namespace getOriginalContext() {
        return context;
    }
}
