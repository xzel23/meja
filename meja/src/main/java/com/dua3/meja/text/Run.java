/*
 * Copyright 2016 Axel Howind.
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
package com.dua3.meja.text;

import java.util.Objects;

public class Run
        implements CharSequence {

    private final String text;
    private final int start;
    private final int length;
    private final Style style;

    Run(String text, int start, int length, Style style) {
        if (start < 0 || start > text.length() || length < 0 || start + length > text.length()) {
            throw new IllegalArgumentException();
        }

        this.text = Objects.requireNonNull(text);
        this.style = Objects.requireNonNull(style);
        this.start = start;
        this.length = length;
    }

    @Override
    public char charAt(int index) {
        assert index >= 0 && index < length;
        return text.charAt(start + index);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Run other = (Run) obj;
        // it is not sufficient to compare text since even for different
        // strings, the char sequences represented by two runs might be
        // the same.
        if (length != other.length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (charAt(i) != other.charAt(i)) {
                return false;
            }
        }

        return style.equals(other.style);
    }

    public int getEnd() {
        return start + length;
    }

    public int getStart() {
        return start;
    }

    public Style getStyle() {
        return style;
    }

    @Override
    public int hashCode() {
        int h = style.hashCode();
        for (int i = 0; i < length; i++) {
            h = 31 * h + charAt(i);
        }
        return h;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new Run(text, this.start + start, end - start, style);
    }

    @Override
    public String toString() {
        return text.substring(start, start + length);
    }

}
