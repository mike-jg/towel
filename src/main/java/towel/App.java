package towel;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Hello world!
 */
class App {

    private static PrintStream outputStream = System.out;
    private static Scanner scanner = new Scanner(System.in);
    private static LoggingErrorReporter reporter = new LoggingErrorReporter();
    private static StdLibraryMapGenerator stdLibraryMapGenerator = new StdLibraryMapGenerator();

    public static void setPrintStream(PrintStream stream) {
        outputStream = stream;
    }

    public static void reset() {
        scanner = new Scanner(System.in);
        reporter = new LoggingErrorReporter();
    }

    public static void setStdLibraryMapGenerator(StdLibraryMapGenerator stdLibraryMapGenerator) {
        App.stdLibraryMapGenerator = stdLibraryMapGenerator;
    }

    /**
     * Example usage:
     *
     * $ java -jar ./target/towel-0.1.jar ./example/mytest.twl --print-ast "--tab-char=|   "
     */
    public static void main(String[] args) {
        Options options = new Options(args, outputStream);
        options.parse();

        try {
            if (options.generateStdLibraryMap()) {
                stdLibraryMapGenerator.generate();
                return;
            }

            go(options);

            if (reporter.hasErrors()) {
                outputStream.print("\n");
                outputStream.print("An error occurred:\n");
                printLogEntries(reporter.getErrors());
            }
            if (!options.suppressNotices() && reporter.hasNotices()) {
                outputStream.print("\n");
                outputStream.print("Notices:\n");
                printLogEntries(reporter.getNotices());
            }

        } catch (IOException e) {
            outputStream.print("Error reading input file: " + options.getFilename() + ". " + e.getMessage() + "\n");
            e.printStackTrace(outputStream);
        }
    }

    private static void printLogEntries(Map<String, List<LoggingErrorReporter.LogEntry>> entries) {

        for (Map.Entry<String, List<LoggingErrorReporter.LogEntry>> errorsInFile : entries.entrySet()) {

            if (errorsInFile.getValue().isEmpty()) {
                continue;
            }

            outputStream.print(errorsInFile.getKey() + "\n");

            for (LoggingErrorReporter.LogEntry logEntry : errorsInFile.getValue()) {
                outputStream.print(logEntry.toString() + "\n");
            }
        }

    }

    private static void go(Options options) throws IOException {
        if (!options.valid()) {
            options.printUsage();
            return;
        }

        SourceFileInterpreter sfi = new SourceFileInterpreter(
                outputStream, scanner, reporter, options
        );

        sfi.interpret();
    }

}
