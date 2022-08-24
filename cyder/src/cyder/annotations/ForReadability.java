package cyder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to tag methods which are used for construction, rarely called, 
 * or extracted from a separate scope to enhance readability.
 
 * Most methods tagged with this annotation will be optimized away by the JVM at runtime but
 * for human-readability, we extract the logic to a named method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE}) /* methods and type for enums */
public @interface ForReadability {}
