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
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Font;
import com.dua3.meja.util.Cache;
import java.awt.Color;
import java.awt.Graphics2D;
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
            return new java.awt.Font(font.getFamily(), style, (int) Math.round(font.getSizeInPoints()));
        }
    };

    protected java.awt.Font getAwtFont(Font font) {
        return fontCache.get(font);
    }

    public DefaultCellRenderer() {
    }

    @Override
    public void render(Graphics2D g, Cell cell, int x, int y, int w, int h, float scale) {
        if (cell.getCellType() == CellType.BLANK) {
            return;
        }

        AttributedString text = cell.getAttributedString();

        CellStyle style = cell.getCellStyle();
        Font font = style.getFont();
        final Color color = font.getColor();

        g.setFont(getAwtFont(font));
        g.setColor(color == null ? Color.BLACK : color);

        AffineTransform originalTransform = g.getTransform();
        g.translate(x, y);
        g.scale(scale, scale);

        float wrapWidth = style.isWrap() ? w/scale : 0;

        // layout text

        FontRenderContext frc = new FontRenderContext(g.getTransform(), true, true);
        List<TextLayout> layouts = prepareText(g, scale, frc, text.getIterator(), wrapWidth);

        // determine size of text
        float textWidth = 0;
        float textHeight = 0;
        for (TextLayout layout : layouts) {
            textWidth = Math.max(textWidth, scale * layout.getVisibleAdvance());
            textHeight += scale * (layout.getAscent() + layout.getDescent() + layout.getLeading());
        }

        // calculate text position
        final float xd, yd;
        switch (style.getHAlign()) {
            case ALIGN_LEFT:
            case ALIGN_JUSTIFY:
                xd = x;
                break;
            case ALIGN_CENTER:
                xd = (float) (x + (w - textWidth) / 2.0);
                break;
            case ALIGN_RIGHT:
                xd = x + w - textWidth;
                break;
            default:
                throw new IllegalArgumentException();
        }
        switch (style.getVAlign()) {
            case ALIGN_TOP:
            case ALIGN_JUSTIFY:
                yd = y;
                break;
            case ALIGN_MIDDLE:
                yd = (float) (y + (h - textHeight - scale * layouts.get(layouts.size() - 1).getLeading()) / 2.0);
                break;
            case ALIGN_BOTTOM:
                yd = y + h - textHeight;
                break;
            default:
                throw new IllegalArgumentException();
        }

        // draw text
        g.setTransform(originalTransform);
        g.translate(xd, yd);
        g.scale(scale, scale);

        float drawPosY = 0;
        for (TextLayout layout : layouts) {
            // Compute pen x position. If the paragraph
            // is right-to-left we will align the
            // TextLayouts to the right edge of the panel.
            float drawPosX = layout.isLeftToRight() ? 0 : w - layout.getAdvance();

            // Move y-coordinate by the ascent of the
            // layout.
            drawPosY += layout.getAscent();

            // Draw the TextLayout at (drawPosX,drawPosY).
            layout.draw(g, drawPosX, drawPosY);

            // Move y-coordinate in preparation for next
            // layout.
            drawPosY += layout.getDescent() + layout.getLeading();
        }

        g.setTransform(originalTransform);
    }


    protected List<TextLayout> prepareText(Graphics2D g, float scale, FontRenderContext frc, AttributedCharacterIterator text, float width) {

        if (width <= 0) {
            // no width is given, so no wrapping will be applied.
            return Collections.singletonList(new TextLayout(text, frc));
        }

        AttributedCharacterIterator paragraph = text;
        int paragraphStart = paragraph.getBeginIndex();
        int paragraphEnd = paragraph.getEndIndex();
        LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(paragraph, frc);
        float drawPosY = 0;
        List<TextLayout> tls = new ArrayList<>();
        // Set position to the index of the first
        // character in the paragraph.
        lineMeasurer.setPosition(paragraphStart);

        // Get lines from until the entire paragraph
        // has been displayed.
        while (lineMeasurer.getPosition() < paragraphEnd) {

            TextLayout layout = lineMeasurer.nextLayout(width);

            // Compute pen x position. If the paragraph
            // is right-to-left we will align the
            // TextLayouts to the right edge of the panel.
            // Move y-coordinate by the ascent of the
            // layout.
            drawPosY += scale * layout.getAscent();

            // Draw the TextLayout at (drawPosX,drawPosY).
            tls.add(layout);

            // Move y-coordinate in preparation for next
            // layout.
            drawPosY += scale * (layout.getDescent() + layout.getLeading());
        }
        return tls;
    }

}
