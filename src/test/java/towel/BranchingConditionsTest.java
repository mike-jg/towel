package towel;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static towel.Assertions.assertExecutesWithResult;

public class BranchingConditionsTest {

    @ParameterizedTest
    @MethodSource("conditionsProvider")
    public void testEvaluatesBinary(Object expect, String code) {
        assertExecutesWithResult(expect, code);
    }

    public static Stream<Arguments> conditionsProvider() {
        return Stream.of(
                Arguments.of(false, " true { false } ?? "),
                Arguments.of("yea", " true { \"yea\" } ?? "),
                Arguments.of(true, " true false { \"yea\" } ?? ")
        );
    }
}
