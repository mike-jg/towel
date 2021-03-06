package towel;

import towel.ast.Import;
import towel.ast.Program;
import towel.ast.Token;
import towel.interpreter.ImportNodeResolver;
import towel.interpreter.Interpreter;
import towel.interpreter.NamespaceLoader;
import towel.interpreter.NativeNamespaceLoader;
import towel.parser.Lexer;
import towel.parser.Parser;
import towel.pass.StaticPass;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Run the full 'pipeline' on a single source file
 *
 * This will recursively interpret all external imports too
 *
 * As this was quickly thrown together to allow the recursive parser of imports,
 * it needs revisiting and tidying up
 *
 * @todo rethink how this works in general
 */
public class SourceFileInterpreter {

    private PrintStream outputStream;
    private ContextualErrorReporter reporter;
    private NamespaceLoader loader;
    private Options options;
    private DependencyGraph dependencyGraph;
    private final static String STD_LIB_PATH = Paths.get("src/main/resources/standard-lib/").toAbsolutePath().toString();
    private Map<SourceFile, Program> parsedFiles = new HashMap<>();

    private class ProgramError extends RuntimeException {
        ProgramError() {
        }
    }

    SourceFileInterpreter(PrintStream outputStream, Scanner scanner, ContextualErrorReporter reporter, Options options) {
        this.outputStream = Objects.requireNonNull(outputStream);
        this.reporter = Objects.requireNonNull(reporter);
        this.loader = new NativeNamespaceLoader(this.outputStream, Objects.requireNonNull(scanner));
        this.options = Objects.requireNonNull(options);
    }

    public void interpret() throws IOException {
        try {
            SourceFile file = new SourceFile(Paths.get(options.getFilename()));

            dependencyGraph = new DependencyGraph(file.getName());
            final Program program = createProgram(file);

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

    private Program createProgram(SourceFile sourceFile) throws IOException {

        if (parsedFiles.containsKey(sourceFile)) {
            return parsedFiles.get(sourceFile);
        }

        String filename = sourceFile.getName();
        String source = sourceFile.readAllContents();
        reporter.setContext(filename);

        Program program = runAstPipeline(source, sourceFile.getNamespace());

        List<Import> imports = program.getImports();

        for (Import importNode : imports) {
            int replacementLocation = program.getNodes().indexOf(importNode);

            Program subProgram = parseImportIntoSubProgram(importNode, sourceFile);

            // Add the parsed imported code just before the Import
            // This allows the Interpreter to go through the AST in order, and
            // it'll naturally get to the AST for the import before the import itself

            // It can then package the parsed program node into a 'NamespaceLoader', which will handle
            // importing the names based on the import
            if (subProgram != null) {
                subProgram.notRootNode();
                program.getNodes().add(replacementLocation, subProgram);
            }
        }

        reporter.setContext(filename);

        parsedFiles.put(sourceFile, program);

        return program;
    }

    private Program parseImportIntoSubProgram(Import importNode, SourceFile rootFile) throws IOException {

        Program subProgram = null;

        ImportNodeResolver adapter = ImportNodeResolver.wrap(importNode);

        if (adapter.isExternal()) {

            // External file import
            // Looks in the current directory for a file with a matching name

            reporter.setContext(adapter.getNamespace());

            assertNoCircularDependencies(rootFile.getName(), adapter.getNamespace());

            // Create a new source file for the file name
            // Always look relative to the current directory
            SourceFile imported = new SourceFile(Paths.get(rootFile.getParentDirectory().toString(), adapter.getFileName()));

            subProgram = createProgram(imported);
        } else {
            // Internal import, so this could be either a pure Java import, or importing a file
            // contained in the 'resources/standard-lib' directory. Java imports are handled
            // purely in the interpreter
            //
            // This part checks if it's a file in the resources directory, if it is
            // it parses it into a Program node
            //
            // This is for 'internal' libraries which are implemented as .twl files

            Path pathToFile = Paths.get(STD_LIB_PATH, adapter.getFileName());
            reporter.setContext("Internal source file: " + adapter.getFileName());
            File libraryFile = new File(pathToFile.toString());
            if (libraryFile.exists() && !libraryFile.isDirectory()) {
                subProgram = createProgram(new SourceFile(pathToFile));
                subProgram.setProgramType(Program.ProgramType.INTERNAL);
            }
        }

        return subProgram;
    }

    private void assertErrorFree() {
        if (reporter.hasErrors()) {
            throw new ProgramError();
        }
    }

    /**
     * Check for a circular dependency
     *
     * @param filename   the file doing the import
     * @param importName the file being imported
     */
    private void assertNoCircularDependencies(String filename, String importName) {
        dependencyGraph.addDependency(filename, importName);

        if (dependencyGraph.hasCircularDependency()) {
            reporter.error(String.format("Circular reference detected when importing '%s' from '%s'.", importName, filename));
            assertErrorFree();
        }
    }

    private Program runAstPipeline(String source, String namespace) {
        List<Token> tokens = lex(source);
        Program program = parse(tokens, namespace);
        analyze(program);

        return program;
    }

    private List<Token> lex(String source) {
        Lexer lexer = Lexer.getFor(source, reporter);
        List<Token> tokens = lexer.tokenize();
        assertErrorFree();
        return tokens;
    }

    private void analyze(Program program) {
        StaticPass.getDefaultPass(reporter).performAnalysis(program);
        assertErrorFree();
    }

    private Program parse(List<Token> tokens, String namespace) {
        Parser parser = Parser.getFor(tokens, reporter, namespace);
        Program prog = parser.parse();
        assertErrorFree();
        return prog;
    }

    private void runInterpreter(Program program) {
        Interpreter interpreter = Interpreter.getFor(program, loader, reporter);
        interpreter.interpret();
        assertErrorFree();
    }

    private void printAst(Options options, Program program) {
        AstPrinter printer = new AstPrinter(options.getTabChar());
        outputStream.print(printer.print(program));
    }
}
