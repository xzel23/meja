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
import com.dua3.meja.util.AttributedStringHelper;
import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.JTextPane;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public class DefaultCellEditor implements CellEditor {

    private final JTextPane component;
    private Cell cell;

    public DefaultCellEditor() {
        component = new JTextPane();
        component.setContentType("text/html");
        component.setOpaque(false);
        component.setBackground(Color.WHITE);
        cell = null;
    }

    @Override
    public boolean isEditing() {
        return cell != null;
    }

    @Override
    public JComponent startEditing(Cell cell) {
        if (isEditing()) {
            throw new IllegalStateException("Already editing.");
        }
        this.cell = cell;
        component.setText(AttributedStringHelper.toHtml(cell.getAttributedString(), true));
        return component;
    }

    @Override
    public void stopEditing(boolean commit) {
        if (commit) {
            // TODO
        }
        this.cell=null;
        component.setText("");
        component.setVisible(false);
    }

    /*
     public void render(Graphics2D g, Cell cell, Rectangle cr, Rectangle clipRect, float scale) {
     component.setBounds(clipRect);
     component.setText(AttributedStringHelper.toHtml(cell.getAttributedString(), true));

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
     component.paint(g_);
     }
     */
}
