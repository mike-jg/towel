package towel.pass;

import towel.ErrorReporter;
import towel.parser.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Performs a static analysis pass over the AST to spot problems
 *
 * 'Resolves' names by renaming them according to scope so that closures work correctly
 *
 * Stops a name being used twice <- temporarily removed until imports are tidied up
 * Stops invalid imports, e.g. non-existent names  <- temporarily removed until imports are tidied up
 * Raises a notice for unused names <- temporarily removed until imports are tidied up
 *
 * @todo forbid imports except at top of file
 */
class Resolver implements NodeVisitor<Void> {

    private static final String IDENTIFIER_RENAME_SCHEME = "%s$%s";

    private final ErrorReporter reporter;

    /**
     * All of the identifiers what have been renamed in the current scope
     */
    private final Map<String, String> identifiersRenamedInScope = new HashMap<>();

    /**
     * Currently in a function?
     */
    private boolean isInFunction = false;

    private String currentFunctionName = null;

    Resolver(ErrorReporter reporter) {
        this.reporter = Objects.requireNonNull(reporter);
    }

    private void rename(Renameable identifier, String scope) {
        identifier.setName(String.format(IDENTIFIER_RENAME_SCHEME, scope, identifier.getOriginalName()));
    }

    @Override
    public Void visit(Program programNode) {
        for (Node node : programNode.getNodes()) {
            node.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Array arrayNode) {
        return null;
    }

    @Override
    public Void visit(Literal literalNode) {
        return null;
    }

    @Override
    public Void visit(BinaryOperator binaryOperatorNode) {
        return null;
    }

    @Override
    public Void visit(Condition conditionNode) {
        return null;
    }

    @Override
    public Void visit(Comparison comparisonNode) {
        return null;
    }

    @Override
    public Void visit(Sequence sequenceNode) {
        for (Node e : sequenceNode.getNodes()) {
            e.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(Function functionNode) {
        if (isInFunction) {
            logErrorAtToken("Cannot declare a function within another function.", functionNode.getToken());
        }

        isInFunction = true;
        currentFunctionName = functionNode.getLexeme();
        for (Node e : functionNode.getBody()) {
            e.accept(this);
        }
        identifiersRenamedInScope.clear();
        currentFunctionName = null;
        isInFunction = false;

        return null;
    }

    @Override
    public Void visit(Identifier identifierNode) {

        // If the target of this identifier has been renamed in the current scope, then rename this to point back to it
        // e.g. in cases such as the below:
        //
        // "wrong" let somevar
        //
        // def sometest {
        //    "correct" let somevar
        //    { somevar }
        // }
        //
        // sometest exec print
        //
        // The inner 'let' will have been renamed so that the sequence then refers to the correct variable
        // when it is executed in a different scope

        if (isInFunction && identifiersRenamedInScope.containsKey(identifierNode.getName())) {
            rename(identifierNode, currentFunctionName);
        }

        String name = identifierNode.getName();
        if (identifierNode.isNamespaced()) {
            name = identifierNode.getNamespace() + "." + name;
        }

        return null;
    }

    @Override
    public Void visit(Import importNode) {
        return null;
    }

    @Override
    public Void visit(Let letNode) {

        // Inside function scope, 'let' program are renamed
        // so that they have a unique name based on the function
        // they were defined in, in case the identifier they create
        // is referred to by something which has changed scope

        if (isInFunction) {
            String original = letNode.getLexeme();
            rename(letNode, currentFunctionName);
            identifiersRenamedInScope.put(original, letNode.getName());
        }
        return null;
    }

    private void logErrorAtToken(String message, Token token) {
        reporter.error(message, token.getLine(), token.getCharacter());
    }
}
