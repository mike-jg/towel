package towel.interpreter;

import towel.ast.Sequence;
import towel.ast.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent the stack, container for values in the running program
 * <p>
 * The {@code <T>popAsType} and {@code pop*} methods will be safe to use provided the stack has been asserted to contain
 * the correct types first
 * </p>
 */
public class Stack {

    private List<Object> stack = new ArrayList<>();

    public void push(Object item) {
        stack.add(stack.size(), item);
    }

    public Object pop() {
        return stack.remove(stack.size() - 1);
    }

    public String popString() {
        return this.<String>popAsType();
    }

    public boolean popBoolean() {
        return this.<Boolean>popAsType();
    }

    public double popDouble() {
        return this.<Double>popAsType();
    }

    public Sequence popSequence() {
        return this.<Sequence>popAsType();
    }

    public TowelArray popArray() {
        return this.<TowelArray>popAsType();
    }

    @SuppressWarnings("unchecked")
    private <T> T popAsType() {
        return (T) pop();
    }

    /**
     *
     * @return the current size of the stack
     */
    public int size() {
        return stack.size();
    }

    /**
     * Get, but don't remove whatever is on top of the stack
     *
     * @return top of the stack
     */
    public Object peek() {
        return stack.get(stack.size() - 1);
    }

    /**
     * Assert that the stack is at least the given length
     */
    public void assertStack(int length, String message, Token token) throws StackAssertionError {
        if (stack.size() < length) {
            throw new StackAssertionError(message, token);
        }
    }

    /**
     * Assert the stack is in the state described by {@code StackCondition}
     *
     */
    public void assertState(StackCondition condition, Token token) throws StackAssertionError {
        if (condition.length() == 0) {
            return;
        }

        if (stack.size() < condition.length()) {
            throw new StackAssertionError(createErrorForInvalidState(condition, "length", token), token);
        }

        assertStack( condition.length(), "Stack does not meet length preConditions.\n" +
                "Length must be at least " +  condition.length() + ", current length is " + stack.size() + ".", token);

        for (int i = 0; i <  condition.length(); i++) {
            if (! condition.condition()[i].isInstance(stack.get(stack.size() - 1 - i))) {
                throw new StackAssertionError(createErrorForInvalidState(condition, "type", token), token);
            }
        }
    }

    /**
     * Create a verbose error message for an invalid stack
     */
    private String createErrorForInvalidState(StackCondition condition, String error, Token token) {

        StringBuilder message = new StringBuilder("Stack does not meet ")
                .append(error)
                .append(" ")
                .append(condition.isPostCondition() ? "post-conditions" : "pre-conditions")
                .append(" for ")
                .append(token.getLexeme())
                .append(".\n")
                .append("Length must be at least ")
                .append(condition.length())
                .append(", current length is ")
                .append(stack.size())
                .append(".");

        if (condition.length() > 0) {
            message.append("\n");
        }

        for (int i = 0; i < condition.length(); i++) {
            message.append("Item ")
                    .append(i)
                    .append(" from the top of the stack must be of type '")
                    .append(TypeNameTranslator.get(condition.condition()[i]));

            if (i < stack.size()) {
                message.append("', '")
                        .append(TypeNameTranslator.get(stack.get(stack.size() - 1 - i).getClass()))
                        .append("' was found.");
            } else {
                message.append("', nothing was found.");
            }
            if (i + 1 < condition.length()) {
                message.append("\n");
            }
        }

        return message.toString();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Stack{\n--Top--\n");

        for (int i = stack.size() - 1; i >= 0; i--) {
            stringBuilder.append("\t");
            stringBuilder.append(stack.get(i).toString());
            stringBuilder.append("\n");
        }

        return stringBuilder.append("-- Bottom --\n}").toString();
    }
}
