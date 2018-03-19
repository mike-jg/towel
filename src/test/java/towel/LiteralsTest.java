package towel;

import org.junit.jupiter.api.Test;

import static towel.Assertions.assertExecutesWithResult;

public class LiteralsTest {

    @Test
    public void testEvaluatesLiterals() {
        assertExecutesWithResult(5d, "1 2 3 4 5");
        assertExecutesWithResult("some test", "1 5 \"some test\"");
        assertExecutesWithResult(false, "true false");
    }
}
