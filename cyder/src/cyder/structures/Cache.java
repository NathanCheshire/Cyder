package cyder.structures;

import com.google.common.base.Preconditions;
import cyder.handlers.internal.Logger;

/**
 * A cache for a value.
 *
 * @param <T> the type of cached value.
 */
public class Cache<T> {
    /**
     * The current cache value.
     */
    private T cachedValue;

    /**
     * Constructs a new cache.
     */
    public Cache() {
        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Constructs a new cache.
     *
     * @param initialValue the initial value cached
     */
    public Cache(T initialValue) {
        cachedValue = Preconditions.checkNotNull(initialValue);
    }

    /**
     * Sets the cache value.
     *
     * @param newCache the new cache value
     * @throws NullPointerException if newCache is null
     */
    public void setCache(T newCache) {
        cachedValue = Preconditions.checkNotNull(newCache);
    }

    /**
     * Returns the current cache value.
     *
     * @return the current cache value
     * @throws IllegalStateException if the cache value is not present
     */
    public T getCache() {
        Preconditions.checkState(isCachePresent());
        return cachedValue;
    }

    /**
     * Clears the current cache value.
     */
    public void clear() {
        cachedValue = null;
    }

    /**
     * Returns whether the cache value is present.
     *
     * @return whether the cache value is present
     */
    public boolean isCachePresent() {
        return cachedValue != null;
    }
}
