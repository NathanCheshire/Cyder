package cyder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to mark features of Cyder as being Vanilla (not a 3rd party developer).
 */
@Retention(RetentionPolicy.RUNTIME) /* allow to be found after compilation to bytecode */
@Target(ElementType.TYPE) /* restrict annotations to classes */
public @interface Vanilla {
}
