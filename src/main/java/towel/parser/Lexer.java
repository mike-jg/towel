package towel.parser;

import towel.ErrorReporter;
import towel.ast.Token;

import java.util.List;

/**
 * Convert source code into a list of tokens
 */
public interface Lexer {

    List<Token> tokenize();

    static Lexer getFor(String sourceCode, ErrorReporter reporter) {
        return new StringLexer(sourceCode, reporter);
    }
}
