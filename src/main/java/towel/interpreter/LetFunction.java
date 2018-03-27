package towel.interpreter;

/**
 * This is used for {@code let} statements, its simply a function which pushes
 * the value of the {@code let} onto the stack
 */
class LetFunction implements TowelFunction, ExecuteInOriginalContext {

    private final Object value;
    private Namespace context;

    LetFunction(Object value, Namespace context) {
        this.value = value;
        this.context = context;
    }

    @Override
    public void call(Interpreter interpreter) {
        interpreter.getStack().push(value);
    }

    @Override
    public Namespace getOriginalContext() {
        return context;
    }
}
