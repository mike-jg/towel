package towel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static towel.Assertions.*;

public class FunctionTest {


    @Test
    public void testThrowsForNotMeetingPreConditions() {
        assertExecutesWithError(
                "Stack does not meet type pre-conditions for repeat.\n" +
                        "Length must be at least 2, current length is 2.\n" +
                        "Item 0 from the top of the stack must be of type 'seq', 'num' was found.\n" +
                        "Item 1 from the top of the stack must be of type 'num', 'num' was found.",
                "import * from <sequences>" +
                        " 5 5 repeat "
        );
    }

    @Test
    public void testThrowsForNotMeetingPreConditionsPrint() {
        assertExecutesWithError(
                "Stack does not meet length pre-conditions for print.\n" +
                        "Length must be at least 1, current length is 0.\n" +
                        "Item 0 from the top of the stack must be of type 'any', nothing was found.",
                "import * from <io>" +
                        " print "
        );
    }

    @Test
    public void testConditionalCall() {

        String code = "def a_func { \"a_func called\" } " +
                "def another_func { \"another_func called\" }" +
                "false { a_func } { another_func }" +
                "?";

        assertExecutesWithResult(
                "another_func called",
                code
        );
    }

    @Test
    public void testCanCallFunction() {
        assertExecutesWithResult(5d, " def test { 5 } test");
    }

    @ParameterizedTest
    @MethodSource("reverseProvider")
    public void testReverse(String expect, String code) {
        assertExecutesWithOutputIgnoreNotices(expect, code);
    }

    public static Stream<Arguments> reverseProvider() {
        return Stream.of(
                Arguments.of("level", "import * from <strings> import * from <io> \"level\" reverse print"),
                Arguments.of("nevar", "import * from <strings> import * from <io> \"raven\" reverse print"),
                Arguments.of("loob", "import * from <strings> import * from <io> \"bool\" reverse print")
        );
    }

    @Test
    public void testDup2() {
        assertExecutesWithOutputIgnoreNotices("5.0\n1.0\n5.0\n1.0",
                "import * from <stack> import * from <io> " +
                        " 1 5 dup2 println println println print ");
    }

    @Test
    public void testDup() {
        assertExecutesWithOutputIgnoreNotices("5.0\n5.0",
                "import * from <stack> import * from <io> " +
                        " 5 dup println print ");
    }

    @Test
    public void testRotate() {
        assertExecutesWithOutputIgnoreNotices("1.0\n3.0\n2.0",
                "import * from <stack> import * from <io> " +
                        " 1 2 3 rotate println println print ");
    }

    @Test
    public void testRepeat() {
        assertExecutesWithOutput("hi hi hi hi hi ", "import repeat from <sequences> " +
                "import print from <io> " +
                "5 { \"hi \" print } repeat");
    }

    @Test
    public void testCurryZeroLengthSequence() {
        assertExecutesWithOutputIgnoreNotices("test",
                "import * from <sequences> import * from <strings> " +
                        "import print from <io> " +
                        " \"test\" { } curry exec print ");
    }

    @Test
    public void testCurrySequences() {
        assertExecutesWithOutputIgnoreNotices("etam aey",
                "import * from <sequences> import * from <strings> " +
                        "import print from <io> " +
                        " \"yea mate\"  { reverse } curry { exec } curry exec print  ");
    }

    @Test
    public void testCurryArray() {
        assertExecutesWithOutputIgnoreNotices("25.010.05.0",
                "import * from <sequences> import * from <strings> import <arrays> " +
                        "import print from <io> " +
                        " [ 1,2,5 ] { { 5* } arrays.map } curry exec arrays.pop print arrays.pop print arrays.pop print  ");
    }

    @Test
    public void testCurryBoolean() {
        assertExecutesWithOutputIgnoreNotices("false",
                "import * from <sequences> " +
                        "import print from <io> " +
                        " true { true != } curry exec print ");
    }

    @Test
    public void testExecString() {
        assertExecutesWithOutputIgnoreNotices("tset",
                "import * from <sequences> import * from <strings> " +
                        "import print from <io> " +
                        " \"test\" { reverse print } exec ");
    }

    @Test
    public void testExecBoolean() {
        assertExecutesWithOutputIgnoreNotices("1.0",
                "import * from <sequences> " +
                        "import print from <io> " +
                        " true { { 1 print } { 0 print } ? } exec ");
    }

    @Test
    public void testExecNumber() {
        assertExecutesWithOutputIgnoreNotices("equal",
                "import * from <sequences> " +
                        "import print from <io> " +
                        " 15 { 15 == { \"equal\" print } { \"not equal\" print } ? } exec ");
    }

    @Test
    public void testLibraryFuncAndUserFunc() {
        assertExecutesWithResult(true, "import dup from <stack> import reverse from <strings> def palindrome { dup reverse == } \"level\" palindrome");
    }

    @Test
    public void testLibraryFuncAndUserFunc2() {
        assertExecutesWithResult(false, "import dup from <stack> import reverse from <strings> def palindrome { dup reverse == } \"nope\" palindrome");
    }

}
