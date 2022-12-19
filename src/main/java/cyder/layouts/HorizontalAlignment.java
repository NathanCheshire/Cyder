package main.java.cyder.layouts;

/**
 * An alignment property to determine how components are laid out
 * on a specific axis and what to do with excess space.
 */
public enum HorizontalAlignment {
    /**
     * Components are aligned on the left with minimum spacing in between.
     */
    LEFT,
    /**
     * Components are centered and excess space is placed in between components.
     */
    CENTER,
    /**
     * Components are aligned on the right with minimum spacing in between.
     */
    RIGHT,
    /**
     * Components are centered absolutely with the excess space placed evenly
     * on on the left of the left most component and the right of the right most
     * component.
     */
    CENTER_STATIC
}
