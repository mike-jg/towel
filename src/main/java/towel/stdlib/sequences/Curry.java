package towel.stdlib.sequences;

import towel.LibraryMetadata;
import towel.ast.*;
import towel.interpreter.*;

/**
 * Curry moves something onto the beginning of a sequence
 */
@LibraryMetadata(
        name = "curry",
        namespace = "sequences"
)
public class Curry implements TowelFunction {
    @Override
    public StackCondition.PreCondition getPreCondition() {
        return StackCondition.preConditionFor(Sequence.class, Object.class);
    }

    @Override
    public StackCondition.PostCondition getPostCondition() {
        return StackCondition.postConditionFor();
    }

    @Override
    public void call(Interpreter interpreter) {
        Sequence seq = interpreter.getStack().popSequence();
        Object anything = interpreter.getStack().pop();

        Node[] nodes = new Node[seq.getNodes().length + 1];

        // currying an array requires making a new array initializer based on the contents of the array
        // then when the sequence is eventually executed, it'll evaluate the initializer, creating the array
        if (anything instanceof TowelArray) {

            TowelArray array = (TowelArray) anything;

            Object[] initializer = new Object[array.size()];

            for (int i = 0; i < initializer.length; i++) {
                initializer[i] = array.get(i);
            }
            nodes[0] = new Array(Token.create(Token.TokenType.ARRAY, "", ""), initializer);

        } else if (anything instanceof Sequence) {
            nodes[0] = (Sequence) anything;
        } else {

            Token.TokenType type = null;

            if (anything instanceof String) {
                type = Token.TokenType.STRING_LITERAL;
            } else if (anything instanceof Double) {
                type = Token.TokenType.NUMBER_LITERAL;
            } else if (anything instanceof Boolean) {
                type = Token.TokenType.BOOLEAN_LITERAL;
            } else {
                throw new IllegalArgumentException(String.format("Cannot curry %s.", TypeNameTranslator.get(anything.getClass().getSimpleName())));
            }

            nodes[0] = new Literal(Token.create(type, anything.toString(), anything));
        }

        System.arraycopy(seq.getNodes(), 0, nodes, 1, seq.getNodes().length);

        Sequence newSequence = new Sequence(seq.getToken(), nodes);
        interpreter.getStack().push(newSequence);
    }
}
