package towel.interpreter;

import towel.LibraryMetadata;
import towel.stdlib.ClassMap;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

/**
 * Loads the parts of the standard library that are implemented in Java
 */
public class NativeNamespaceLoader implements NamespaceLoader {

    private final PrintStream stream;
    private final Scanner scanner;

    /**
     * The names of library functions are stored here, in the form:
     * <pre>
     * namespace -> function name -> implementation
     *           -> function name -> implementation
     *           -> function name -> implementation
     *
     * namespace -> function name -> implementation
     *           -> function name -> implementation
     * </pre>
     * According to the classes annotation data provided by the {@code LibraryMetadata} annotation
     *
     * @see LibraryMetadata
     */
    private final Map<String, Map<String, Class>> libraryMap = new HashMap<>();

    /**
     * Instances of library classes
     */
    private final Map<String, Map<String, TowelFunction>> instanceMap = new HashMap<>();

    /**
     *
     * @param stream the stream to inject into library functions, for outputting
     * @param scanner the scanner to inject into library functions, for receiving input
     */
    public NativeNamespaceLoader(PrintStream stream, Scanner scanner) {
        this.stream = Objects.requireNonNull(stream);
        this.scanner = Objects.requireNonNull(scanner);
        for (Class c : ClassMap.libraryClasses) {
            LibraryMetadata attribs = (LibraryMetadata) c.getAnnotation(LibraryMetadata.class);

            if (!libraryMap.containsKey(attribs.namespace())) {
                libraryMap.put(attribs.namespace(), new HashMap<>());
            }
            libraryMap.get(attribs.namespace()).put(attribs.name(), c);
        }
    }

    @Override
    public boolean hasNamespace(String name) {
        return libraryMap.containsKey(name);
    }

    @Override
    public String[] getPublicNamesInNamespace(String namespace) {
        assertLibrary(namespace);
        return libraryMap.get(namespace).keySet().toArray(new String[0]);
    }

    private void assertLibrary(String name) {
        if (!hasNamespace(name)) {
            throw new MissingLibraryException(String.format("Specified library does not exist: '%s'.", name));
        }
    }

    @Override
    public TowelFunction getFunction(String namespace, String functionName) {
        assertLibrary(namespace);
        ensureLoaded(namespace, functionName);
        return instanceMap.get(namespace).get(functionName);
    }

    /**
     * If the given function isn't loaded, try to load it
     */
    private void ensureLoaded(String libraryName, String functionName) {
        if (!instanceMap.containsKey(libraryName)) {
            instanceMap.put(libraryName, new HashMap<>());
        }
        if (!instanceMap.get(libraryName).containsKey(functionName)) {
            Class clazz = libraryMap.get(libraryName).get(functionName);
            TowelFunction function = loadClass(clazz);
            instanceMap.get(libraryName).put(functionName, function);
        }
    }

    /**
     * Load the given class
     */
    private TowelFunction loadClass(Class<?> libClass) {
        try {
            TowelFunction function = (TowelFunction) libClass.getConstructor().newInstance();

            addDependencies(function);

            return function;

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not import " + libClass.getName() + ". Failed on instantiate. " + e.toString());
        }
    }

    /**
     * Add dependencies to the given function
     */
    private void addDependencies(TowelFunction function) {
        if (function instanceof RequiresPrintStream) {
            ((RequiresPrintStream) function).setPrintStream(stream);
        }
        if (function instanceof RequiresScanner) {
            ((RequiresScanner) function).setScanner(scanner);
        }
    }

    @Override
    public boolean namespaceContainsFunction(String namespace, String functionName) {
        if (!hasNamespace(namespace)) {
            return false;
        }

        return libraryMap.get(namespace).containsKey(functionName);
    }
}
