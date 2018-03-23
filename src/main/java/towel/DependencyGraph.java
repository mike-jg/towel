package towel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detects any circular dependencies in imports
 *
 * @see <a href="https://en.wikipedia.org/wiki/Topological_sorting">Topological sorting</a>
 */
public class DependencyGraph {

    private enum Mark {
        PERMANENT, TEMPORARY, NONE
    }

    /**
     * Dependency node
     */
    private static class Node {

        /**
         * Dependency name - probably a file name or namespace name
         */
        final String name;

        /**
         * A list of Nodes that this Node is dependent upon
         */
        final List<Node> dependencies = new ArrayList<Node>();

        /**
         * Current mark state of the node
         * <p>
         * Used in the search below to mark which nodes have been visited
         */
        Mark mark = Mark.NONE;

        public Node(String name) {
            this.name = name;
        }

        boolean isTemporary() {
            return mark == Mark.TEMPORARY;
        }

        boolean isPermanent() {
            return mark == Mark.PERMANENT;
        }

        void markPermanent() {
            mark = Mark.PERMANENT;
        }

        void markTemporary() {
            mark = Mark.TEMPORARY;
        }
    }

    /**
     * Root dependency node - the application entry point
     */
    private Node root = null;

    /**
     * All Nodes, easy way to prevent duplicate Nodes
     */
    private Map<String, Node> nodes = new HashMap<>();

    /**
     * Unmarked Nodes
     */
    private List<Node> unmarkedNodes = new ArrayList<>();

    /**
     * Whether the graph has been closed
     * <p>
     * If the graph is closed, that means that something in the dependency graph
     * has a dependency back to the root node. This is an easy indication of a circular dependency
     * <p>
     * e.g. the graph looks like this:
     * <p>
     * A -> B -> C -> D -> A ... etc.
     */
    private boolean closedGraph = false;

    /**
     * @param rootName the root of the dependency graph
     */
    public DependencyGraph(String rootName) {
        root = createNode(rootName);
    }

    /**
     * Add a dependency to the given name
     *
     * @param name       the name to which the dependency will be added
     * @param dependency the dependency
     */
    public void addDependency(String name, String dependency) {
        if (closedGraph) {
            return;
        }

        Node node = createNode(name);
        Node dep = createNode(dependency);

        if (dep == root) {
            closedGraph = true;
            return;
        }

        node.dependencies.add(dep);
    }

    /**
     * Has this dependency graph found a circular dependency?
     *
     * @return whether a circular dependency has been found
     */
    public boolean hasCircularDependency() {
        if (closedGraph) {
            return true;
        }

        return searchForCircularDependency();
    }

    /**
     * Create a new node, or return the existing node if one already exists for this name
     *
     * @param name the node's name
     * @return a node
     */
    private Node createNode(String name) {
        if (!nodes.containsKey(name)) {
            Node node = new Node(name);
            nodes.put(name, node);
        }
        return nodes.get(name);
    }

    /**
     * @return whether there's a circular dependency
     * @see <a href="https://en.wikipedia.org/wiki/Topological_sorting#Depth-first_search">Depth-first search</a>
     */
    private boolean searchForCircularDependency() {
        unmarkedNodes.clear();
        unmarkedNodes.addAll(nodes.values());
        unmarkedNodes.forEach(node -> node.mark = Mark.NONE);

        while (unmarkedNodes.size() > 0) {
            Node unmarkedNode = unmarkedNodes.get(0);
            if (!visit(unmarkedNode)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param node node
     * @return returns false when the algorithm should stop - at this point a circular dependency has been found
     */
    private boolean visit(Node node) {
        if (node.isPermanent()) {
            return true;
        }
        // Somewhere in this node's dependency graph
        // something has a dependency back to it, meaning we
        // have a circular dependency
        if (node.isTemporary()) {
            return false;
        }

        node.markTemporary();
        unmarkedNodes.remove(node);

        for (Node dep : node.dependencies) {
            if (!visit(dep)) {
                return false;
            }
        }

        node.markPermanent();
        return true;
    }

}
