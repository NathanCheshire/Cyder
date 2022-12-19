package main.java.cyder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to mark Cyder tests. Specifically, how they are triggered.
 * If the value/trigger is left blank, then the trigger is "test" and the annotated
 * method will be invoked when the Console is initially loaded.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CyderTest {
    String value() default "test";
}