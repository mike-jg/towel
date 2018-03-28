package towel.ast;

import java.util.List;
import java.util.Objects;

public class Program implements Node {

    public static final String DEFAULT_NAMESPACE = "DEFAULTNS";

    public enum ProgramType {
        INTERNAL, USER
    }

    private final String namespaceName;
    private final List<Node> nodes;
    private final List<Import> imports;
    private boolean isRootNode = true;
    private ProgramType programType = ProgramType.USER;

    public Program(String namespaceName, List<Node> nodes, List<Import> imports) {
        this.namespaceName = Objects.requireNonNull(namespaceName);
        this.nodes = Objects.requireNonNull(nodes);
        this.imports = Objects.requireNonNull(imports);
    }

    public boolean isInternal() {
        return programType == ProgramType.INTERNAL;
    }

    public void setProgramType(ProgramType programType) {
        this.programType = programType;
    }

    public boolean isDefaultNamespace() {
        return namespaceName.equals(DEFAULT_NAMESPACE);
    }

    public boolean isRootNode() {
        return isRootNode;
    }

    public void notRootNode() {
        isRootNode = false;
    }

    public String getNamespace() {
        return namespaceName;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Import> getImports() {
        return imports;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
