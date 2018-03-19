package towel;

import org.junit.jupiter.api.Test;

import static towel.Assertions.assertExecutesWithOutputIgnoreNotices;
import static towel.Assertions.assertExecutesWithResult;

public class ImportTest {

    @Test
    public void testImportMultipleNames() {
        assertExecutesWithOutputIgnoreNotices("10.0", "import dup, rotate from <stack> import * from <io> 5 dup + print");
    }

    @Test
    public void testStarImport() {
        assertExecutesWithOutputIgnoreNotices("10.0", "import * from <stack> import * from <io> 5 dup + print");
    }

    @Test
    public void testBadImport() {
        assertExecutesWithOutputIgnoreNotices("10.0", "import * from <stack> import * from <io> 5 dup + print");
    }

    @Test
    public void testAliasImport() {
        assertExecutesWithResult(10d, "import dup from <stack> as test 5 test +");
    }

    @Test
    public void testImportWorks() {
        assertExecutesWithResult(10d, "import dup from <stack> 5 dup +");
    }

}
