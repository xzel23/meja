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

    private final Function<? super K, ? extends V> compute;

    private final Map<K, SoftReference<V>> items;

    public Cache(Type type, Function<? super K, ? extends V> compute) {
        this.compute = compute;
        switch (type) {
            case STRONG_KEYS -> items = new HashMap<>();
            case WEAK_KEYS -> items = new WeakHashMap<>();
            default -> throw new IllegalArgumentException();
        }
    }

    /**
     * Clear contents.
     */
    public void clear() {
        items.clear();
    }

    public V get(K key) {
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
}
