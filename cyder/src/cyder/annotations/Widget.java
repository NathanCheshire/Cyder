package cyder.annotations;

/**
 * An annotation used to mark Cyder widgets, how they are triggered, and a breif explenation of them
 */
public @interface Widget {
    String trigger();
    String description();
}
