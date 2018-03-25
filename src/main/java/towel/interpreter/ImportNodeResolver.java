package towel.interpreter;

import towel.parser.Import;

import java.io.File;

/**
 * Resolves import nodes, that is to say provides an interface to query them in a more abstract way.
 * <p>
 * This is mostly aimed at trying to simplify determining where to look for imported files,
 * as well as simplifying the conversion of an import name to a filename
 */
public abstract class ImportNodeResolver {

    private final Import node;

    private ImportNodeResolver(Import node) {
        this.node = node;
    }

    public static ImportNodeResolver wrap(Import node) {
        if (node.getNamespace().endsWith(".twl")) {
            return new ExternalImport(node);
        } else {
            return new InternalImport(node);
        }
    }

    protected final Import getNode() {
        return node;
    }

    /**
     * A namespace import is:
     * <pre>import from &lt;namespace&gt;</pre>
     *
     * @return whether this is importing a whole namespace
     */
    public boolean isImportingWholeNamespace() {
        return getTarget().length == 0 && !isAliased();
    }

    /**
     * A star import is:
     * <pre>import * from &lt;namespace&gt;</pre>
     *
     * @return whether this is a star import
     */
    public boolean isStarImport() {
        return node.getTarget().length == 1 && node.getTarget()[0].equals("*");
    }

    /**
     * Return the targets of this import.
     * <p>
     * The targets are the objects being pulled in by the namespace.
     *
     * @return the targets
     */
    public String[] getTarget() {
        return node.getTarget();
    }

    public String getAlias() {
        return node.getAlias();
    }

    public boolean isAliased() {
        return getAlias() != null;
    }

    /**
     * Is this an external import?
     *
     * <ul>
     * <li>An external import is a user-supplied file</li>
     * <li>An internal import is an internal .twl file, or a Java class</li>
     * </ul>
     *
     * @return whether this is external
     */
    public abstract boolean isExternal();

    /**
     * Get the file name that this import is referring to
     *
     * @return the filename
     */
    public abstract String getFileName();

    /**
     * Get the normalized form of the namespace, that is the form that is used
     * internally to represent a namespace in the environment
     *
     * @return the normalized namespace
     */
    public abstract String getNormalized();

    public String getNamespace() {
        return node.getNamespace();
    }

    private static class InternalImport extends ImportNodeResolver {
        public InternalImport(Import node) {
            super(node);
        }

        @Override
        public boolean isExternal() {
            return false;
        }

        @Override
        public String getFileName() {
            return getNamespace() + ".twl";
        }

        @Override
        public String getNormalized() {
            return getNamespace();
        }
    }

    private static class ExternalImport extends ImportNodeResolver {

        private String parsedNamespace;
        private String normalized;

        public ExternalImport(Import node) {
            super(node);
        }

        @Override
        public String getNamespace() {
            if (parsedNamespace != null) {
                return parsedNamespace;
            }

            parsedNamespace = super.getNamespace();
            int sepChar;
            // return everything after the last :, i.e. just the filename
            if ((sepChar = parsedNamespace.lastIndexOf(":")) > 0) {
                parsedNamespace = parsedNamespace.substring(sepChar + 1);
            }
            return parsedNamespace;
        }

        @Override
        public boolean isExternal() {
            return true;
        }

        @Override
        public String getFileName() {
            return getNode().getNamespace().replace(":", File.separator);
        }

        @Override
        public String getNormalized() {
            if (normalized != null) {
                return normalized;
            }

            normalized = getNamespace();

            return normalized = normalized.substring(
                    Math.max(normalized.lastIndexOf(":"), 0),
                    normalized.lastIndexOf(".twl")
            );
        }
    }
}
