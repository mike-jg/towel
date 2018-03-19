package towel;

import org.junit.jupiter.api.Test;

import static towel.Assertions.*;

public class LetTest {

    @Test
    public void testBasicLet() {
        assertExecutesWithOutputIgnoreNotices("5.0", "\n" +
                "import print from <io>\n" +
                "\n" +
                "def test {\n" +
                "    5 let mytest\n" +
                "\n" +
                "    10\n" +
                "    mytest print\n" +
                "}\n" +
                "\n" +
                "75 test");
    }

    @Test
    public void testLetCannotBeAccessedOutOfDefinitionScope() {
        assertExecutesWithError("Unknown identifier 'mytest2'.", "\n" +
                "import print from <io>\n" +
                "\n" +
                "def test {\n" +
                "    5 let mytest2\n" +
                "}\n" +
                "\n" +
                "test\n" +
                "\n" +
                "mytest2");
    }


    @Test
    public void testLetInSequence() {
        assertExecutesWithOutputIgnoreNotices("5.0", "\n" +
                "import print from <io>\n" +
                "import * from <sequences>\n" +
                "import * from <stack>\n" +
                "\n" +
                " def test { { 5 let myvar } 25 swap exec 100 myvar print } test");
    }

    @Test
    public void testCannotUseLetWithNoName() {
        assertParsesWithError("Expecting identifier after 'let'.", " 5 let ");
    }
}
