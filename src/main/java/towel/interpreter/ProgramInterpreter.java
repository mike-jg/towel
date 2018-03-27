package towel.interpreter;

import towel.ErrorReporter;
import towel.parser.*;

import java.util.Objects;

/**
 * Default interpreter
 */
class ProgramInterpreter implements Interpreter, NodeVisitor<Void> {

    /**
     * Container for values in the running program
     */
    private final Stack stack = new Stack();

    /**
     * Root AST node
     */
    private final Program program;

    /**
     * The namespace loaders, for loading identifiers into the current namespace
     * <p>
     * These will generally look into a parsed Program node (that was expanded from an import), or a
     * 'virtual' namespace comprised of exposed Java functionality
     * </p>
     */
    private final NamespaceLoaderStack loader = new NamespaceLoaderStack();

    private final ErrorReporter reporter;

    /**
     * Current namespace, containing lookups for identifiers
     */
    private Namespace namespace;

    /**
     * Thrown when an error is encountered, used only internally
     * as it's easier to throw and unwind the stack to get out of an error condition and quit
     */
    public static class InterpreterError extends RuntimeException {

        final Token token;

        private InterpreterError(String message, Token token) {
            super(message);
            this.token = token;
        }

        private InterpreterError(String message, Token token, Throwable previous) {
            super(message, previous);
            this.token = token;
        }
    }

    /**
     * @param program   the root ast node
     * @param loader    default loader to use
     * @param reporter  log errors to this
     * @param namespace initial namespace config
     */
    ProgramInterpreter(Program program, NamespaceLoader loader, ErrorReporter reporter, Namespace namespace) {
        this.program = Objects.requireNonNull(program);
        this.reporter = Objects.requireNonNull(reporter);
        this.loader.push(Objects.requireNonNull(loader));
        this.namespace = namespace;
    }

    @Override
    public Object interpret() {
        try {
            program.accept(this);
        } catch (InterpreterError exception) {
            reporter.error(exception.getMessage(), exception.token.getLine(), exception.token.getCharacter());

            return null;
        } catch (StackAssertionError exception) {
            reporter.error(exception.getMessage(), exception.token.getLine(), exception.token.getCharacter());

            return null;
        }

        if (stack.size() > 0) {
            return stack.peek();
        }
        return null;
    }

    @Override
    public Object interpret(Node[] node) {
        for (Node n : node) {
            n.accept(this);
        }

        if (stack.size() > 0) {
            return stack.peek();
        }
        return null;
    }

    /**
     * Get the current stack
     *
     * @return the stack
     */
    @Override
    public Stack getStack() {
        return stack;
    }

    /**
     * Get the current namespace
     *
     * @return the namespace
     */
    public Namespace getNamespace() {
        return namespace;
    }

    /**
     * Visit the given program node. This could be the root program, or a 'sub program', which will then
     * be loaded into a namespace loader to be queried later on in any import statements
     */
    @Override
    public Void visit(Program programNode) {

        Namespace previousNamespace = null;

        // Not root node, meaning this is not the entry point of the application.
        //
        // Create a new namespace to capture everything in this node,
        // then it can be wrapped in a namespace loader to expose it to import statements.

        if (!programNode.isRootNode()) {
            previousNamespace = namespace;
            namespace = new Namespace();
        }

        for (Node node : programNode.getNodes()) {
            node.accept(this);
        }

        if (previousNamespace != null) {
            if (programNode.isInternal()) {
                loader.push(new InternalFileNamespaceLoader(programNode.getNamespace(), namespace.exportPublicMembers()));
            } else {
                loader.push(new UserFileNamespaceLoader(programNode.getNamespace(), namespace.exportPublicMembers()));
            }
            namespace = previousNamespace;
        }

        return null;
    }

    @Override
    public Void visit(Literal literalNode) {
        stack.push(literalNode.getToken().getLiteral());

        return null;
    }

    @Override
    public Void visit(BinaryOperator binaryOperatorNode) {

        // binary operators are all mathematical, so the
        // stack must have two numbers at the top

        stack.assertState(
                StackCondition.preConditionFor(Double.class, Double.class),
                binaryOperatorNode.getToken()
        );

        double right = stack.popDouble();
        double left = stack.popDouble();

        switch (binaryOperatorNode.getTokenType()) {
            case PLUS:
                stack.push(left + right);
                return null;

            case MINUS:
                stack.push(left - right);
                return null;

            case STAR:
                stack.push(left * right);
                return null;

            case MOD:
                if (right == 0) {
                    throw new InterpreterError("Division by zero.", binaryOperatorNode.getToken());
                }
                stack.push(left % right);
                return null;

            case SLASH:
                if (right == 0) {
                    throw new InterpreterError("Division by zero.", binaryOperatorNode.getToken());
                }
                stack.push(left / right);
                return null;
        }

        throw new InterpreterError("Invalid binary operator: " + binaryOperatorNode.getTokenType(), binaryOperatorNode.getToken());
    }

