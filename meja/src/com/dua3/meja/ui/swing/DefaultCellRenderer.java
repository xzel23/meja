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

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.BorderFactory;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class DefaultCellRenderer implements CellRenderer {

    private final CellEditorPane component;

    /**
     * Creae a new {@code DefaultCellRenderer}.
     */
    public DefaultCellRenderer() {
        component = new CellEditorPane();
        component.setOpaque(false);
        component.setBorder(BorderFactory.createEmptyBorder());
    }

    @Override
    public void render(Graphics2D g, Cell cell, Rectangle cr, Rectangle clip, float scale) {
        if (cell.isEmpty()) {
            return;
        }

        int maxWidthScaled = (int) (SheetView.MAX_WIDTH*scale);

        // if text is not wrapped, paint with a maximum width to allow overflowing text
        CellStyle style = cell.getCellStyle();
        boolean wrap = style.isWrap() || style.getHAlign().isWrap() || style.getVAlign().isWrap();
        Rectangle bounds = new Rectangle(wrap ? cr.width : maxWidthScaled, cr.height);
        Rectangle canvas;
        if (wrap) {
            canvas = cr;
        } else {
            switch (CellEditorPane.getHAlign(style.getHAlign(), cell.getResultType())) {
                case ALIGN_LEFT:
                    canvas = new Rectangle(cr.x, cr.y, maxWidthScaled, cr.height);
                    break;
                case ALIGN_RIGHT:
                    canvas = new Rectangle(cr.x+cr.width-maxWidthScaled, cr.y, maxWidthScaled, cr.height);
                    break;
                case ALIGN_CENTER:
                    canvas = new Rectangle(cr.x+(cr.width-maxWidthScaled)/2, cr.y, maxWidthScaled, cr.height);
                    break;
                case ALIGN_JUSTIFY:   // ALIGN_JUSTIFY implies wrap
                case ALIGN_AUTOMATIC: // ALIGN_AUTOMATIC should already be mapped to another value
                default:
                    throw new IllegalStateException();
            }
        }

        component.setBounds(bounds);
        component.setContent(cell, scale, true);

        // setup a graphics context for painting
        clip = canvas.intersection(clip).intersection(g.getClipBounds());
        clip.translate(-canvas.x, -canvas.y);
        final Graphics gPaint = g.create(canvas.x, canvas.y, canvas.width, canvas.height);
        gPaint.setClip(clip);

        component.paint(gPaint);
    }

}
