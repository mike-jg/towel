package towel.interpreter;

import java.util.Scanner;

/**
 * If a standard library function needs a Scanner, it can implement this
 * and will be passed one
 */
public interface RequiresScanner {
    void setScanner(Scanner scanner);
}
