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

import java.awt.Color;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public class HtmlBuilder extends TextBuilder<String> {

    private final StringBuilder buffer = new StringBuilder();

    @Override
    protected void append(String text, Map<AttributedCharacterIterator.Attribute, Object> attributes) {
        // handle attributes
        String separator = "<span style=\"";
        String closing = "";
        String endTag = "";
        for (Map.Entry<AttributedCharacterIterator.Attribute, Object> entry : attributes.entrySet()) {
            AttributedCharacterIterator.Attribute key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                continue;
            }

            if (key.equals(java.awt.font.TextAttribute.FAMILY)) {
                buffer.append(separator).append("font-family:").append(value);
            } else if (key.equals(java.awt.font.TextAttribute.SIZE)) {
                buffer.append(separator).append("font-size:").append(value).append("pt");
            } else if (key.equals(java.awt.font.TextAttribute.FOREGROUND)) {
                buffer.append(separator).append("color:#").append(getColorValue((Color) value));
            } else if (key.equals(java.awt.font.TextAttribute.BACKGROUND)) {
                buffer.append(separator).append("background-color:#").append(getColorValue((Color) value));
            } else if (key.equals(java.awt.font.TextAttribute.WEIGHT)) {
                buffer.append(separator).append("font-weight:").append((int) (400 * (Float) value));
            } else if (key.equals(java.awt.font.TextAttribute.POSTURE)) {
                buffer.append(separator).append("font-style:");
                if (value.equals(java.awt.font.TextAttribute.POSTURE_OBLIQUE)) {
                    buffer.append("oblique");
                } else {
                    buffer.append("normal");
                }
            } else {
                continue;
            }
            separator = "; ";
            closing = "\">";
            endTag = "</span>";
        }
        buffer.append(closing);

        // append text (need to do characterwise because of escaping)
        for (char c : text.toCharArray()) {
            appendChar(c);
        }

        // add end tag;
        buffer.append(endTag);
    }

    private static String getColorValue(Color color) {
        return Integer.toHexString(color.getRGB()).substring(2);
    }

    private void appendChar(char c) {
        // escape characters as suggested by OWASP.org
        switch (c) {
            case '<':
                buffer.append("&lt;");
                break;
            case '>':
                buffer.append("&gt;");
                break;
            case '&':
                buffer.append("&amp;");
                break;
            case '"':
                buffer.append("&quot;");
                break;
            case '\'':
                buffer.append("&#x27;");
                break;
            case '/':
                buffer.append("&#x2F;");
                break;
            default:
                buffer.append(c);
                break;
        }
    }

    @Override
    protected String get() {
        return buffer.toString();
    }

}
