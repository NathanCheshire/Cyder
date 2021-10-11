
package cyder.annotations;

import java.lang.annotation.*;

//no target so we may place anywhere
@Retention(RetentionPolicy.CLASS)
public @interface Showcase {
    String help() default "Showcase me";
}