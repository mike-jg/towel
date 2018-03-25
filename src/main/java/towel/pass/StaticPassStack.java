package towel.pass;

import towel.parser.NodeVisitor;
import towel.parser.Program;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class StaticPassStack implements StaticPass {

    private final List<NodeVisitor<Void>> passes = new ArrayList<>();
    private final Program program;

    StaticPassStack(Program program) {
        this.program = Objects.requireNonNull(program);
    }

    void addPass(NodeVisitor<Void> pass) {
        passes.add(pass);
    }

    @Override
    public void performAnalysis() {
        passes.forEach(program::accept);
    }
}
