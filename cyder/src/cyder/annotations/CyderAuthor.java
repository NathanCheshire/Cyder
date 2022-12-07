package cyder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to credit authors of Cyder classes.
 */
@Retention(RetentionPolicy.RUNTIME) /* allow to be found after compilation to bytecode */
@Target(ElementType.TYPE) /* restrict annotations to classes */
public @interface CyderAuthor {
    String author() default "Nate Cheshire";
}
