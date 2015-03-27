/*
 * Copyright 2015 Axel Howind <axel@dua3.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dua3.meja.model.poi;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 *
 * @author axel
 * @param <KEY>
 * @param <VALUE>
 */
public abstract class PoiCache<KEY, VALUE> {

    private final WeakHashMap<KEY, WeakReference<VALUE>> items = new WeakHashMap<>();

    public VALUE get(KEY key) {
        if (key == null) {
            return null;
        }

        WeakReference<VALUE> weak = items.get(key);
        VALUE item = weak == null ? null : weak.get();

        if (item == null) {
            item = create(key);
            items.put(key, new WeakReference<>(item));
        }

        return item;
    }

    protected abstract VALUE create(KEY key);
}
