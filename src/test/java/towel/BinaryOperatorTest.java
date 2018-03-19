package towel;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static towel.Assertions.*;


public class BinaryOperatorTest {

    @ParameterizedTest
    @MethodSource("binaryProvider")
    public void testEvaluatesBinary(double expect, String code) {
        assertExecutesWithResult(expect, code);
    }

    public static Stream<Arguments> binaryProvider() {
        return Stream.of(
                Arguments.of(56d, "2 3 + 11 * 1 +"),
                Arguments.of(9d, "15 6 -"),
                Arguments.of(1d, "15 2 %"),
                Arguments.of(9d, "15\n6\n\t \r\n-"),
                Arguments.of(-1d, "2 1 12 3 / - +"),
                Arguments.of(18d, "2 3 11 + 5 -*"),
                Arguments.of(-10d, "-5 -5 +")
        );
    }

    @ParameterizedTest
    @MethodSource("divisionByZeroProvider")
    public void testDivisionByZero(String expect, String code) {
        assertExecutesWithError(expect, code);
    }

    public static Stream<Arguments> divisionByZeroProvider() {
        return Stream.of(
                Arguments.of("Division by zero.", "5 0 /"),
                Arguments.of("Division by zero.", "17 0 %"),
                Arguments.of("Division by zero.", "12 0 /"),
                Arguments.of("Division by zero.", "17.5 0 %")
        );
    }

    @ParameterizedTest
    @MethodSource("boolComparisonProvider")
    public void testBoolComparisons(boolean expect, String code) {
        assertExecutesWithResult(expect, code);
    }

    public static Stream<Arguments> boolComparisonProvider() {
        return Stream.of(
                Arguments.of(true, "true true =="),
                Arguments.of(true, "false false =="),
                Arguments.of(false, "true true !="),
                Arguments.of(false, "false false !="),
                Arguments.of(true, "false true !="),
                Arguments.of(false, "false true =="),
                Arguments.of(true, "false true ||"),
                Arguments.of(true, "true true ||"),
                Arguments.of(false, "false false ||")
        );
    }

    @ParameterizedTest
    @MethodSource("numberComparisonProvider")
    public void testNumberComparisons(boolean expect, String code) {
        assertExecutesWithResult(expect, code);
    }

    public static Stream<Arguments> numberComparisonProvider() {
        return Stream.of(
                Arguments.of(true, "5 1 >"),
                Arguments.of(false, "5 1 <"),
                Arguments.of(false, "5 1 =="),
                Arguments.of(true, "5 1 !="),
                Arguments.of(true, "7 9 <="),
                Arguments.of(true, "7 7 <="),
                Arguments.of(true, "900 45 >="),
                Arguments.of(true, "45 45 >="),
                Arguments.of(true, "55 55 ==")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidComparisonProvider")
    public void testInvalidComparisons(String error, String code) {
        assertExecutesWithError(error, code);
    }

    public static Stream<Arguments> invalidComparisonProvider() {
        return Stream.of(
                Arguments.of("Strings can only be checked with == and !=.", "\"1\" \"string\" >"),
                Arguments.of("Strings can only be checked with == and !=.", "\"1\" \"string\" >="),
                Arguments.of("Strings can only be checked with == and !=.", "\"1\" \"string\" <="),
                Arguments.of("Strings can only be checked with == and !=.", "\"1\" \"string\" <"),
                Arguments.of("This comparison cannot be done between 'num' and 'str'.", "1 \"string\" !="),
                Arguments.of("This comparison cannot be done between 'num' and 'str'.", "1 \"string\" =="),
                Arguments.of("This comparison cannot be done between 'num' and 'bool'.", "1 false =="),
                Arguments.of("This comparison cannot be done between 'num' and 'bool'.", "1 true =="),
                Arguments.of("This comparison cannot be done between 'str' and 'bool'.", "\"test\" false  =="),
                Arguments.of("Booleans can only be checked with '==', '!=', '||' and '&&'.", "false true >="),
                Arguments.of("Booleans can only be checked with '==', '!=', '||' and '&&'.", "false true <="),
                Arguments.of("Booleans can only be checked with '==', '!=', '||' and '&&'.", "false true >"),
                Arguments.of("Booleans can only be checked with '==', '!=', '||' and '&&'.", "false true <"),
                Arguments.of("Two arguments are required for comparisons.", ">"),
                Arguments.of("Two arguments are required for comparisons.", ">="),
                Arguments.of("Two arguments are required for comparisons.", "!="),
                Arguments.of("Two arguments are required for comparisons.", "=="),
                Arguments.of("Two arguments are required for comparisons.", "<"),
                Arguments.of("Two arguments are required for comparisons.", "<="),
                Arguments.of("Two arguments are required for comparisons.", "1 >"),
                Arguments.of("Two arguments are required for comparisons.", "false >="),
                Arguments.of("Two arguments are required for comparisons.", "\"test\" !="),
                Arguments.of("Two arguments are required for comparisons.", "7 =="),
                Arguments.of("Two arguments are required for comparisons.", "100 <"),
                Arguments.of("Two arguments are required for comparisons.", "true <=")
        );
    }

    @ParameterizedTest
    @MethodSource("stringComparisonProvider")
    public void testCanCompareStrings(boolean outcome, String code) {
        assertExecutesWithResult(outcome, code);
    }

    public static Stream<Arguments> stringComparisonProvider() {
        return Stream.of(
                Arguments.of(true, "\"my string\" \"my string\" =="),
                Arguments.of(false, "\"my string\" \"not my string\" =="),
                Arguments.of(false, "\"my string\" \"my string\" !="),
                Arguments.of(true, "\"my string\" \"not my string\" !=")
        );
    }

    @ParameterizedTest
    @MethodSource("badBinaryProvider")
    public void testBinaryFailsWithBadArgs(String code) {
        assertExecutesWithAnyOneError(code);
    }

    public static Stream<Arguments> badBinaryProvider() {
        return Stream.of(
                Arguments.of("+"),
                Arguments.of("-"),
                Arguments.of("*"),
                Arguments.of("/"),
                Arguments.of("1 +"),
                Arguments.of("2 -"),
                Arguments.of("4 *"),
                Arguments.of("1 /"),
                Arguments.of("1 \"wrong arg\" +"),
                Arguments.of("2 \"wrong arg\" -"),
                Arguments.of("4 \"wrong arg\" *"),
                Arguments.of("1 \"wrong arg\" /")
        );
    }

    @ParameterizedTest
    @MethodSource("cannotAddStringsProvider")
    public void testCannotAddStrings(String code) {
        assertExecutesWithAnyOneError(code);
    }

    public static Stream<Arguments> cannotAddStringsProvider() {
        return Stream.of(
                Arguments.of("\"my string\" 5 +"),
                Arguments.of("\"my string\" \"f\" +"),
                Arguments.of("\"1\" 5 +"),
                Arguments.of("5 6 + \"test\" *"),
                Arguments.of("5 6 + \"test\" /"),
                Arguments.of("5 6 + \"test\" -")
        );
    }

}
