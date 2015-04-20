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
import java.text.AttributedString;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

/**
 * A utility class for handling instances of class {@code AttributedString}.
 * @author Axel Howind (axel@dua3.com)
 */
public class AttributedStringHelper {

    private AttributedStringHelper() {
    }

    /**
     * Wrap text in HTML tag.
     * @param text
     * @param tag
     * @return {@code <tag>text</tag>}
     */
    private static String wrap(String text, String tag) {
        return "<"+tag+">"+text+"</"+tag+">";
    }

    /**
     * Extract non-attributed text.
     * @param text instance of {@code AttributedString}
     * @return the content of {@code text} as a plain {@code java.lang.String}
     */
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

    /**
     * Test for empty text.
     * @param text instance of {@code AttributedString}
     * @return true if {@code text} is of zero length
     */
    public static boolean isEmpty(AttributedString text) {
        return text.getIterator().getEndIndex()==0;
    }

    /**
     * Convert {@code AttributedString} to HTML conserving text attributes.
     * @param text an instance of {@code AttributedString}
     * @param addHtmlTag true if the resulting string should be enclosed in
     * &lt;html&gt;-tags
     * @return HTML-representation of {@code text}
     */
    public static String toHtml(AttributedString text, boolean addHtmlTag) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.add(text.getIterator());
        final String htmlText = builder.toString();
        return addHtmlTag ? wrap(htmlText, "html") : htmlText;
    }

    /**
     * Convert {@code AttributedString} to {@code StyledDocument} conserving text attributes.
     * @param text an instance of {@code AttributedString}
     * @param dfltAttr
     * @param scale
     * @return instance of {@code StyledDocument} with {@code text} as its content
     */
    public static StyledDocument toStyledDocument(AttributedString text, SimpleAttributeSet dfltAttr, float scale) {
        StyledDocumentBuilder builder = new StyledDocumentBuilder(scale);
        builder.add(text.getIterator());
        final StyledDocument doc = builder.get();
        doc.setParagraphAttributes(0, doc.getLength(), dfltAttr, false);
        return doc;
    }
}
