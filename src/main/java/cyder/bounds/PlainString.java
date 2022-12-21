package main.java.cyder.bounds;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.strings.CyderStrings;

/**
 * A plain, non-html formatted string.
 */
@Immutable
public final class PlainString extends StringContainer {
    /**
     * The string contained by this plain string.
     */
    private final String containedString;

    /**
     * Suppress default constructor.
     */
    private PlainString() {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Constructs a new plain string.
     *
     * @param containedString the contained string
     */
    public PlainString(String containedString) {
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
        } else if (!(o instanceof PlainString)) {
            return false;
        }

        PlainString other = (PlainString) o;
        return containedString.equals(other.getString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "PlainString{" + containedString + "}";
    }
}
