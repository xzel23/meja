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
package com.dua3.meja.text;

/**
 * Base class for text builders.
 * This class is intended a s a common base class for creating builders
 * that transform text represented as {@code RichText} into other
 * formats.
 * @author Axel Howind (axel@dua3.com)
 * @param <T> class matching produced document type
 */
public abstract class TextBuilder<T> {

    /**
     * Constructor.
     */
    protected TextBuilder() {
    }

    /**
     * Add text.
     * @param text
     *  the richt text to add
     */
    public void add(RichText text) {
        for (Run r: text) {
            append(r);
        }
    }

    /**
     * Add text to document.
     * Implementations must override this method to append {@code text} which
     * is attributed with {@code attributes} to the result document.
     * @param run
     */
    protected abstract void append(Run run);

    /**
     * Get document.
     * @return the document after transformation.
     */
    protected abstract T get();

}
