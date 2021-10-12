
package cyder.annotations;

import java.lang.annotation.*;

//no target so we may place this above methods, variables, etc.
// to be used to determine what we should test for showcase videos/documentation
@Retention(RetentionPolicy.CLASS)
public @interface Showcase {
    String value();
}