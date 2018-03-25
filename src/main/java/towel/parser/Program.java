package towel.parser;

import java.util.List;

public interface Program extends Node {

    String DEFAULT_NAMESPACE = "DEFAULTNS";

    enum ProgramType {
        INTERNAL, USER
    }

    boolean isInternal();

    void setProgramType(ProgramType programType);

    boolean isDefaultNamespace();

    /**
     * If this is the top level, that means it's the 'root' or 'entry point' of the application
     *
     * If this isn't top level, it means it's an import
     */
    boolean isRootNode();

    void notRootNode();

    String getNamespace();

    List<Node> getNodes();

    List<Import> getImports();
}
