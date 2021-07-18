
package cyder.annotations;

import java.lang.annotation.*;

@Target(ElementType.LOCAL_VARIABLE)
@Retention(RetentionPolicy.CLASS)
public @interface Helper {
    String help() default "DEFAULT HELPER";
}
