package towel.interpreter;

import towel.ErrorReporter;
import towel.ast.Node;
import towel.ast.Program;

public interface Interpreter {

    /**
     * Execute the program, beginning at the root Program node
     */
    Object interpret();

    /**
     * Interpret the given nodes within the context of the running program
     */
    Object interpret(Node[] node);

    /**
     * Get the program stack
     *
     * @return the stack
     */
    Stack getStack();

    static Interpreter getFor(Program program, NamespaceLoader loader, ErrorReporter reporter) {
        return getFor(program, loader, reporter, new Namespace());
    }

    static Interpreter getFor(Program program, NamespaceLoader loader, ErrorReporter reporter, Namespace namespace) {
        return new ProgramInterpreter(program, loader, reporter, namespace);
    }
}
