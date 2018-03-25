package towel.interpreter;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Translates Java names to the 'Towel' version
 */
public class TypeNameTranslator {

    private final static Map<String, String> names = new HashMap<>();

    static {
        names.put("Boolean", "bool");
        names.put("Double", "num");
        names.put("String", "str");
        names.put("Sequence", "seq");
        names.put("TowelArray", "array");
        names.put("Object", "any");
    }

    private TypeNameTranslator() {

    }

    public static String get(Class name) {
        Objects.requireNonNull(name);

        return get(name.getSimpleName());
    }

    public static String get(String name) {
        Objects.requireNonNull(name);

        if (names.containsKey(name)) {
            return names.get(name);
        }

        throw new IllegalArgumentException(String.format("Unknown name: %s", name));
    }

}
