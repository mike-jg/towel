package towel;

import java.io.PrintStream;

/**
 * Quick and dirty to getLibrary command line args
 */
class Options {

    private boolean print = false;
    private String filename = null;
    private String tabChar = "\t";
    private boolean suppressNotices = false;
    private final String[] args;
    private final PrintStream outputStream;
    private boolean parsed = false;
    private boolean generateStdLibraryMap = false;

    Options(String[] args, PrintStream outputStream) {
        this.args = args;
        this.outputStream = outputStream;
    }

    public void parse() {
        parsed = true;
        int argIndex = 0;
        for (String arg : args) {
            if (arg.equals("--generate-std-map")) {
                generateStdLibraryMap = true;
                break;
            } else if (arg.equals("--print-ast")) {
                print = true;
            } else if (arg.indexOf("--tab-char=") == 0) {
                tabChar = arg.substring(11);
            } else if (arg.indexOf("--suppress-notices") == 0) {
                suppressNotices = true;
            } else if (argIndex == 0) {
                filename = arg;
            }

            argIndex++;
        }
    }

    public boolean valid() {
        assertParsed();
        return filename != null;
    }

    public boolean printAst() {
        assertParsed();
        return print;
    }

    public String getFilename() {
        assertParsed();
        return filename;
    }

    public String getTabChar() {
        assertParsed();
        return tabChar;
    }

    public boolean suppressNotices() {
        assertParsed();
        return suppressNotices;
    }

    public boolean generateStdLibraryMap() {
        assertParsed();
        return generateStdLibraryMap;
    }

    private void assertParsed() {
        if (!parsed) {
            throw new IllegalStateException("Options haven't been parsed yet. Call parse() first.");
        }
    }

    public void printUsage() {
        outputStream.println("Usage: towel [OPTION] [FILE]");
        outputStream.println("Options:");
        outputStream.println("--print-ast");
        outputStream.println("\tPrint the AST.");
        outputStream.println("--tab-char=TABCHAR");
        outputStream.println("\tFor indentation of the AST, use this character as the tab.");
        outputStream.println("\tDefaults to a tab.");
        outputStream.println("--suppress-notices");
        outputStream.println("\tDon't display notices.");
    }
}
