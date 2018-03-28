package towel.parser;

import towel.ErrorReporter;
import towel.ast.Program;
import towel.ast.Token;

import java.util.List;

public interface Parser {
    Program parse();

    static Parser getFor(List<Token> tokens, ErrorReporter reporter, String namespace) {
        return new TokenParser(tokens, reporter, namespace);
    }

    static Parser getFor(List<Token> tokens, ErrorReporter reporter) {
        return new TokenParser(tokens, reporter, Program.DEFAULT_NAMESPACE);
    }
}
