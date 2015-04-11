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
package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Font;
import com.dua3.meja.util.Cache;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public class DefaultCellRenderer implements CellRenderer {

    private final Cache<Font, java.awt.Font> fontCache = new Cache<Font, java.awt.Font>() {
        @Override
        protected java.awt.Font create(Font font) {
            int style = (font.isBold() ? java.awt.Font.BOLD : 0) | (font.isItalic() ? java.awt.Font.ITALIC : 0);
            return new java.awt.Font(font.getFamily(), style, Math.round(font.getSizeInPoints()));
        }
    };

    protected java.awt.Font getAwtFont(Font font) {
        return fontCache.get(font);
    }

    public DefaultCellRenderer() {
    }

    @Override
    public void render(Graphics2D g, Cell cell, Rectangle cr, Rectangle clipRect, float scale) {
        if (cell.isEmpty()) {
            return;
        }

        AttributedString text = cell.getAttributedString();

        CellStyle style = cell.getCellStyle();
        Font font = style.getFont();
        final Color color = font.getColor();

        g.setFont(getAwtFont(font));
        g.setColor(color == null ? Color.BLACK : color);

        AffineTransform originalTransform = g.getTransform();
        g.scale(scale, scale);

        boolean wrapText = SheetView.isWrapping(style);
        float wrapWidth = wrapText ? cr.width / scale : 0;

        // layout text
        FontRenderContext frc = g.getFontRenderContext();
        List<TextLayout> layouts = prepareText(g, scale, frc, text.getIterator(), wrapWidth);

        // determine size of text
        float textWidth = 0;
        float textHeight = 0;
        for (TextLayout layout : layouts) {
            textWidth = Math.max(textWidth, scale * layout.getVisibleAdvance());
            textHeight += scale * (layout.getAscent() + layout.getDescent() + layout.getLeading());
        }

        // calculate text position
        final float xd = cr.x;
        float yd;
        switch (style.getVAlign()) {
            case ALIGN_TOP:
            case ALIGN_JUSTIFY:
                yd = cr.y;
                break;
            case ALIGN_MIDDLE:
                yd = cr.y + (cr.height - textHeight - scale * layouts.get(layouts.size() - 1).getLeading()) / 2f;
                break;
            case ALIGN_BOTTOM:
                yd = cr.y + cr.height - textHeight;
                break;
            default:
                throw new IllegalArgumentException();
        }

        // draw text
        g.setTransform(originalTransform);
        g = (Graphics2D) g.create(clipRect.x, clipRect.y, clipRect.width, clipRect.height);
        g.translate(xd - clipRect.x, yd - clipRect.y);
        g.scale(scale, scale);

        float drawPosY = 0;
        for (TextLayout layout : layouts) {
                // Compute pen x position. If the paragraph is right-to-left
            // we will align the TextLayouts to the right edge of the panel.
            float drawPosX;
            switch (style.getHAlign()) {
                default:
                    // default is left aligned
                    drawPosX = layout.isLeftToRight() ? 0 : cr.width / scale - layout.getAdvance();
                    break;
                case ALIGN_RIGHT:
                    drawPosX = layout.isLeftToRight() ? cr.width / scale - layout.getAdvance() : 0;
                    break;
                case ALIGN_CENTER:
                    drawPosX = (cr.width / scale - layout.getAdvance()) / 2f;
                    break;
            }

            // Move y-coordinate by the ascent of the layout.
            drawPosY += layout.getAscent();

            // Draw the TextLayout at (drawPosX,drawPosY).
            layout.draw(g, drawPosX, drawPosY);

            // Move y-coordinate in preparation for next layout.
            drawPosY += layout.getDescent() + layout.getLeading();
        }
        g.setTransform(originalTransform);
    }

    protected List<TextLayout> prepareText(Graphics2D g, float scale, FontRenderContext frc, AttributedCharacterIterator text, float width) {
        if (width <= 0) {
            // no width is given, so no wrapping will be applied.
            return Collections.singletonList(new TextLayout(text, frc));
        }

        // Get all lines from text.
        List<TextLayout> tls = new ArrayList<>();
        int paragraphEnd = text.getEndIndex();
        LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(text, frc);
        while (lineMeasurer.getPosition() < paragraphEnd) {
            tls.add(lineMeasurer.nextLayout(width));
        }
        return tls;
    }

}
