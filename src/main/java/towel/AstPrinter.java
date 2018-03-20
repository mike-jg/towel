package towel;

import towel.ast.*;

import java.util.Objects;

/**
 * Print a human readable output of the AST
 */
class AstPrinter implements NodeVisitor<Void> {

    private StringBuilder sb;
    private int depth = 0;
    private String tabChar = "\t";

    public AstPrinter(String tabChar) {
        this.tabChar = Objects.requireNonNull(tabChar);
    }

    public AstPrinter() {
    }

    String print(Program program) {
        Objects.requireNonNull(program);
        sb = new StringBuilder();

        program.accept(this);

        return sb.toString().trim();
    }

    @Override
    public Void visit(Program programNode) {
        int size = programNode.getNodes().size();
        int idx = 0;

        // don't bother wrapping the default namespace around
        // anything, it doesn't really add any benefit
        if (!programNode.isDefaultNamespace()) {
            append("(PROGRAM ");
            append(programNode.getNamespace());
            indent();
        }

        for (Node e : programNode.getNodes()) {
            idx++;
            e.accept(this);
            if (idx < size) {
                append("\n");
            }
        }

        if (!programNode.isDefaultNamespace()) {
            unindent();
            append(")");
        }

        return null;
    }

    private void printNodes(Node nodes[]) {
        for (Node node : nodes) {
            node.accept(this);
            if (node != nodes[nodes.length - 1]) {
                append("\n");
            }
        }
    }

    private void indent() {
        depth++;
        sb.append("\n");
    }

    private void unindent() {
        depth--;
        sb.append("\n");
    }

    private void unindent(int amount) {
        depth = depth - amount;
        sb.append("\n");
    }

    private void append(String str) {
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            for (int i = 0; i < depth; i++) {
                sb.append(tabChar);
            }
        }
        sb.append(str);
    }

    @Override
    public Void visit(Literal literalNode) {
        if (literalNode.getTokenType() == Token.TokenType.STRING_LITERAL) {
            parenthesize(literalNode.getTokenType().toString(), literalNode.getLexeme());
            return null;
        }
        parenthesize(literalNode.getTokenType().toString(), literalNode.getLiteralAsString());
        return null;
    }

    @Override
    public Void visit(BinaryOperator binaryOperatorNode) {
        parenthesize("BINARY_OPERATOR", binaryOperatorNode.getTokenType().toString(), binaryOperatorNode.getLexeme());
        return null;
    }

    @Override
    public Void visit(Sequence sequenceNode) {

        append("(SEQUENCE " + sequenceNode.getTokenType().toString());
        indent();
        printNodes(sequenceNode.getNodes());
        unindent();
        append(")");

        return null;
    }

    @Override
    public Void visit(Condition conditionNode) {
        parenthesize("CONDITION", conditionNode.getTokenType().toString());
        return null;
    }

    @Override
    public Void visit(Comparison comparisonNode) {
        append("(COMPARISON ");
        parenthesize(comparisonNode.getTokenType().toString(), comparisonNode.getLexeme());
        append(")");
        return null;
    }

    @Override
    public Void visit(Function functionNode) {
        append("(DEF");
        indent();

        parenthesize(functionNode.getTokenType().toString(), functionNode.getLiteralAsString());
        indent();

        printNodes(functionNode.getBody());

        unindent(2);
        append(")");
        return null;
    }

    @Override
    public Void visit(Identifier identifierNode) {
        parenthesize(identifierNode.getTokenType().toString(), identifierNode.getOriginalName());
        return null;
    }

    @Override
    public Void visit(Import importNode) {
        append("(IMPORT");
        indent();

        String[] target = new String[importNode.getTarget().length + 1];
        target[0] = "TARGET";
        System.arraycopy(importNode.getTarget(), 0, target, 1, importNode.getTarget().length);
        parenthesize(target);
        append("\n");

        parenthesize("FROM", importNode.getNamespace());
        if (importNode.isAliased()) {
            append("\n");
            parenthesize("AS", importNode.getAlias());
        }

        unindent();
        append(")");
        return null;
    }

    @Override
    public Void visit(FileImport fileImportNode) {
        parenthesize(fileImportNode.getTokenType().toString(), fileImportNode.getFile());
        return null;
    }

    @Override
    public Void visit(Let letNode) {
        parenthesize("LET", letNode.getLexeme());
        return null;
    }

    private void parenthesize(String... str) {
        append("(");
        for (int i = 0; i < str.length; i++) {
            append(str[i]);
            if (i + 1 < str.length) {
                append(" ");
            }
        }
        append(")");
    }

    @Override
    public Void visit(Array arrayNode) {
        append("(ARRAY");
        indent();
        for (int i = 0; i < arrayNode.getContents().length; i++) {
            append(arrayNode.getContents()[i].toString());
            if (i + 2 <= arrayNode.getContents().length) {
                append(" ");
            }
        }
        unindent();
        append(")");
        return null;
    }
}


