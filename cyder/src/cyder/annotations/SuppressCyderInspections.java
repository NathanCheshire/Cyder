package cyder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to mark methods throughout Cyder to
 * ignore when performing certain tests/checks.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.METHOD,
        ElementType.CONSTRUCTOR,
        ElementType.FIELD,
        ElementType.TYPE
})
public @interface SuppressCyderInspections {
    String[] values();
}
