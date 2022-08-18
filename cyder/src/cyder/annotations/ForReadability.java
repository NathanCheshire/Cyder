package cyder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to tag methods as being called typically only once or in rare cases.
 * These methods will most likely be optimized away by the JVM at runtime but for human-readability,
 * we extract the logic to a named method for readability purposes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ForReadability {}
