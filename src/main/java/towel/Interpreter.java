package towel;

import towel.ast.*;

import java.util.Objects;

/**
 * Run through the AST and execute its instructions
 */
public class Interpreter implements NodeVisitor<Void> {

    public final Stack stack = new Stack();
    public final Program program;
    private Namespace namespace;
    private final NamespaceLoaderStack loader = new NamespaceLoaderStack();
    private final ErrorReporter reporter;

    static class InterpreterError extends RuntimeException {

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

    Interpreter(Program program, NamespaceLoader loader, ErrorReporter reporter, Namespace namespace) {
        this.program = Objects.requireNonNull(program);
        this.reporter = Objects.requireNonNull(reporter);
        this.loader.push(Objects.requireNonNull(loader));
        this.namespace = namespace;
    }

    Interpreter(Program program, NamespaceLoader loader, ErrorReporter reporter) {
        this(program, loader, reporter, new Namespace());
    }

    /**
     * Execute the program
     */
    Object interpret() {
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

    public Namespace getNamespace() {
        return namespace;
    }

    @Override
    public Void visit(Program programNode) {

        Namespace oldns = null;

        if (!programNode.isTopLevel()) {
            oldns = namespace;
            namespace = new Namespace();
        }

        for (Node node : programNode.getNodes()) {
            node.accept(this);
        }

        if (oldns != null) {
            oldns.define(programNode.getNamespace(), namespace.exportPublicMembers());
            loader.push(new ImportFileNamespaceLoader(programNode.getNamespace(), oldns.exportPublicMembers()));
            namespace = oldns;
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

        stack.assertState(new Class[]{
                Double.class,
                Double.class
        }, Stack.Conditions.PRE, binaryOperatorNode.getToken());

        Object right = stack.popDouble();
        Object left = stack.popDouble();

        switch (binaryOperatorNode.getTokenType()) {
            case PLUS:
                stack.push((double) left + (double) right);
                return null;

            case MINUS:
                stack.push((double) left - (double) right);
                return null;

            case STAR:
                stack.push((double) left * (double) right);
                return null;

            case MOD:
                if ((double) right == 0) {
                    throw new InterpreterError("Division by zero.", binaryOperatorNode.getToken());
                }
                stack.push((double) left % (double) right);
                return null;

            case SLASH:
                if ((double) right == 0) {
                    throw new InterpreterError("Division by zero.", binaryOperatorNode.getToken());
                }
                stack.push((double) left / (double) right);
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
        stack.assertState(new Class[]{
                Sequence.class,
                Sequence.class,
                Boolean.class
        }, Stack.Conditions.PRE, node.getToken());

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
        stack.assertState(new Class[]{
                Sequence.class,
                Boolean.class
        }, Stack.Conditions.PRE, node.getToken());

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

        namespace.define(functionNode.getLexeme(),
                new UserDefinedFunction(
                        functionNode.getToken(),
                        functionNode.getBody(),
                        functionNode.getPreConditions(),
                        functionNode.getPostConditions(),
                        namespace
                )
        );
        return null;
    }

    @Override
    public Void visit(Identifier identifierNode) {

        // identifier visit is a function call, as even 'let' variables are
        // parsed into a function (one that just pushes its value onto the stack)

        Namespace targetNamespace = namespace;

        if (identifierNode.isNamespaced()) {
            targetNamespace = namespace.getNamespace(identifierNode.getNamespace());
        }

        if (!targetNamespace.isDefined(identifierNode.getName())) {
            throw new InterpreterError(String.format("Unknown identifier '%s'.", identifierNode.getName()), identifierNode.getToken());
        }
        Object target = targetNamespace.get(identifierNode.getName());

        if (!(target instanceof TowelFunction)) {
            throw new IllegalArgumentException("Not a valid function.");
        }

        try {
            TowelFunction t = ((TowelFunction) target);
            stack.assertState(t.getPreConditions(), Stack.Conditions.PRE, identifierNode.getToken());

            Namespace oldns = null;
            if (t instanceof ExecuteInOriginalContext) {
                oldns = namespace;
                namespace = ((ExecuteInOriginalContext) t).getOriginalContext();
            }

            t.call(this, namespace);

            if (oldns != null) {
                namespace = oldns;
            }

            stack.assertState(t.getPostConditions(), Stack.Conditions.POST, identifierNode.getToken());
        } catch (FunctionExecutionError e) {

            Token errorIdentifier = identifierNode.getToken();
            if (identifierNode.isNamespaced()) {
                errorIdentifier = identifierNode.getNamespaceToken();
            }

            throw new InterpreterError(e.getMessage(), errorIdentifier, e);
        }

        return null;
    }

    @Override
    public Void visit(Import importNode) {

        String namespace = importNode.getNamespace();
        String[] funcNames = loader.getNamesInLibrary(namespace);

        for (String functionName : funcNames) {

            // import <io>
            // so take everything in <io> and add it to the namespace
            // with 'io.' prefix, e.g. io.print
            if (importNode.isImportingWholeNamespace()) {
                TowelFunction func = loader.getFunction(importNode.getNamespace(), functionName);

                if (!this.namespace.isDefined(importNode.getNamespace())) {
                    this.namespace.define(importNode.getNamespace(), new Namespace());
                }

                this.namespace.getNamespace(importNode.getNamespace()).define(functionName, func);
            } else {
                // there's a pattern such as
                // import print from <io>
                // import print, println from <io>
                // import * from <io>
                if (!matchesImportPattern(importNode, functionName)) {
                    continue;
                }

                TowelFunction func = loader.getFunction(importNode.getNamespace(), functionName);

                // check for an alias
                // import print from <io> as my_print
                String alias = importNode.isAliased()
                        ? importNode.getAlias() : functionName;

                this.namespace.define(alias, func);
            }
        }

        return null;
    }

    @Override
    public Void visit(FileImport fileImportNode) {

        // By the time the interpreter has access to the AST, file import nodes should have been
        // expanded into their own Program node, with the parsed contents of the included file.

        throw new UnsupportedOperationException();
    }

    @Override
    public Void visit(Let letNode) {

        // a let is converted into a function which pushes
        // the value of the let onto the stack when called

        stack.assertState(new Class[]{
                Object.class
        }, Stack.Conditions.PRE, letNode.getToken());

        final Object value = stack.pop();

        namespace.define(letNode.getName(), new LetFunction(value, namespace));

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
    private boolean matchesImportPattern(Import _import, String func) {
        if (_import.isStarImport()) {
            return true;
        }

        for (String pattern : _import.getTarget()) {
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
