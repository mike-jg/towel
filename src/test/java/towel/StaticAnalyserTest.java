package towel;

import org.junit.jupiter.api.Test;

import static towel.Assertions.assertAnalysisWithError;
import static towel.Assertions.assertExecutesWithError;

public class StaticAnalyserTest {


    @Test
    public void testlogsErrorForNestedDefinition() {
        assertAnalysisWithError("def test { def my_nested_test {} } ", "Cannot declare a function within another function.");
    }

    @Test
    public void testlogsErrorForNonExistentNamespace() {
        assertAnalysisWithError("def test { def my_nested_test {} } ", "Cannot declare a function within another function.");
    }

    @Test
    public void testDoesNotWorkWithoutImport() {
        assertExecutesWithError("Unknown identifier 'print'.", "5 print");
    }

}
