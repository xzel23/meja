/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
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
package com.dua3.meja.util;

import java.util.Iterator;

/**
 * A helper class to convert iterators of derived classes to iterators of base classes.
 * @author Axel Howind (axel@dua3.com)
 * @param <T> the base class
 * @param <D> the derived class
 */
public class IteratorAdapter<T, D extends T>
implements Iterator<T> {
    private final Iterator<D> iter;

    /**
     * Create {@code Iterator<T>} from {@code Iterator<D>}.
     * @param iter iterator for derived class
     */
    public IteratorAdapter(Iterator<D> iter) {
    this.iter=iter;
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public T next() {
        return iter.next();
    }

}
