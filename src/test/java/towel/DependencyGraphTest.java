package towel;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DependencyGraphTest {

    @Test
    public void testDoesNotFalselyDetectCircular() {
        DependencyGraph dependencyGraph = new DependencyGraph("root");
        dependencyGraph.addDependency("root", "test");
        dependencyGraph.addDependency("test", "test2");
        dependencyGraph.addDependency("test2", "test3");

        Assertions.assertEquals(false, dependencyGraph.hasCircularDependency());
    }

    @Test
    public void testDetectClosedGraph() {
        DependencyGraph dependencyGraph = new DependencyGraph("root");
        dependencyGraph.addDependency("root", "test");
        dependencyGraph.addDependency("test", "test2");
        dependencyGraph.addDependency("test2", "root");

        Assertions.assertEquals(true, dependencyGraph.hasCircularDependency());
    }

    @Test
    public void testDetectClosedGraph2() {
        DependencyGraph dependencyGraph = new DependencyGraph("root");
        dependencyGraph.addDependency("root", "test1");
        dependencyGraph.addDependency("test1", "root");
        dependencyGraph.addDependency("test1", "test2");
        dependencyGraph.addDependency("test2", "root");

        Assertions.assertEquals(true, dependencyGraph.hasCircularDependency());
    }

    @Test
    public void testDetectClosedGraph3() {
        DependencyGraph dependencyGraph = new DependencyGraph("root");
        dependencyGraph.addDependency("root", "test1");
        dependencyGraph.addDependency("test1", "test2");
        dependencyGraph.addDependency("test2", "test3");
        dependencyGraph.addDependency("test3", "test4");
        dependencyGraph.addDependency("test4", "test5");
        dependencyGraph.addDependency("test5", "test6");
        dependencyGraph.addDependency("test6", "test7");
        dependencyGraph.addDependency("test7", "root");

        Assertions.assertEquals(true, dependencyGraph.hasCircularDependency());
    }

    @Test
    public void testDetectCircularDependency() {
        DependencyGraph dependencyGraph = new DependencyGraph("root");
        dependencyGraph.addDependency("root", "test");
        dependencyGraph.addDependency("test", "test2");
        dependencyGraph.addDependency("test2", "test");

        Assertions.assertEquals(true, dependencyGraph.hasCircularDependency());
    }

    @Test
    public void testDetectCircularDependency2() {
        DependencyGraph dependencyGraph = new DependencyGraph("root");
        dependencyGraph.addDependency("root", "test1");
        dependencyGraph.addDependency("test1", "test2");
        dependencyGraph.addDependency("test2", "test3");
        dependencyGraph.addDependency("test3", "test4");
        dependencyGraph.addDependency("test4", "test5");
        dependencyGraph.addDependency("test5", "test6");
        dependencyGraph.addDependency("test6", "test7");
        dependencyGraph.addDependency("test7", "test4");
        dependencyGraph.addDependency("test7", "test8");
        dependencyGraph.addDependency("test7", "test10");
        dependencyGraph.addDependency("test8", "test9");

        Assertions.assertEquals(true, dependencyGraph.hasCircularDependency());
    }

    @Test
    public void testOkWithMultipleRefsToSameDependency() {
        DependencyGraph dependencyGraph = new DependencyGraph("root");
        dependencyGraph.addDependency("root", "test");
        dependencyGraph.addDependency("root", "test");
        dependencyGraph.addDependency("root", "test2");
        dependencyGraph.addDependency("root", "test3");
        dependencyGraph.addDependency("root", "test3");
        dependencyGraph.addDependency("root", "test4");
        dependencyGraph.addDependency("root", "test4");
        dependencyGraph.addDependency("root", "test4");

        dependencyGraph.addDependency("test4", "test2");
        dependencyGraph.addDependency("test4", "test2");
        dependencyGraph.addDependency("test4", "test2");
        dependencyGraph.addDependency("test4", "test3");
        dependencyGraph.addDependency("test4", "test3");
        dependencyGraph.addDependency("test4", "test3");

        dependencyGraph.addDependency("test3", "test5");
        dependencyGraph.addDependency("test3", "test2");

        Assertions.assertEquals(false, dependencyGraph.hasCircularDependency());
    }

}
