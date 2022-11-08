package cyder.ui.slider;

/**
 * Slider thumb shapes for a {@link javax.swing.JSlider}.
 */
public enum ThumbShape {
    /**
     * A classic filled circle.
     */
    CIRCLE,

    /**
     * A rectangle biased towards the opposite slider direction.
     */
    RECTANGLE,

    /**
     * A circle that isn't filled in, you can see where the track splits colors
     * if old value is different from new value.
     */
    HOLLOW_CIRCLE,

    /**
     * No thumb will be painted.
     */
    NONE
}
