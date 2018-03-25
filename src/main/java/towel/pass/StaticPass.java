package towel.pass;

import towel.ErrorReporter;
import towel.parser.Program;

public interface StaticPass {

    void performAnalysis();

    static StaticPass getPass(Program program, ErrorReporter reporter) {
        StaticPassStack stack = new StaticPassStack(program);
        stack.addPass(new Resolver(reporter));
        return stack;
    }

}
