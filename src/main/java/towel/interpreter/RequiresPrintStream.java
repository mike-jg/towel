package towel.interpreter;

import java.io.PrintStream;

/**
 * If a standard library function needs a PrintStream, it can implement this
 * and will be passed one
 */
public interface RequiresPrintStream {
    void setPrintStream(PrintStream stream);
}
