package towel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This is used by the Java classes that provide functionality to the towel standard library
 * <p>
 * Determines the run-time namespace and name of the implementing class
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface LibraryMetadata {
    String namespace();

    String name();
}
