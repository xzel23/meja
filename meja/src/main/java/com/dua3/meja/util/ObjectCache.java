package com.dua3.meja.util;

import java.util.function.Function;

import com.dua3.meja.util.Cache.Type;

/**
 * A simple object cache used to avoid creating unnecessary instances of some implementation classes, notably
 * in the POI implementation.
 */
public class ObjectCache {

    private final Cache<Object, Object> cache;

    public ObjectCache() {
        cache = new Cache<>(Type.WEAK_KEYS, Function.identity());
    }

    @SuppressWarnings("unchecked")
    public <T> T get(T item) {
        return (T) cache.get(item);
    }
}
