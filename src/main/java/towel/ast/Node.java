package towel.ast;

public interface Node {
    <T> T accept(NodeVisitor<T> visitor);
}
