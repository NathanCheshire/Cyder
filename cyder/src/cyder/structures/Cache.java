package cyder.structures;

import com.google.common.base.Preconditions;

public class Cache<T> {
    private T cachedValue;

    /**
     * Constructs a new cache.
     */
    public Cache() {}

    /**
     * Constructs a new cache.
     *
     * @param initialValue the initial value cached
     */
    public Cache(T initialValue) {
        cachedValue = Preconditions.checkNotNull(initialValue);
    }

    public void setCache(T newCache) {
        cachedValue = Preconditions.checkNotNull(newCache);
    }

    public T getCache() {
        Preconditions.checkState(isCachePresent());
        return cachedValue;
    }

    public void clear() {
        cachedValue = null;
    }

    public boolean isCachePresent() {
        return cachedValue != null;
    }
}
