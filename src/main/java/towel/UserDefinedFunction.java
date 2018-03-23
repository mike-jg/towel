package towel;

import towel.ast.Node;

/**
 * Represents a user-land function definition
 *
 * e.g. the def keyword
 */
public class UserDefinedFunction implements TowelFunction, ExecuteInOriginalContext {

    private final Token name;
    private final Node[] body;
    private final Class[] preConditions;
    private final Class[] postConditions;

    /**
     * The context that this was defined in
     *
     * For functions included from other files they need
     * this so they have access to the imports/definitions in their declaring file
     */
    private final Namespace context;

    public UserDefinedFunction(Token name, Node[] body, Class[] preConditions, Class[] postConditions, Namespace originalContext) {
        this.name = name;
        this.body = body;
        this.preConditions = preConditions;
        this.postConditions = postConditions;
        context = originalContext;
    }

    @Override
    public void call(Interpreter interpreter) {
        for (Node e : body) {
            e.accept(interpreter);
        }
    }

    @Override
    public Class[] getPreConditions() {
        return preConditions;
    }

    @Override
    public Class[] getPostConditions() {
        return postConditions;
    }

    @Override
    public Namespace getOriginalContext() {
        return context;
    }
}
