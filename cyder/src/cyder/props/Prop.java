package cyder.props;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import cyder.constants.CyderStrings;
import cyder.exceptions.FatalException;
import cyder.ui.drag.DragLabelButtonSize;

import java.util.Optional;

/**
 * An optional positional argument to adjust a setting about Cyder.
 *
 * @param <T> the type of the value of the prop
 */
@Immutable
@SuppressWarnings("ClassCanBeRecord") /* No */
public final class Prop<T> {
    /**
     * The key for the prop.
     */
    private final String key;

    /**
     * The default value of the prop.
     */
    private final T defaultValue;

    /**
     * The type of this prop.
     */
    private final Class<T> type;

    /**
     * Constructs a new prop.
     *
     * @param key          the key for the prop
     * @param defaultValue the default value of the prop
     * @param type         the type of this prop
     */
    public Prop(String key, T defaultValue, Class<T> type) {
        Preconditions.checkNotNull(key);
        Preconditions.checkArgument(!key.isEmpty());
        Preconditions.checkNotNull(defaultValue);
        Preconditions.checkNotNull(type);

        this.key = key;
        this.defaultValue = defaultValue;
        this.type = type;
    }

    /**
     * Returns the key for the prop.
     *
     * @return the key for the prop
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the default value of this prop.
     *
     * @return the default value of this prop
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the type of this prop.
     *
     * @return the type of this prop
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * Returns whether a value, that of the default one or a user-configured one, is present.
     *
     * @return whether a value, that of the default one or a user-configured one, is present
     */
    public boolean valuePresent() {
        if (type == String.class) {
            return !getValue().toString().isEmpty();
        } else {
            return true;
        }
    }

    /**
     * Returns the value of this prop by first checking the prop files for the
     * prop and if not present, returning the default value.
     *
     * @return the prop value
     */
    public T getValue() {
        Optional<String> optionalStringValue = PropLoader.getStringPropFromFile(getKey());
        if (optionalStringValue.isPresent()) {
            String stringValue = optionalStringValue.get();

            // todo extract comma to prop var such as splitListsAtChar
            if (type == String[].class) {
                return type.cast(stringValue.split(CyderStrings.comma));
            } else if (type == String.class) {
                return type.cast(stringValue);
            } else if (type == Boolean.class) {
                return type.cast(Boolean.valueOf(stringValue));
            } else if (type == Integer.class) {
                return type.cast(Integer.valueOf(stringValue));
            } else if (type == Double.class) {
                return type.cast(Double.valueOf(stringValue));
            } else if (type == Float.class) {
                return type.cast(Float.valueOf(stringValue));
            } else if (type == Byte.class) {
                return type.cast(Byte.valueOf(stringValue));
            } else if (type == Short.class) {
                return type.cast(Short.valueOf(stringValue));
            } else if (type == Long.class) {
                return type.cast(Long.valueOf(stringValue));
            } else if (type == Character.class) {
                return type.cast(stringValue.charAt(0));
            } else if (type == DragLabelButtonSize.class) {
                return type.cast(stringValue); // todo will this one work?
            } else {
                throw new FatalException("Case for type not handled. Type: " + type + ", stringValue: " + stringValue);
            }
        }

        return type.cast(defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Prop)) {
            return false;
        }

        Prop<?> other = (Prop<?>) o;
        return getKey().equals(other.getKey())
                && getValue().equals(other.getValue())
                && getType().equals(other.getType())
                && defaultValue.equals(other.getDefaultValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = key.hashCode();
        ret = 31 * ret + getValue().hashCode();
        ret = 31 * ret + type.hashCode();
        ret = 31 * ret + defaultValue.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Proper{"
                + "key=" + key
                + ", value=" + getValue()
                + ", type=" + type
                + ", defaultValue=" + defaultValue
                + "}";
    }
}
