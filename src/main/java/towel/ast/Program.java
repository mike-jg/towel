package towel.ast;

import java.util.List;
import java.util.Objects;

public class Program implements Node {

    private final String namespaceName;
    private final List<Node> nodes;
    private final List<Import> imports;
    private boolean isRootNode = true;
    private ProgramType programType = ProgramType.USER;

    public enum ProgramType {
        INTERNAL, USER
    }

    public final static String DEFAULT_NAMESPACE = "DEFAULTNS";

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

    /**
     * If this is the top level, that means it's the 'root' or 'entry point' of the application
     *
     * If this isn't top level, it means it's an import
     */
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
