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
package com.dua3.meja.ui.swing;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.dua3.meja.text.RichText;
import com.dua3.meja.text.Run;
import com.dua3.meja.text.Style;
import com.dua3.meja.text.TextBuilder;
import com.dua3.meja.util.MejaHelper;

/**
 * A {@link TextBuilder} implementation for translating {@code RichText}
 * to {@code StyledDocument}.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class StyledDocumentBuilder extends TextBuilder<StyledDocument> {
    private static final Logger LOGGER = Logger.getLogger(StyledDocumentBuilder.class.getName());

    /**
     * Convert {@code RichText} to {@code StyledDocument} conserving text attributes.
     * @param text an instance of {@code RichText}
     * @param dfltAttr
     * @param scale
     * @return instance of {@code StyledDocument} with {@code text} as its content
     */
    public static StyledDocument toStyledDocument(RichText text, AttributeSet dfltAttr, double scale) {
        StyledDocumentBuilder builder = new StyledDocumentBuilder(scale);
        builder.add(text);
        final StyledDocument doc = builder.get();
        doc.setParagraphAttributes(0, doc.getLength(), dfltAttr, false);
        return doc;
    }

    private final StyledDocument doc = new DefaultStyledDocument();
    private final double scale;

    public StyledDocumentBuilder(double scale) {
        this.scale = scale;
    }

    @Override
    public StyledDocument get() {
        return doc;
    }

    @Override
    protected void append(Run run) {
        SimpleAttributeSet as = new SimpleAttributeSet();
        for (Map.Entry<String, String> e: run.getStyle().properties().entrySet()) {
            switch (e.getKey()) {
            case Style.FONT_FAMILY:
                StyleConstants.setFontFamily(as, e.getValue());
                break;
            case Style.FONT_SIZE:
                StyleConstants.setFontSize(as, (int) Math.round(scale * MejaHelper.decodeFontSize(e.getValue())));
                break;
            case Style.COLOR:
                StyleConstants.setForeground(as, MejaSwingHelper.toAwtColor(e.getValue()));
                break;
            case Style.BACKGROUND_COLOR:
                StyleConstants.setBackground(as, MejaSwingHelper.toAwtColor(e.getValue()));
                break;
            case Style.FONT_WEIGHT:
                StyleConstants.setBold(as, e.getValue().equals("bold"));
                break;
            case Style.FONT_STYLE:
                switch(e.getValue()) {
                case "normal":
                    StyleConstants.setItalic(as, false);
                    break;
                case "italic":
                case "oblique":
                    StyleConstants.setItalic(as, true);
                    break;
                }
                break;
            case Style.TEXT_DECORATION:
                switch (e.getValue()) {
                case "line-through":
                    StyleConstants.setStrikeThrough(as, true);
                    break;
                case "underline":
                    StyleConstants.setUnderline(as, true);
                    break;
                }
                break;
            }
        }

        try {
            doc.insertString(doc.getLength(), run.toString(), as);
        } catch (BadLocationException ex) {
            LOGGER.log(Level.SEVERE, "Exception in StyledDocumentBuilder.append()", ex);
        }
    }

}
