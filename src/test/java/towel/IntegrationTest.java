package towel;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

public class IntegrationTest {

    private OutputStream outputStream;
    private PrintStream printStream;
    private final static String searchDir = Paths.get("src", "test", "resources", "integration-test").toAbsolutePath().toString();

    @Test
    public void testUsage() throws IOException {
        setStreams();

        App.setPrintStream(printStream);
        App.main(new String[]{});

        assertTrue(outputStream.toString().startsWith("Usage: towel"));
    }

    @Test
    public void testCallsGenerator() throws IOException {
        setStreams();

        StdLibraryMapGenerator mock = Mockito.mock(StdLibraryMapGenerator.class);

        App.setPrintStream(printStream);
        App.setStdLibraryMapGenerator(mock);
        App.main(new String[]{
                "--generate-std-map"
        });

        verify(mock).generate();
    }

    @Test
    public void testBadFile() throws IOException {
        setStreams();

        App.setPrintStream(printStream);
        App.main(new String[]{
                "asdlaksjdlaks.twl"
        });

        assertTrue(outputStream.toString().startsWith("Error reading input file: asdlaksjdlaks.twl"));
    }

    private void setStreams() throws UnsupportedEncodingException {
        outputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outputStream, false, "UTF-8");
    }

    private List<File> gatherFiles(String dir) {
        File[] files = new File(dir).listFiles();
        List<File> allFiles = new ArrayList<>();

        if (files == null) {
            return allFiles;
        }

        for (File f : files) {
            if (f.getName().endsWith(".twl")) {
                continue;
            }

            if (f.isDirectory()) {
                allFiles.addAll(gatherFiles(f.getAbsolutePath()));
            } else {
                allFiles.add(f);
            }
        }

        return allFiles;
    }

    @TestFactory
    public Collection<DynamicTest> testInExpectDirectory() throws IOException {
        List<File> files = gatherFiles(searchDir);
        Collection<DynamicTest> tests = new ArrayList<>();

        for (File assertionFile : files) {

            String sourceDirectory = assertionFile.getParent();

            Executable exec;

            if (assertionFile.getName().endsWith(".expect")) {

                String codeName = assertionFile.getName().replace(".expect", ".twl");
                String codePath = Paths.get(sourceDirectory, codeName).toAbsolutePath().toString();

                exec = runTest(new String[]{
                        codePath,
                        "--suppress-notices"
                }, assertionFile.getAbsolutePath());

            } else if (assertionFile.getName().endsWith(".ast")) {

                String codeName = assertionFile.getName().replace(".ast", ".twl");
                String codePath = Paths.get(sourceDirectory, codeName).toAbsolutePath().toString();

                exec = runTest(new String[]{
                        codePath,
                        "--print-ast",
                        "--suppress-notices"
                }, assertionFile.getAbsolutePath());

            } else {
                continue;
            }

            DynamicTest test = DynamicTest.dynamicTest(assertionFile.getName(), exec);

            tests.add(test);
        }

        return tests;
    }

    private Executable runTest(String[] args, String expectedOutputFile) throws IOException {
        setStreams();

        App.reset();
        App.setPrintStream(printStream);
        App.main(args);

        String expect = readFile(expectedOutputFile);
        String actual = outputStream.toString();

        return () -> assertEquals(expect, actual, String.valueOf(args.length > 0 ? args[0] : ""));
    }

    private static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, Charset.forName("UTF-8"));
    }

}
