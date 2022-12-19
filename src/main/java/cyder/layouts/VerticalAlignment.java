package main.java.cyder.layouts;

/**
 * An alignment property to determine how components are laid out
 * on a specific axis and what to do with excess space.
 */
public enum VerticalAlignment {
    /**
     * Components are aligned on the top with minimum spacing in between.
     */
    TOP,
    /**
     * Components are centered and excess space is placed in between components.
     */
    CENTER,
    /**
     * Components are aligned on the bottom with minimum spacing in between.
     */
    BOTTOM,
    /**
     * Components are centered absolutely with the excess space placed evenly
     * on the absolute top of the top most component and bottom of the bottom
     * most component.
     */
    CENTER_STATIC
}
