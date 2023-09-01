/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.meja.util;

import com.dua3.cabe.annotations.Nullable;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * A simple cache implementation.
 * <p>
 * This class is not intended as a replacement for {@code JCache} (JSR 107).
 * </p>
 *
 * @param <K> key class
 * @param <V> value class
 */
public class Cache<K, V> {

    private final Function<? super K, ? extends V> compute;
    private final Map<K, SoftReference<V>> items;

    /**
     * Constructs a new Cache object with the given type and compute function.
     *
     * @param type    the type of the cache, either STRONG_KEYS or WEAK_KEYS
     * @param compute a function that computes the value for the given key if it is not already present in the cache
     * @throws IllegalArgumentException if the type is not STRONG_KEYS or WEAK_KEYS
     */
    public Cache(Type type, Function<? super K, ? extends V> compute) {
        this.compute = compute;
        switch (type) {
            case STRONG_KEYS -> items = new HashMap<>();
            case WEAK_KEYS -> items = new WeakHashMap<>();
            default -> throw new IllegalArgumentException("unexpected value: " + type);
        }
    }

    /**
     * Clear contents.
     */
    public void clear() {
        items.clear();
    }

    /**
     * Gets the value associated with the specified key.
     *
     * @param key the key whose associated value is to be retrieved
     * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
     */
    public V get(@Nullable K key) {
        if (key == null) {
            return null;
        }

        SoftReference<V> weak = items.get(key);
        V item = weak == null ? null : weak.get();

        if (item == null) {
            item = compute.apply(key);
            items.put(key, new SoftReference<>(item));
        }

        return item;
    }

    @Override
    public String toString() {
        return String.format("Cache backed by %s [%d entries]", items.getClass().getSimpleName(), items.size());
    }

    /**
     * Type controlling how keys should be treated in a cache.
     * <p>
     * Values are held as instances of {@link SoftReference}. That means values can
     * be garbage collected at any time. When a value is requested via the
     * {@link Cache#get(Object)} method, it will be created on-the-fly if
     * no entry for the key exists or the corresponding value has been garbage
     * collected.
     * </p>
     * <p>
     * There are two modes of operation:
     * <ul>
     * <li>When using <em>strong keys</em>, the keys are normal references.</li>
     * <li>When using <em>weak keys</em>, keys are also held using soft references.
     * This is necessary if the value itself holds a reference to the key in which
     * case entries could never be garbage collected when using strong keys. This
     * can be used for some sort of reverse mapping when there is need to store
     * additional data for instances of classes that both cannot easily be extended
     * to hold the additional data, and when there is no direct control over the
     * lifetime of these instances.
     * </ul>
     */
    public enum Type {

        /**
         * Use strong keys.
         */
        STRONG_KEYS,

        /**
         * Use weak keys.
         */
        WEAK_KEYS
    }
}
