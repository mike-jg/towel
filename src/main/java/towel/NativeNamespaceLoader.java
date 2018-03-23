package towel;

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

    private final Map<String, Map<String, Class>> libraryMap = new HashMap<>();
    private final Map<String, Map<String, TowelFunction>> instanceMap = new HashMap<>();

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
    public boolean hasLibrary(String name) {
        return libraryMap.containsKey(name);
    }

    @Override
    public String[] getNamesInLibrary(String name) {
        assertLibrary(name);
        return libraryMap.get(name).keySet().toArray(new String[0]);
    }

    private void assertLibrary(String name) {
        if (!hasLibrary(name)) {
            throw new MissingLibraryException(String.format("Specified library does not exist: '%s'.", name));
        }
    }

    @Override
    public TowelFunction getFunction(String namespace, String functionName) {
        assertLibrary(namespace);
        ensureLoaded(namespace, functionName);
        return instanceMap.get(namespace).get(functionName);
    }

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

    private TowelFunction loadClass(Class<?> libClass) {
        try {
            TowelFunction function = (TowelFunction) libClass.getConstructor().newInstance();

            addDependencies(function);

            return function;

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not import " + libClass.getName() + ". Failed on instantiate. " + e.toString());
        }
    }

    private void addDependencies(TowelFunction function) {
        if (function instanceof RequiresPrintStream) {
            ((RequiresPrintStream) function).setPrintStream(stream);
        }
        if (function instanceof RequiresScanner) {
            ((RequiresScanner) function).setScanner(scanner);
        }
    }

    @Override
    public boolean libraryContainsFunction(String library, String func) {
        if (!hasLibrary(library)) {
            return false;
        }

        return libraryMap.get(library).containsKey(func);
    }
}
