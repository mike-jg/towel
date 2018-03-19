package towel;

import towel.ast.FileImport;
import towel.ast.Import;
import towel.ast.Node;
import towel.ast.Program;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Run the full 'pipeline' on a single source file
 *
 * This will recursively interpret all external imports too
 *
 * As this was quickly thrown together to allow the recursive parsing of imports,
 * it needs revisiting and tidying up
 *
 * @todo rethink how this works in general
 * @todo rethink how imports work specifically
 */
public class SourceFileInterpreter {

    private PrintStream outputStream;
    private FileAwareLoggingErrorReporter reporter;
    private NamespaceLoaderStack loader = new NamespaceLoaderStack();
    private Options options;
    private Map<String, Boolean> fileLoadRequests = new HashMap<>();
    private final static String STD_LIB_PATH = Paths.get("src/main/resources/standard-lib/").toAbsolutePath().toString();

    private class ProgramError extends RuntimeException {
        public ProgramError() {
        }
    }

    public SourceFileInterpreter(PrintStream outputStream, FileAwareLoggingErrorReporter reporter, NamespaceLoader loader, Options options) {
        this.outputStream = Objects.requireNonNull(outputStream);
        this.reporter = Objects.requireNonNull(reporter);
        this.loader.push(Objects.requireNonNull(loader));
        this.options = Objects.requireNonNull(options);
    }

    public void interpret() throws IOException {
        try {
            Program program = createProgram(Paths.get(options.getFilename()));

            if (options.printAst()) {
                printAst(options, program);
            } else {
                runInterpreter(program);
            }
        } catch (ProgramError e) {
            // intentionally empty, the 'reporter' will contain any errors
            // which are handled elsewhere
        }
    }

    private void assertErrorFree() {
        if (reporter.hasErrors()) {
            throw new ProgramError();
        }
    }

    /**
     * If a file gets imported twice, assume that we have a circular dependency
     */
    private void assertNoCircularDependencies(String filename) {
        if (fileLoadRequests.containsKey(filename)) {
            reporter.error(String.format("Circular reference detected when importing '%s'.", filename));
            assertErrorFree();
        }

        fileLoadRequests.put(filename, true);
    }

    private Program createProgram(Path file) throws IOException {

        String filename = file.getFileName().toString();

        String source = readFile(file.toAbsolutePath().toString());

        reporter.setCurrentFile(filename);

        List<Token> tokens = lex(source);

        Program program = parse(tokens, file.getFileName().toString().replace(".twl", ""));

        List<FileImport> fileImports = program.getFileImports();
        List<Import> imports = program.getImports();
        Node[] allImports = new Node[fileImports.size() + imports.size()];

        System.arraycopy(imports.toArray(new Node[0]), 0, allImports, 0, imports.size());
        System.arraycopy(fileImports.toArray(new Node[0]), 0, allImports, imports.size(), fileImports.size());

        /* @todo make the imports less of a mess

            Consider parsing them all to a normalized form, rather than 'Import' and 'FileImport', which can handle:

            IMPORT (target, target, target) FROM ( <namespace> | "file" ) ( as name, name, name )

            When the AST gets placed in here, don't make it a Program node, use a new node type that can
            hold metadata about the imported AST. That way the interpreter gets a new visit method it can use to make sense
            of what's going on, maybe?

            The interpreter can use this to determine what to import from the resulting environment

            It would also be nice to allow standard library namespaces to be defined as a mix of
            Java and 'twl' files
         */

        for (Node importNode : allImports) {
            int replacementLocation = program.getNodes().indexOf(importNode);
            Program subProgram = null;

            if (importNode instanceof FileImport) {

                // FileImport is an import from a user-supplied source file,
                // this will look in the same folder as the current source file

                FileImport fileImport = (FileImport) importNode;
                assertNoCircularDependencies(fileImport.getFile());

                subProgram = createProgram(Paths.get(getPathToFile(file), fileImport.getFile()));
            } else if (importNode instanceof Import) {

                // Internal import, so this could be either a pure Java import, or importing a file
                // contained in the 'resources/standard-lib' directory. Java imports are handled
                // purely in the interpreter
                //
                // This part checks if it's a file in the resources directory, if it is
                // it parses it and replaces the Import node with the parsed Program node

                Import _import = (Import) importNode;
                Path pathToFile = Paths.get(STD_LIB_PATH, _import.getNamespace() + ".twl");
                File libraryFile = new File(pathToFile.toString());
                if (libraryFile.exists() && !libraryFile.isDirectory()) {
                    subProgram = createProgram(pathToFile);
                }
            }

            // Replace the 'import' with the AST of the imported file
            // This causes some issues, such as losing all context of the import, meaning
            // once clauses are parsed into the FileImport node, they'll be lost here
            // This needs correcting, as it currently makes file imports poor
            if (subProgram != null) {
                subProgram.setTopLevel(false);
                program.getNodes().set(replacementLocation, subProgram);
            }
        }

        analyze(program);

        return program;
    }

    private String getPathToFile(Path fileName) {
        return fileName.getParent().toAbsolutePath().toString();
    }

    private List<Token> lex(String source) {
        Lexer lexer = new Lexer(source, reporter);
        List<Token> tokens = lexer.tokenize();
        assertErrorFree();
        return tokens;
    }

    private Program parse(List<Token> tokens, String namespace) {
        Parser parser = new Parser(tokens, reporter, namespace);
        Program prog = parser.parse();
        assertErrorFree();
        return prog;
    }

    private void analyze(Program program) {
        new StaticAnalyser(program, reporter).performAnalysis();
        assertErrorFree();
    }

    private void runInterpreter(Program program) {
        Interpreter interpreter = new Interpreter(program, loader, reporter);
        interpreter.interpret();
        assertErrorFree();
    }

    private void printAst(Options options, Program program) {
        AstPrinter printer = new AstPrinter(options.getTabChar());
        outputStream.print(printer.print(program));
    }

    private String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, Charset.forName("utf-8"));
    }
}
