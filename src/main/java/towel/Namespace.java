package towel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a namespace, tracks identifiers and their values contained within
 */
public class Namespace {

    private Map<String, Object> identifiers = new HashMap<>();
    private List<String> userDefinedNames = new ArrayList<>();

    public void clear() {
        identifiers.clear();
    }

    public String[] getNames() {
        return userDefinedNames.toArray(new String[userDefinedNames.size()]);
    }

    public Namespace exportPublicMembers() {
        Namespace export = new Namespace();

        for (HashMap.Entry<String, Object> entry : identifiers.entrySet())
        {
            // export all user-defined functions and lets
            if (entry.getValue() instanceof LetFunction || entry.getValue() instanceof UserDefinedFunction ) {
                export.define(entry.getKey(), entry.getValue());
            }
        }

        return export;
    }

    public void define(String name, Object value) {
        // everything that's been user defined in a particular namespace should be 'exported'
        if (value instanceof LetFunction || value instanceof UserDefinedFunction) {
            userDefinedNames.add(name);
        }
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

        throw new RuntimeException(String.format("Invalid namespace requested '%s'.", name));
    }

    public Object get(String name) {
        if (identifiers.containsKey(name)) {
            return identifiers.get(name);
        }
        return null;
    }
}
