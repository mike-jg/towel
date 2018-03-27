package towel.interpreter;

import towel.parser.Node;
import towel.parser.Token;

/**
 * Represents a user-land function definition
 *
 * e.g. the def keyword
 */
class UserDefinedFunction implements TowelFunction, ExecuteInOriginalContext {

    private final Token name;
    private final Node[] body;
    private final StackCondition.PreCondition preConditions;
    private final StackCondition.PostCondition postConditions;

    /**
     * The context that this was defined in
     * <p>
     * For functions included from other files they need
     * this so they have access to the imports/definitions in their declaring file
     */
    private final Namespace context;

    UserDefinedFunction(Token name, Node[] body, StackCondition.PreCondition preConditions, StackCondition.PostCondition postConditions, Namespace originalContext) {
        this.name = name;
        this.body = body;
        this.preConditions = preConditions;
        this.postConditions = postConditions;
        context = originalContext;
    }

    @Override
    public void call(Interpreter interpreter) {
        interpreter.interpret(body);
    }

    @Override
    public StackCondition.PreCondition getPreCondition() {
        return preConditions;
    }

    @Override
    public StackCondition.PostCondition getPostCondition() {
        return postConditions;
    }

    @Override
    public Namespace getOriginalContext() {
        return context;
    }
}
