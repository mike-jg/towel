package towel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NamespaceTest {

    Namespace env;

    @BeforeEach
    public void setUp(){
        env = new Namespace();
    }

    @Test
    public void testDefineAndGet() {
        env.define("blah", 100);
        assertEquals(100, env.get("blah"));
    }

    @Test
    public void testIsDefined() {
        env.define("blah", 100);
        assertEquals(true, env.isDefined("blah"));
    }

    @Test
    public void testIsDefined2() {
        assertEquals(false, env.isDefined("blah"));
    }

    @Test
    public void testGetReturnsNullForMissingObject() {
        assertEquals(null, env.get("blah"));
    }

    @Test
    public void testClear() {
        env.define("test", 5);
        env.define("blah", 100);

        assertEquals(true, env.isDefined("test"));
        assertEquals(true, env.isDefined("blah"));

        env.clear();

        assertEquals(false, env.isDefined("test"));
        assertEquals(false, env.isDefined("blah"));
    }
}
