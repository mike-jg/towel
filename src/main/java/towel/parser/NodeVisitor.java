package towel.parser;

public interface NodeVisitor<T> {
    T visit(Program programNode);

    T visit(Literal literalNode);

    T visit(BinaryOperator binaryOperatorNode);

    T visit(Condition conditionNode);

    T visit(Comparison comparisonNode);

    T visit(Sequence sequenceNode);

    T visit(Function functionNode);

    T visit(Identifier identifierNode);

    T visit(Import importNode);

    T visit(Let letNode);

    T visit(Array arrayNode);
}
