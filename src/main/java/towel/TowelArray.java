package towel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents an array in the running program
 */
public class TowelArray {

    private final static TowelArray EMPTY_ARRAY = new TowelArray(new Object[0]);

    private final List<Object> array = new ArrayList<>();
    private final String type;

    private TowelArray(Object[] contents) {
        array.addAll(Arrays.asList(contents));

        // @todo comparing types using strings perhaps isn't the best approach, should rethink
        type = contents.getClass().getSimpleName().replace("[]", "");
    }

    /**
     * Create an array with the given contents
     *
     * Arrays cannot contain mixed types, so all elements must be of the same type
     */
    public static TowelArray of(Object... contents) {
        if (contents.length == 0) {
            return EMPTY_ARRAY;
        }

        if (!areAllOfType(contents[0].getClass(), contents)) {
            throw new InvalidArrayValueError("Items in an array must all be of the same type.");
        }

        // @todo support more types in arrays

        if (contents[0] instanceof Double) {
            return new TowelArray(Arrays.copyOf(contents, contents.length, Double[].class));
        } else if (contents[0] instanceof String) {
            return new TowelArray(Arrays.copyOf(contents, contents.length, String[].class));
        } else if (contents[0] instanceof Boolean) {
            return new TowelArray(Arrays.copyOf(contents, contents.length, Boolean[].class));
        }

        throw new InvalidArrayValueError(
                String.format("Invalid type. Only '%s', '%s', or '%s' can be added to an array.",
                        TypeNameTranslator.get("Double"),
                        TypeNameTranslator.get("Boolean"),
                        TypeNameTranslator.get("String")
                )
        );
    }

    private static boolean areAllOfType(Class expectedType, Object... objects) {
        for (Object object : objects) {
            if (!expectedType.isInstance(object)) {
                return false;
            }
        }
        return true;
    }

    public Object pop() {
        return array.remove(array.size() - 1);
    }

    public void push(Object element) {
        assertValidValue(element);
        array.add(element);
    }

    private void assertValidValue(Object value) {
        if (!canAcceptValue(value)) {
            throw new FunctionExecutionError(
                    String.format(
                            "Invalid type for array. Expected %s, received %s.",
                            TypeNameTranslator.get(type),
                            TypeNameTranslator.get(value.getClass().getSimpleName())
                    )
            );
        }
    }

    private boolean isOfType(Class<?> check) {
        return check.getSimpleName().equals(type);
    }

    private boolean canAcceptValue(Object value) {
        return isInitialized() && isOfType(value.getClass());
    }

    /**
     * An array is considered 'initialized' once it has at least one value contained inside it
     *
     * Once initialized, an array's type is locked, meaning all subsequent values added to it must
     * be of the same type
     */
    public boolean isInitialized() {
        return !type.equals("Object") && !isEmpty();
    }

    public int size() {
        return array.size();
    }

    public boolean isEmpty() {
        return array.isEmpty();
    }

    public Object get(int index) {
        return array.get(index);
    }

    public void set(int index, Object element) {
        assertValidValue(element);
        array.set(index, element);
    }

    @Override
    public String toString() {
        return TypeNameTranslator.get(getClass()) + " type='" + TypeNameTranslator.get(type) + "' values=[" + stringifyVals() + "]";
    }

    private String stringifyVals() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.size(); i++) {
            sb.append(array.get(i));
            if (i + 1 < array.size()) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

}
