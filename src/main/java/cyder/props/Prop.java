package cyder.props;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import cyder.exceptions.FatalException;

import java.util.Objects;
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
     * The cache of the prop value specified in a local prop file after being cast to T.
     */
    private T cachedCastedPropSpecifiedValue = null;

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
        Preconditions.checkArgument(defaultValue.getClass().equals(type));

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
        if (cachedCastedPropSpecifiedValue == null) {
            attemptToSetCachedCastedPropSpecifiedValue();
        }

        if (cachedCastedPropSpecifiedValue != null) {
            return cachedCastedPropSpecifiedValue;
        }

        return type.cast(defaultValue);
    }

    /**
     * Attempts to set the cached casted prop specified value by invoking
     * {@link PropLoader#getPropValueStringFromFile(String)} and providing {@link #key}.
     * The result is then attempted to be casted to T and stored for future reference.
     */
    private void attemptToSetCachedCastedPropSpecifiedValue() {
        Optional<String> optionalStringValue = PropLoader.getPropValueStringFromFile(getKey());

        if (optionalStringValue.isPresent()) {
            String stringValue = optionalStringValue.get();

            if (type == PropValueList.class) {
                cachedCastedPropSpecifiedValue = type.cast(new PropValueList(
                        ImmutableList.copyOf(stringValue.split(PropConstants.splitListsAtChar))));
            } else if (type == String.class) {
                cachedCastedPropSpecifiedValue = type.cast(stringValue);
            } else if (type == Boolean.class) {
                cachedCastedPropSpecifiedValue = type.cast(Boolean.valueOf(stringValue));
            } else if (type == Integer.class) {
                cachedCastedPropSpecifiedValue = type.cast(Integer.valueOf(stringValue));
            } else if (type == Double.class) {
                cachedCastedPropSpecifiedValue = type.cast(Double.valueOf(stringValue));
            } else if (type == Float.class) {
                cachedCastedPropSpecifiedValue = type.cast(Float.valueOf(stringValue));
            } else if (type == Byte.class) {
                cachedCastedPropSpecifiedValue = type.cast(Byte.valueOf(stringValue));
            } else if (type == Short.class) {
                cachedCastedPropSpecifiedValue = type.cast(Short.valueOf(stringValue));
            } else if (type == Long.class) {
                cachedCastedPropSpecifiedValue = type.cast(Long.valueOf(stringValue));
            } else if (type == Character.class) {
                cachedCastedPropSpecifiedValue = type.cast(stringValue.charAt(0));
            } else {
                throw new FatalException("Case for type not handled. Type: " + type + ", stringValue: " + stringValue);
            }
        }
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
                && defaultValue.equals(other.getDefaultValue())
                && Objects.equals(cachedCastedPropSpecifiedValue, other.cachedCastedPropSpecifiedValue);
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
        if (cachedCastedPropSpecifiedValue != null) ret = 31 * ret + cachedCastedPropSpecifiedValue.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Prop{"
                + "key=" + key
                + ", value=" + getValue()
                + ", type=" + type
                + ", defaultValue=" + defaultValue
                + ", cachedCastedPropSpecifiedValue=" + cachedCastedPropSpecifiedValue
                + "}";
    }
}