    @Override
    public Void visit(Comparison comparisonNode) {

        stack.assertStack(2, "Two arguments are required for comparisons.", comparisonNode.getToken());

        // comparisons can happen on most types, so it's partitioned off
        // into separate methods for each compatible set of types

        Object right = stack.pop();
        Object left = stack.pop();

        if (areAllOfType(Double.class, right, left)) {
            doNumberComparison(comparisonNode, (double) left, (double) right);
            return null;
        }

        if (areAllOfType(String.class, right, left)) {
            doStringComparison(comparisonNode, left.toString(), right.toString());
            return null;
        }

        if (areAllOfType(Boolean.class, left, right)) {
            doBooleanComparison(comparisonNode, (boolean) left, (boolean) right);
            return null;
        }

        String message = String.format("This comparison cannot be done between '%s' and '%s'.",
                TypeNameTranslator.get(left.getClass()),
                TypeNameTranslator.get(right.getClass())
        );

        throw new InterpreterError(message, comparisonNode.getToken());
    }

    /**
     * Compare two numbers, leave result on the top of the stack
     */
    private void doNumberComparison(Comparison node, double left, double right) {
        boolean wasTruthy;

        switch (node.getTokenType()) {
            case LESS_THAN:
                wasTruthy = left < right;
                break;

            case LESS_THAN_EQUAL:
                wasTruthy = left <= right;
                break;

            case EQUAL_EQUAL:
                wasTruthy = left == right;
                break;

            case GREATER_THAN:
                wasTruthy = left > right;
                break;

            case GREATER_THAN_EQUAL:
                wasTruthy = left >= right;
                break;

            case NOT_EQUAL:
                wasTruthy = left != right;
                break;

            default:
                throw new InterpreterError("Invalid comparison: " + node, node.getToken());
        }

        stack.push(wasTruthy);
    }

    /**
     * Compare two strings, leave result on the top of the stack
     */
    private void doStringComparison(Comparison node, String left, String right) {
        if (node.getTokenType() == Token.TokenType.EQUAL_EQUAL) {
            stack.push(left.equals(right));
            return;
        }
        if (node.getTokenType() == Token.TokenType.NOT_EQUAL) {
            stack.push(!left.equals(right));
            return;
        }
        throw new InterpreterError("Strings can only be checked with == and !=.", node.getToken());
    }

    /**
     * Compare two booleans, leave result on the top of the stack
     */
    private void doBooleanComparison(Comparison node, boolean left, boolean right) {
        if (node.getTokenType() == Token.TokenType.EQUAL_EQUAL) {
            stack.push(left == right);
            return;
        }
        if (node.getTokenType() == Token.TokenType.NOT_EQUAL) {
            stack.push(left != right);
            return;
        }
        if (node.getTokenType() == Token.TokenType.OR) {
            stack.push(left || right);
            return;
        }
        if (node.getTokenType() == Token.TokenType.AND) {
            stack.push(left && right);
            return;
        }
        throw new InterpreterError("Booleans can only be checked with '==', '!=', '||' and '&&'.", node.getToken());
    }

    @Override
    public Void visit(Condition conditionNode) {

        if (conditionNode.getTokenType() == Token.TokenType.QUESTION_MARK) {
            branchedCondition(conditionNode);
        } else if (conditionNode.getTokenType() == Token.TokenType.DOUBLE_QUESTION_MARK) {
            singleBranchCondition(conditionNode);
        }
        return null;
    }

    /**
     * This condition has two branches, e.g. a 'then' and an 'else'
     */
    private void branchedCondition(Condition node) {
        stack.assertState(
                StackCondition.preConditionFor(
                        Sequence.class,
                        Sequence.class,
                        Boolean.class),
                node.getToken()
        );

        Sequence elseBranch = stack.popSequence();
        Sequence thenBranch = stack.popSequence();
        boolean condition = stack.popBoolean();

        Sequence chosen;
        chosen = condition ? thenBranch : elseBranch;

        for (Node e : chosen.getNodes()) {
            e.accept(this);
        }
    }

    /**
     * This condition has one branch, so just a 'then', if the condition fails then nothing happens
     */
    private void singleBranchCondition(Condition node) {
        stack.assertState(
                StackCondition.preConditionFor(
                        Sequence.class,
                        Boolean.class),
                node.getToken()
        );

        Sequence thenBranch = stack.popSequence();
        boolean condition = stack.popBoolean();

        if (condition) {
            for (Node e : thenBranch.getNodes()) {
                e.accept(this);
            }
        }
    }

    @Override
    public Void visit(Sequence sequenceNode) {
        stack.push(sequenceNode);
        return null;
    }

