package cyder.props;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;

import java.util.Optional;

/**
 * An optional positional argument to adjust a setting about Cyder.
 *
 * @param <T> the type of the value of the prop
 */
@Immutable
public final class Proper<T> {
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
    public Proper(String key, T defaultValue, Class<T> type) {
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
     * Returns the default value of the prop.
     *
     * @return the default value of the prop
     */
    public T getDefaultValue() {
        return type.cast(defaultValue);
    }

    /**
     * Returns the value of this prop by first checking the prop files for the
     * prop and if not present, returning the default value.
     *
     * @return the prop value
     */
    public T getValue() {
        Optional<String> optionalStringValue = PropLoader.getStringProp(getKey());
        if (optionalStringValue.isPresent()) {
            String stringValue = optionalStringValue.get();

            if (type == String[].class) {
                return type.cast(stringValue.split(","));
            } else {
                return type.cast(stringValue);
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
        } else if (!(o instanceof Proper)) {
            return false;
        }

        Proper<?> other = (Proper<?>) o;
        return getKey().equals(other.getKey())
                && getValue().equals(other.getValue())
                && getDefaultValue().equals(other.getDefaultValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = getKey().hashCode();
        ret += 31 * ret + getValue().hashCode();
        ret += 31 * ret + getDefaultValue().hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Proper{"
                + "name=" + getKey()
                + ", value=" + getValue()
                + ", defaultValue=" + getDefaultValue()
                + "}";
    }
}
