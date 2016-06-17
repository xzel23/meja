/*
 * Copyright 2016 Axel Howind <axel@dua3.com>.
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 *
 * @author axel
 */
public class RichText implements Iterable<Run> {

    private static RichText EMPTY_TEXT = RichText.valueOf("");

    private final String text;
    private final List<Run> runs;

    public static RichText emptyText() {
        return EMPTY_TEXT;
    }

    public static RichText valueOf(Object o) {
        return valueOf(String.valueOf(o));
    }

    public static RichText valueOf(String s) {
        return new RichText(s, Arrays.asList(new Run(s, 0, s.length(), Style.none())));
    }

    RichText(String text, List<Run> runs) {
        this.text = Objects.requireNonNull(text);
        this.runs = runs;
    }

    RichText(String text, Run[] runs) {
        this(text, Arrays.asList(runs));
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public Iterator<Run> iterator() {
        return runs.iterator();
    }

    public Stream<Run> stream() {
        return runs.stream();
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }

    public int length() {
        return text.length();
    }

}
