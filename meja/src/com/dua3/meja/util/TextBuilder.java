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

import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 * Base class for text builders.
 * This class is intended a s a common base class for creating builders
 * that transform text represented as {@code AttributedString} into other
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
     * @param iter character iterator
     */
    void add(AttributedCharacterIterator iter) {
        // extract the text
        final int begin = iter.getBeginIndex();
        final int end = iter.getEndIndex();
        StringBuilder sb = new StringBuilder(end-begin);
        for (int i=begin; i<end;i++,iter.next()) {
            sb.append(iter.current());
        }
        String text = new String(sb);

        iter.setIndex(begin);
        while (iter.getIndex() != end) {
            int runStart = iter.getRunStart();
            int runLimit = iter.getRunLimit();

            Map<AttributedCharacterIterator.Attribute, Object> attributes = iter.getAttributes();
            append(text.substring(runStart, runLimit), attributes);
            iter.setIndex(runLimit);
        }
    }

    /**
     * Add text to document.
     * Implementations must override this method to append {@code text} which
     * is attributed with {@code attributes} to the result document.
     * @param text the text to append
     * @param attributes the attributes to use
     */
    protected abstract void append(String text, Map<AttributedCharacterIterator.Attribute, Object> attributes);

    /**
     * Get document.
     * @return the document after transformation.
     */
    protected abstract T get();

}
