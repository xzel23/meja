package com.dua3.meja.util;

import java.util.function.Function;

import com.dua3.meja.util.Cache.Type;

/**
 * A simple object cache used to avoid creating unnecessary instances of some implementation classes, notably
 * in the POI implementation.
 */
public class ObjectCache {

    private final Cache<Object, Object> cache;

    /**
     * Initializes a new instance of the ObjectCache class.
     * This constructor initializes the cache using weak keys and a function identity mapper.
     */
    public ObjectCache() {
        cache = new Cache<>(Type.WEAK_KEYS, Function.identity());
    }

    /**
     * Retrieves an item from the cache.
     *
     * @param <T>  the type of the item to retrieve from the cache
     * @param item the item to retrieve from the cache
     * @return the item retrieved from the cache, or null if the item is not present in the cache
     */
    @SuppressWarnings("unchecked")
    public <T> T get(T item) {
        return (T) cache.get(item);
    }
}
