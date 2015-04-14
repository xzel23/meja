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
import java.text.AttributedString;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class AttributedStringHelper {

    private static String wrap(String text, String tag) {
        return "<"+tag+">"+text+"</"+tag+">";
    }

    public static String toString(AttributedString text) {
        AttributedCharacterIterator iter = text.getIterator();
        final int endIndex = iter.getEndIndex();
        StringBuilder sb = new StringBuilder(endIndex);
        while (iter.getIndex()<endIndex) {
            sb.append(iter.current());
            iter.next();
        }
        return sb.toString();
    }

    public static boolean isEmpty(AttributedString text) {
        return text.getIterator().getEndIndex()==0;
    }

    private AttributedStringHelper() {
    }

    public static String toHtml(AttributedString text, boolean addHtmlTag) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.add(text.getIterator());
        final String htmlText = builder.toString();
        return addHtmlTag ? wrap(htmlText, "html") : htmlText;
    }

    public static StyledDocument toStyledDocument(AttributedString text) {
        StyledDocumentBuilder builder = new StyledDocumentBuilder();
        builder.add(text.getIterator());
        return builder.get();
    }
}
