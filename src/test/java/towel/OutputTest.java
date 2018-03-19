package towel;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class OutputTest {

    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final PrintStream outContent = new PrintStream(byteArrayOutputStream);

    @Test
    public void testOptions() {
        Options o = new Options(new String[]{
                "/some/file.twl",
                "--print-ast",
                "--tab-char=test"
        }, outContent);

        o.parse();

        assertEquals(true, o.printAst());
        assertEquals(true, o.valid());
        assertEquals("test", o.getTabChar());
        assertEquals("/some/file.twl", o.getFilename());
    }

    @Test
    public void testOptions2() {
        Options o = new Options(new String[]{
                "/some/other/f.smo"
        }, outContent);

        o.parse();

        assertEquals(false, o.printAst());
        assertEquals(true, o.valid());
        assertEquals(o.getTabChar(), "\t");
        assertEquals(o.getFilename(), "/some/other/f.smo");
    }

    @Test
    public void testOptions3() {
        Options o = new Options(new String[]{
                "--tab-char=test"
        }, outContent);

        o.parse();

        assertEquals(false, o.printAst());
        assertEquals(false, o.valid());
        assertEquals(o.getTabChar(), "test");
        assertNull(o.getFilename());
    }

    @Test
    public void testOptionsThrowsExceptionIfNotParsed() {
        Options o = new Options(new String[]{
                "--tab-char=test"
        }, outContent);

        assertThrows(RuntimeException.class, o::printAst);
        assertThrows(RuntimeException.class, o::valid);
        assertThrows(RuntimeException.class, o::getTabChar);
        assertThrows(RuntimeException.class, o::getFilename);
    }

    @Test
    public void testPrintsToCorrectOutputStream() {
        Options o = new Options(new String[]{}, outContent);
        o.printUsage();
        assertTrue(byteArrayOutputStream.toString().startsWith("Usage: towel"));
    }

}
