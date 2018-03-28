package towel.pass;

import towel.ErrorReporter;
import towel.ast.*;

import java.util.Objects;

/**
 * Forbids imports in invalid places, i.e. anywhere except at the top of a file
 *
 * @todo add more checks, such as duplicate imports, unused imports
 */
public class ImportResolver implements NodeVisitor<Void> {

    private final ErrorReporter reporter;
    private boolean importsLegal = true;

    ImportResolver(ErrorReporter reporter) {
        this.reporter = Objects.requireNonNull(reporter);
    }

    @Override
    public Void visit(Program programNode) {
        for (Node node : programNode.getNodes()) {
            node.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Literal literalNode) {
        importsLegal = false;
        return null;
    }

    @Override
    public Void visit(BinaryOperator binaryOperatorNode) {
        importsLegal = false;
        return null;
    }

    @Override
    public Void visit(Condition conditionNode) {
        importsLegal = false;
        return null;
    }

    @Override
    public Void visit(Comparison comparisonNode) {
        importsLegal = false;
        return null;
    }

    @Override
    public Void visit(Sequence sequenceNode) {
        importsLegal = false;
        for (Node node : sequenceNode.getNodes()) {
            node.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Function functionNode) {
        importsLegal = false;
        for (Node node : functionNode.getBody()) {
            node.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Identifier identifierNode) {
        importsLegal = false;
        return null;
    }

    @Override
    public Void visit(Import importNode) {
        if (!importsLegal) {
            reporter.error(
                    "Cannot import here, all imports must be first.",
                    importNode.getToken().getLine(), importNode.getToken().getCharacter()
            );
        }
        return null;
    }

    @Override
    public Void visit(Let letNode) {
        importsLegal = false;
        return null;
    }

    @Override
    public Void visit(Array arrayNode) {
        importsLegal = false;
        return null;
    }
}
