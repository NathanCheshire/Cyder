package cyder.bounds;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

/**
 * A class representing a segment of text as either being raw text or an HTML tag.
 * Instances of this class are immutable and thus thread-safe.
 */
@Immutable
public final class TaggedString {
    /** The text of the tagged string. */
    private final String text;

    /** The tag of the string. */
    private final TaggedStringType type;

    /** Suppress default constructor. */
    private TaggedString() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Constructs a new tagged string.
     *
     * @param text the text
     * @param type the type
     */
    public TaggedString(String text, TaggedStringType type) {
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(!text.isEmpty());
        Preconditions.checkNotNull(type);

        this.text = text;
        this.type = type;
    }

    /**
     * Returns the text of the tagged string.
     *
     * @return the text of the tagged string
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the type of the tagged string.
     *
     * @return the type of the tagged string
     */
    public TaggedStringType getType() {
        return type;
    }
}
