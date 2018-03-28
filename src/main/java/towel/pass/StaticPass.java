package towel.pass;

import towel.ErrorReporter;
import towel.ast.Program;

/**
 * Represents a pass over the AST
 */
public interface StaticPass {

    void performAnalysis(Program program);

    static StaticPass getDefaultPass(ErrorReporter reporter) {
        StaticPassStack stack = new StaticPassStack();
        stack.addPass(new ImportResolver(reporter));
        stack.addPass(new ScopeResolver(reporter));
        return stack;
    }

}
