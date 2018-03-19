package towel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates the standard library class map
 *
 * @see towel.stdlib.ClassMap
 */
public class StdLibraryMapGenerator {

    private final static String PATH_TO_TEMPLATE = Paths.get("src", "main", "resources", "ClassMap.template").toAbsolutePath().toString();
    private final static Path PATH_TO_CLASS_MAP_FILE = Paths.get("src", "main", "java", "towel", "stdlib", "ClassMap.java").toAbsolutePath();
    private final static String PATH_TO_STD_LIBRARY = Paths.get("src", "main", "java", "towel", "stdlib").toAbsolutePath().toString();
    private final static Pattern PACKAGE_PATTERN = Pattern.compile("package (.*);");
    private final static Pattern CLASS_NAME_PATTERN = Pattern.compile("class (\\w+) implements TowelFunction");
    private StringBuilder classBuilder;


    public void generate() throws IOException {

        classBuilder = new StringBuilder();

        String templateFileContents = readFile(PATH_TO_TEMPLATE);
        List<File> files = getAllFiles(PATH_TO_STD_LIBRARY);

        generateClassList(files);

        String classes = classBuilder.toString();

        templateFileContents = templateFileContents
                .replace("{{CLASSES}}", classes.substring(0, classes.length() - 2));

        Files.write(PATH_TO_CLASS_MAP_FILE, templateFileContents.getBytes(Charset.forName("utf-8")));

        templateFileContents = null;
        classBuilder = null;
    }

    private List<File> getAllFiles(String path) {
        List<File> files = new ArrayList<>();
        File[] dirFiles = new File(path).listFiles();
        if (dirFiles == null) {
            return files;
        }

        for (File libraryFile : dirFiles) {
            if (libraryFile.getName().equals(PATH_TO_CLASS_MAP_FILE.getFileName().toString())) {
                continue;
            } else if (libraryFile.isDirectory()) {
                files.addAll(getAllFiles(libraryFile.getAbsolutePath()));
            } else if (libraryFile.getName().endsWith(".java")) {
                files.add(libraryFile);
            }
        }
        return files;
    }

    private void generateClassList(List<File> files) throws IOException {
        for (File f : files) {
            String fileContents = readFile(f.getAbsolutePath());
            writeImport(fileContents);
        }
    }

    private void writeImport(String fileContents) {
        Matcher packageMatcher = PACKAGE_PATTERN.matcher(fileContents);
        Matcher classNameMatcher = CLASS_NAME_PATTERN.matcher(fileContents);
        String className = "";
        String packageName = "";

        while (classNameMatcher.find()) {
            className = classNameMatcher.group(1);
        }
        while (packageMatcher.find()) {
            packageName = packageMatcher.group(1);
        }

        classBuilder.append(String.format("        %s.%s.class,\n", packageName, className));
    }

    private String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, Charset.forName("utf-8"));
    }

}
