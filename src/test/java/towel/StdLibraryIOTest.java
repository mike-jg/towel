package towel;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;

import static towel.Assertions.*;

public class StdLibraryIOTest {


    @Test
    public void testPrint() {
        assertExecutesWithOutput("test", "import print from <io> \"test\" print");
    }

    @Test
    public void testPrintNumber() {
        assertExecutesWithOutput("5.0", "import print from <io> 5 print");
    }

    @Test
    public void testPrintBoolean() {
        assertExecutesWithOutput("false", "import print from <io> false print");
    }

    @Test
    public void testPrintln() {
        assertExecutesWithOutput("test\n", "import println from <io> \"test\" println");
    }

    @Test
    public void testInput() throws UnsupportedEncodingException {
        String input = "Fred\n50\n";
        setScannerInput(input);

        assertExecutesWithOutputIgnoreNotices("What's your name?\n" +
                "Hi Fred, how old are you?\n" +
                "Fred, you are 50.0 years old!\n",
                "\n" +
                "import <io>\n" +
                "import sformat from <strings>\n" +
                "\n" +
                "\"What's your name?\\n\" io.input_str let name\n" +
                "name \"Hi {0}, how old are you?\\n\" sformat io.input_num let age\n" +
                "\n" +
                "age name \"{0}, you are {1} years old!\" sformat io.println\n");
    }

}
