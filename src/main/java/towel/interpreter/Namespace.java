package towel.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a namespace, tracks identifiers and their values contained within
 */
public class Namespace {

    /**
     * All identifiers
     */
    private Map<String, Object> identifiers = new HashMap<>();

    /**
     * A list of public identifiers, i.e. those that would be visible to a parent namespace
     */
    private List<String> publicMembers = new ArrayList<>();

    public void clear() {
        identifiers.clear();
    }

    /**
     *
     * @return all of the public member names
     */
    public String[] getPublicMemberNames() {
        return publicMembers.toArray(new String[publicMembers.size()]);
    }

    /**
     * Create a new Namespace, containing only public exportable names from this namespace
     *
     * @return a namespace containing only the public members of this namespace
     */
    public Namespace exportPublicMembers() {

        Namespace export = new Namespace();

        for (String name : getPublicMemberNames()) {
            export.definePublicMember(name, get(name));
        }

        return export;
    }

    /**
     * Define a new public member
     *
     * @param name name of the member
     * @param value value of the member
     */
    public void definePublicMember(String name, Object value) {
        publicMembers.add(name);
        definePrivateMember(name, value);
    }

    /**
     * Define a private member
     * <p>
     * A private member will not be exported
     * </p>
     *
     * @param name name of the member
     * @param value value of the member
     */
    public void definePrivateMember(String name, Object value) {
        identifiers.put(name, value);
    }

    /**
     *
     * @param name the name to look up
     * @return whether the name is defined
     */
    public boolean isDefined(String name) {
        return identifiers.containsKey(name);
    }

    /**
     * Get the given child namespace
     *
     * @param name name of a namespace
     * @return the namespace
     */
    public Namespace getNamespace(String name) {
        if (identifiers.containsKey(name)) {
            Object ns = identifiers.get(name);

            if (ns instanceof Namespace) {
                return (Namespace) ns;
            }
        }

        throw new IllegalArgumentException(String.format("Invalid namespace requested '%s'.", name));
    }

    /**
     *
     * @param name identifier name
     * @return the identifier
     */
    public Object get(String name) {
        if (identifiers.containsKey(name)) {
            return identifiers.get(name);
        }
        return null;
    }
}
