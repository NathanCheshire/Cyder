package cyder.bounds;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;

/**
 * An HTML formatted string.
 */
@Immutable
public final class HtmlString extends StringContainer {
    /**
     * The string contained by this html string.
     */
    private final String containedString;

    /**
     * Suppress default constructor.
     */
    private HtmlString() {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Constructs a new html string.
     *
     * @param containedString the contained string
     */
    public HtmlString(String containedString) {
        Preconditions.checkNotNull(containedString);

        this.containedString = containedString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getString() {
        return containedString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return containedString.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof HtmlString)) {
            return false;
        }

        HtmlString other = (HtmlString) o;
        return containedString.equals(other.getString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "HtmlString{" + containedString + "}";
    }
}
