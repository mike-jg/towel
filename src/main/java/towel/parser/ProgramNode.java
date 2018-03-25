package towel.parser;

import java.util.List;
import java.util.Objects;

class ProgramNode implements Program {

    private final String namespaceName;
    private final List<Node> nodes;
    private final List<Import> imports;
    private boolean isRootNode = true;
    private ProgramType programType = ProgramType.USER;

    ProgramNode(String namespaceName, List<Node> nodes, List<Import> imports) {
        this.namespaceName = Objects.requireNonNull(namespaceName);
        this.nodes = Objects.requireNonNull(nodes);
        this.imports = Objects.requireNonNull(imports);
    }

    @Override
    public boolean isInternal() {
        return programType == ProgramType.INTERNAL;
    }

    @Override
    public void setProgramType(ProgramType programType) {
        this.programType = programType;
    }

    @Override
    public boolean isDefaultNamespace() {
        return namespaceName.equals(DEFAULT_NAMESPACE);
    }

    @Override
    public boolean isRootNode() {
        return isRootNode;
    }

    @Override
    public void notRootNode() {
        isRootNode = false;
    }

    @Override
    public String getNamespace() {
        return namespaceName;
    }

    @Override
    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public List<Import> getImports() {
        return imports;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
