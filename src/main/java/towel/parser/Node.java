package towel.parser;

public interface Node {
    <T> T accept(NodeVisitor<T> visitor);
}
