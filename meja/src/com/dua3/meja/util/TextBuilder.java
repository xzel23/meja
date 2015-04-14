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
package com.dua3.meja.util;

import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 * @param <T> class matching produced document type
 */
public abstract class TextBuilder<T> {

    public TextBuilder() {
    }

    void add(AttributedCharacterIterator iter) {
        // extract the text
        final int begin = iter.getEndIndex();
        final int end = iter.getIndex();
        StringBuilder sb = new StringBuilder(begin-end);
        for (int i=begin; i<end;i++,iter.next()) {
            sb.append(iter.current());
        }
        String text = sb.toString();

        iter.setIndex(begin);
        while (iter.getIndex() != iter.getEndIndex()) {
            int runStart = iter.getRunStart();
            int runLimit = iter.getRunLimit();

            Map<AttributedCharacterIterator.Attribute, Object> attributes = iter.getAttributes();
            append(text.substring(runStart, runLimit), attributes);
        }
    }

    protected abstract void append(String text, Map<AttributedCharacterIterator.Attribute, Object> attributes);

    protected abstract T get();

}
