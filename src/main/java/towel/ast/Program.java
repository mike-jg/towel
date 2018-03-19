package towel.ast;

import java.util.List;
import java.util.Objects;

public class Program implements Node {

    private final String namespaceName;
    private final List<Node> nodes;
    private final List<FileImport> fileImports;
    private final List<Import> imports;
    private boolean isTopLevel = true;

    public final static String DEFAULT_NAMESPACE = "DEFAULTNS";

    public Program(String namespaceName, List<Node> nodes, List<Import> imports, List<FileImport> fileImports) {
        this.namespaceName = Objects.requireNonNull(namespaceName);
        this.nodes = Objects.requireNonNull(nodes);
        this.fileImports = Objects.requireNonNull(fileImports);
        this.imports = Objects.requireNonNull(imports);
    }

    public boolean isDefaultNamespace() {
        return namespaceName.equals(DEFAULT_NAMESPACE);
    }

    /**
     * If this is the top level, that means it's the 'root' or 'entry point' of the application
     *
     * If this isn't top level, it means it's an import
     */
    public boolean isTopLevel() {
        return isTopLevel;
    }

    public void setTopLevel(boolean topLevel) {
        isTopLevel = topLevel;
    }

    public String getNamespace() {
        return namespaceName;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<FileImport> getFileImports() {
        return fileImports;
    }

    public List<Import> getImports() {
        return imports;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
