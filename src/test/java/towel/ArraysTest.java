package towel;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static towel.Assertions.*;

public class ArraysTest {

    @ParameterizedTest
    @MethodSource("arrayProvider")
    public void testCreatesArray(Object expect, String code) {
        code = "import <arrays> import <stack> import <debug> import print from <io> " + code;
        assertExecutesWithOutputIgnoreNotices(expect, code);
    }

    public static Stream<Arguments> arrayProvider() {
        return Stream.of(
                Arguments.of("56.0", "2 3 + 11 * 1 + [] stack.swap arrays.push arrays.pop print"),
                Arguments.of("my test", " [\"my test\"] arrays.pop print "),
                Arguments.of("false", " [ true , false, ] arrays.pop print "),
                Arguments.of("array type='bool' values=[true, false]", " [ true , false, ] debug.print_array "),
                Arguments.of("array type='num' values=[1.0, 50.0, 123.567]", " [ 1, 50, 123.567, ] debug.print_array "),
                Arguments.of("array type='str' values=[blah, w]", " [ \"blah\", \"w\" ] debug.print_array ")
        );
    }

    @ParameterizedTest
    @MethodSource("badInitializerProvider")
    public void testBadArrayInitializer(String expect, String code) {
        assertParsesWithError(expect, code);
    }

    public static Stream<Arguments> badInitializerProvider() {
        return Stream.of(
                Arguments.of("Expecting closing ']' after array definition.", "  [,5]"),
                Arguments.of("Expecting closing ']' after array definition.", "  [,]"),
                Arguments.of("Expecting closing ']' after array definition.", "  [5"),
                Arguments.of("Expecting closing ']' after array definition.", "  ["),
                Arguments.of("Expecting closing ']' after array definition.", "  [] [\"test\"] [ true "),
                Arguments.of("Expecting closing ']' after array definition.", "  [ true false ] ")
        );
    }

    @ParameterizedTest
    @MethodSource("badContentProvider")
    public void testMixedArrayContent(String expect, String code) {
        code = "import <arrays> " + code;
        assertExecutesWithError(expect, code);
    }

    public static Stream<Arguments> badContentProvider() {
        return Stream.of(
                Arguments.of("Items in an array must all be of the same type.", "  [5, true]"),
                Arguments.of("Items in an array must all be of the same type.", "  [true, true, true, false, \"string\"]"),
                Arguments.of("Items in an array must all be of the same type.", "  [5, 10, \"blah\"]"),
                Arguments.of("Invalid type for array. Expected num, received bool.", "  [5, 10] false arrays.push "),
                Arguments.of("Invalid type for array. Expected str, received num.", "   [\"blah\"] 5 arrays.push "),
                Arguments.of("Invalid type for array. Expected bool, received str.", " [false,false,false] \"test\" arrays.push "),
                Arguments.of("Invalid type. Only 'num', 'bool', or 'str' can be added to an array.", " [] {5} arrays.push ")
        );
    }
}
