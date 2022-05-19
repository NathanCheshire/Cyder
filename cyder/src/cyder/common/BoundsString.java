package cyder.common;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.Logger;

/**
 * A String associated with the width and height it should fit in
 * based on it's contents which possibly contain html formatting.
 */
public class BoundsString {
    /**
     * The width for the container.
     */
    private int width;

    /**
     * The height for the container.
     */
    private int height;

    /**
     * The text for the container
     */
    private String text;

    /**
     * Constructs a new BoundsString object.
     *
     * @param width  the width for the string
     * @param height the height for the string
     * @param text   the string text
     */
    public BoundsString(int width, int height, String text) {
        this.width = width;
        this.height = height;
        this.text = text;

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Restrict class instantiation.
     */
    private BoundsString() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Returns the width of the string.
     *
     * @return the width of the string
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width of the string.
     *
     * @param width the width of the string
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Returns the height of the string.
     *
     * @return the height of the string
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height of the string.
     *
     * @param height the height of the string
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Returns the text of the string
     *
     * @return the text of the string
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text of the string.
     *
     * @param text the text of the string
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[" + width + "x" + height + "], Text: \"" + text + "\"";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        else if (!(o instanceof BoundsString))
            return false;

        BoundsString other = (BoundsString) o;
        return other.getWidth() == getWidth()
                && other.getHeight() == getHeight()
                && other.getText().equals(getText());
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int ret = Integer.hashCode(width);
        ret = 31 * ret + Integer.hashCode(height);
        return 31 * ret + text.hashCode();
    }
}
