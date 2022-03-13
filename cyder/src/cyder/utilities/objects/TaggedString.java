package cyder.utilities.objects;

import cyder.constants.CyderStrings;
import cyder.enums.LoggerTag;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.Logger;
import cyder.utilities.ReflectionUtil;

/**
 * Class representing a segment of text as either being raw text or an html tag
 */
public class TaggedString {
    /**
     * The type a given String is: HTML or TEXT
     */
    public enum Type {
        HTML,TEXT
    }

    private String text;
    private Type type;

    private TaggedString() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    public TaggedString(String text, Type type) {
        this.text = text;
        this.type = type;

        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int hashCode() {
        int ret = text.hashCode();
        ret = 31 * ret + type.hashCode();
        return ret;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } if (!(o instanceof TaggedString)) {
            return false;
        }

        TaggedString other = (TaggedString) o;

        return getText().equals(other.getText()) && getType() == other.getType();
    }

    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
