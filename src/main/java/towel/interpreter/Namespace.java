package towel.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a namespace, tracks identifiers and their values contained within
 */
public class Namespace {

    private Map<String, Object> identifiers = new HashMap<>();
    private List<String> publicMembers = new ArrayList<>();

    public void clear() {
        identifiers.clear();
    }

    public String[] getPublicMembers() {
        return publicMembers.toArray(new String[publicMembers.size()]);
    }

    /**
     * Create a new Namespace, containing only public exportable names from this namespace
     *
     * @return a namespace containing only the public members of this namespace
     */
    public Namespace exportPublicMembers() {

        Namespace export = new Namespace();

        for (String name : getPublicMembers()) {
            export.definePublicMember(name, get(name));
        }

        return export;
    }

    public void definePublicMember(String name, Object value) {
        publicMembers.add(name);
        definePrivateMember(name, value);
    }

    public void definePrivateMember(String name, Object value) {
        identifiers.put(name, value);
    }

    public boolean isDefined(String name) {
        return identifiers.containsKey(name);
    }

    public Namespace getNamespace(String name) {
        if (identifiers.containsKey(name)) {
            Object ns = identifiers.get(name);

            if (ns instanceof Namespace) {
                return (Namespace) ns;
            }
        }

        throw new IllegalArgumentException(String.format("Invalid namespace requested '%s'.", name));
    }

    public Object get(String name) {
        if (identifiers.containsKey(name)) {
            return identifiers.get(name);
        }
        return null;
    }
}
