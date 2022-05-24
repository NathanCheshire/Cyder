package cyder.common;

/**
 * Class representing a segment of text as either being raw text or an html tag
 */
public record TaggedString(String text, cyder.common.TaggedString.Type type) {
    /**
     * The type a given String is: HTML or TEXT
     */
    public enum Type {
        HTML, TEXT
    }
}
