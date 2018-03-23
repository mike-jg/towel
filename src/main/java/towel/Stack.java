package towel;

import towel.ast.Sequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent the stack, container for values in the running program
 *
 * The popAsType and pop* methods will be safe to use provided the stack has been asserted to contain
 * the correct types first
 */
public class Stack {

    private List<Object> stack = new ArrayList<>();

    /**
     * The type of condition to check
     */
    public static enum Conditions {
        POST, PRE
    }

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
    public <T> T popAsType() {
        return (T) pop();
    }

    public int size() {
        return stack.size();
    }

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
     * Assert that the the top N (where N is the length of expectedTypes) Objects in the stack, match the
     * types contained within expectedTypes
     *
     * i.e.
     * Parameters           Checked against
     * expectedTypes[0]     Top of the stack - 0
     * expectedTypes[1]     Top of the stack - 1
     * expectedTypes[2]     Top of the stack - 2
     * expectedTypes[3]     Top of the stack - 3
     */
    public void assertState(Class[] expectedTypes, Conditions conditions, Token token) throws StackAssertionError {
        if (expectedTypes.length == 0) {
            return;
        }

        if (stack.size() < expectedTypes.length) {
            throw new StackAssertionError(createErrorForInvalidState(expectedTypes, "length", conditions, token), token);
        }

        assertStack(expectedTypes.length, "Stack does not meet length preConditions.\n" +
                "Length must be at least " + expectedTypes.length + ", current length is " + stack.size() + ".", token);

        for (int i = 0; i < expectedTypes.length; i++) {
            if (!expectedTypes[i].isInstance(stack.get(stack.size() - 1 - i))) {
                throw new StackAssertionError(createErrorForInvalidState(expectedTypes, "type", conditions, token), token);
            }
        }
    }

    /**
     * Create a verbose error message for an invalid stack
     */
    private String createErrorForInvalidState(Class[] expectedTypes, String error, Conditions conditions, Token token) {

        StringBuilder message = new StringBuilder("Stack does not meet ")
                .append(error)
                .append(" ")
                .append(conditions == Conditions.POST ? "post-conditions" : "pre-conditions")
                .append(" for ")
                .append(token.getLexeme())
                .append(".\n")
                .append("Length must be at least ")
                .append(expectedTypes.length)
                .append(", current length is ")
                .append(stack.size())
                .append(".");

        if (expectedTypes.length > 0) {
            message.append("\n");
        }

        for (int i = 0; i < expectedTypes.length; i++) {
            message.append("Item ")
                    .append(i)
                    .append(" from the top of the stack must be of type '")
                    .append(TypeNameTranslator.get(expectedTypes[i]));

            if (i < stack.size()) {
                message.append("', '")
                        .append(TypeNameTranslator.get(stack.get(stack.size() - 1 - i).getClass()))
                        .append("' was found.");
            } else {
                message.append("', nothing was found.");
            }
            if (i + 1 < expectedTypes.length) {
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
