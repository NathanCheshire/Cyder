package cyder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** An annotation used to mark methods of Cyder which require a stable network connection. */
@Retention(RetentionPolicy.RUNTIME) /* allow to be found after compilation to bytecode */
@Target(ElementType.METHOD) /* restrict annotations to classes */
public @interface RequiresStableNetwork {
}
