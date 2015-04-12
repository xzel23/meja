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
import com.dua3.meja.util.AttributedStringHelper;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.JTextPane;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public class DefaultCellEditor implements CellEditor {

    private final JTextPane painter = new JTextPane();

    public DefaultCellEditor() {
        painter.setContentType("text/html");
        painter.setOpaque(false);
    }

    public void render(Graphics2D g, Cell cell, Rectangle cr, Rectangle clipRect, float scale) {
        painter.setBounds(clipRect);
        painter.setText(AttributedStringHelper.toHtml(cell.getAttributedString(), true));

        CellStyle style = cell.getCellStyle();
        final int x;
        final int w;
        if (style.isWrap()||style.getHAlign().isWrap()||style.getVAlign().isWrap()) {
            x = cr.x;
            w = cr.width;
        } else {
            switch (style.getHAlign()) {
                default:
                    x = cr.x;
                    w = Math.max(cr.width, clipRect.x+clipRect.width-cr.x);
                    break;
                case ALIGN_CENTER:
                    x = cr.x;
                    w = cr.width;
                    break;
            }
        }

        final Graphics2D g_ = (Graphics2D) g.create(x, cr.y, w, cr.height);
        g_.scale(scale, scale);
        painter.paint(g_);
    }

}