    @Override
    public Void visit(Function functionNode) {

        if (namespace.isDefined(functionNode.getToken().getLexeme())) {
            throw new InterpreterError("A function definition with the token '" + functionNode.getToken().getLexeme() + "' already exists.", functionNode.getToken());
        }

        UserDefinedFunction function = new UserDefinedFunction(
                functionNode.getToken(),
                functionNode.getBody(),
                StackCondition.preConditionFor(functionNode.getPreConditions()),
                StackCondition.postConditionFor(functionNode.getPostConditions()),
                namespace
        );

        if (functionNode.isPublic()) {
            namespace.definePublicMember(functionNode.getLexeme(), function);
        } else {
            namespace.definePrivateMember(functionNode.getLexeme(), function);
        }
        return null;
    }

    @Override
    public Void visit(Identifier identifierNode) {

        // identifier visit is a function call, as even 'let' variables are
        // parsed into a function (one that just pushes its value onto the stack)

        Namespace previousNamespace = null;

        try {
            TowelFunction function = getFunction(identifierNode);
            stack.assertState(function.getPreCondition(), identifierNode.getToken());

            if (function instanceof ExecuteInOriginalContext) {
                previousNamespace = namespace;
                namespace = ((ExecuteInOriginalContext) function).getOriginalContext();
            }

            function.call(this);

            stack.assertState(function.getPostCondition(), identifierNode.getToken());
        } catch (FunctionExecutionError e) {

            Token errorIdentifier = identifierNode.getToken();
            if (identifierNode.isNamespaced()) {
                errorIdentifier = identifierNode.getNamespaceToken();
            }

            throw new InterpreterError(e.getMessage(), errorIdentifier, e);
        } finally {
            if (previousNamespace != null) {
                namespace = previousNamespace;
            }
        }

        return null;
    }

    /**
     * Get the function identified by 'identifierNode'
     */
    private TowelFunction getFunction(Identifier identifierNode) {
        Namespace targetNamespace = namespace;

        if (identifierNode.isNamespaced()) {
            targetNamespace = namespace.getNamespace(identifierNode.getNamespace());
        }

        if (!targetNamespace.isDefined(identifierNode.getName())) {
            throw new InterpreterError(String.format("Unknown identifier '%s'.", identifierNode.getName()), identifierNode.getToken());
        }

        Object target = targetNamespace.get(identifierNode.getName());

        if (!(target instanceof TowelFunction)) {
            // This should really be impossible
            throw new IllegalStateException("Not a valid function.");
        }

        return (TowelFunction) target;
    }

    @Override
    public Void visit(Import importNode) {

        ImportNodeResolver adapter = ImportNodeResolver.wrap(importNode);

        String[] funcNames = loader.getPublicNamesInNamespace(adapter.getNamespace());

        for (String functionName : funcNames) {

            // import <io>
            // so take everything in <io> and add it to the namespace
            // with 'io.' prefix, e.g. io.print
            if (adapter.isImportingWholeNamespace()) {
                TowelFunction func = loader.getFunction(adapter.getNamespace(), functionName);

                if (!namespace.isDefined(adapter.getNormalized())) {
                    namespace.definePrivateMember(adapter.getNormalized(), new Namespace());
                }

                namespace.getNamespace(adapter.getNormalized()).definePrivateMember(functionName, func);
            } else {
                // there's a pattern such as
                // import print from <io>
                // import print, println from <io>
                // import * from <io>
                if (!matchesImportPattern(adapter, functionName)) {
                    continue;
                }

                TowelFunction func = loader.getFunction(adapter.getNamespace(), functionName);

                // check for an alias
                // import print from <io> as my_print
                String alias = adapter.isAliased()
                        ? adapter.getAlias() : functionName;

                namespace.definePrivateMember(alias, func);
            }
        }

        return null;
    }

    @Override
    public Void visit(Let letNode) {

        // a let is converted into a function which pushes
        // the value of the let onto the stack when called

        stack.assertState(StackCondition.preConditionFor(new Class[]{
                Object.class
        }), letNode.getToken());

        final Object value = stack.pop();

        if (letNode.isPublic()) {
            namespace.definePublicMember(letNode.getName(), new LetFunction(value, namespace));
        } else {
            namespace.definePrivateMember(letNode.getName(), new LetFunction(value, namespace));
        }

        return null;
    }

    @Override
    public Void visit(Array arrayNode) {

        Object[] contents = arrayNode.getContents();
        try {
            TowelArray array = TowelArray.of(contents);
            stack.push(array);
        } catch (InvalidArrayValueError error) {
            throw new InterpreterError(error.getMessage(), arrayNode.getToken(), error);
        }

        return null;
    }

    /**
     * Does the given function match the import pattern?
     *
     * Pattern is either a * or a function token like print, etc
     */
    private boolean matchesImportPattern(ImportNodeResolver adapter, String func) {
        if (adapter.isStarImport()) {
            return true;
        }

        for (String pattern : adapter.getTarget()) {
            if (pattern.equals(func)) {
                return true;
            }
        }

        return false;
    }

    private boolean areAllOfType(Class expectedType, Object... objects) {
        for (Object object : objects) {
            if (!expectedType.isInstance(object)) {
                return false;
            }
        }
        return true;
    }
}
