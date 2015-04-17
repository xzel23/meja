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

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * A {@link TextBuilder} implementation for translating {@code AttributedString}
 * to {@code StyledDocument}.
 * @author Axel Howind (axel@dua3.com)
 */
public class StyledDocumentBuilder extends TextBuilder<StyledDocument> {

    private final StyledDocument doc = new DefaultStyledDocument();

    @Override
    protected StyledDocument get() {
        return doc;
    }

    @Override
    protected void append(String text, Map<AttributedCharacterIterator.Attribute, Object> attributes) {
        SimpleAttributeSet as = new SimpleAttributeSet();
        for (Map.Entry<AttributedCharacterIterator.Attribute, Object> entry: attributes.entrySet()) {
            AttributedCharacterIterator.Attribute key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                continue;
            }

            if (key.equals(java.awt.font.TextAttribute.FAMILY)) {
                StyleConstants.setFontFamily(as, value.toString());
            } else if (key.equals(java.awt.font.TextAttribute.SIZE)) {
                StyleConstants.setFontSize(as, ((Number)value).intValue());
            } else if (key.equals(java.awt.font.TextAttribute.FOREGROUND)) {
                StyleConstants.setForeground(as, (Color) value);
            } else if (key.equals(java.awt.font.TextAttribute.BACKGROUND)) {
                StyleConstants.setBackground(as, (Color) value);
            } else if (key.equals(java.awt.font.TextAttribute.WEIGHT)) {
                StyleConstants.setBold(as, ((Number) value).floatValue()>TextAttribute.WEIGHT_MEDIUM);
            } else if (key.equals(java.awt.font.TextAttribute.POSTURE)) {
                StyleConstants.setItalic(as, value.equals(java.awt.font.TextAttribute.POSTURE_OBLIQUE));
            }
            as.addAttribute(key, value);
        }

        try {
            doc.insertString(doc.getLength(), text, as);
        } catch (BadLocationException ex) {
            Logger.getLogger(StyledDocumentBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
