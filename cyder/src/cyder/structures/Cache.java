package cyder.structures;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.logging.LogTag;
import cyder.logging.Logger;

import java.util.function.Function;

/**
 * A cache for a type.
 *
 * @param <T> the type of cache.
 */
public class Cache<T> {
    /**
     * The current cache value.
     */
    private T cachedValue;

    /**
     * The function to invoke to update the currently cached value if requested.
     */
    private Function<Void, T> cachedValueUpdater;

    /**
     * Constructs a new cache.
     */
    public Cache() {
        Logger.log(LogTag.OBJECT_CREATION, this);
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
        if (!isCachePresent() && cachedValueUpdater != null) refreshCachedValue();
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

    /**
     * Caches the provided value if no cache is currently present.
     *
     * @return whether the new cache was set
     * @throws NullPointerException if newCache is attempted to be set as the new cache value and it is null
     */
    @CanIgnoreReturnValue
    public boolean cacheIfNotPresent(T newCache) {
        if (isCachePresent()) return false;

        cachedValue = Preconditions.checkNotNull(newCache);

        return true;
    }

    /**
     * Sets the cached value updater function.
     *
     * @param function the cached value updater function to invoke when {@link #refreshCachedValue()} is called
     * @throws NullPointerException if the provided function is null
     */
    public void setCachedValueUpdater(Function<Void, T> function) {
        cachedValueUpdater = Preconditions.checkNotNull(function);
    }

    /**
     * Refreshes the cached value by invoking the cached value updater function.
     *
     * @throws IllegalStateException if the cached value updater function has not been set
     */
    public void refreshCachedValue() {
        Preconditions.checkState(cachedValueUpdater != null);
        cachedValue = cachedValueUpdater.apply(null);
    }
}
