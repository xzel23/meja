/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.CellType;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Scale2f;
import com.dua3.utility.swing.TextEditorPane;
import com.dua3.utility.swing.SwingUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.RichText;

/**
 * A custom text pane for editing cell content with support for styling, alignment,
 * and rendering specific to spreadsheet-like cells.
 */
public class CellEditorPane extends TextEditorPane {

    /**
     * Construct a new CellEditorPane.
     */
    public CellEditorPane() {
        setWrapText(false);
        setEditable(true);
    }

    /**
     * Set the editor content to the content of the given cell.
     *
     * @param cell the cell to display
     * @param scale the font scaling factor to use
     * @param eval set to true to display formula results instead of the formula
     *             itself
     */
    public void setContent(Cell cell, Scale2f scale, boolean eval) {
        CellStyle cellStyle = cell.getCellStyle();

        Color fg = cellStyle.getFillFgColor();
        Color bg = cellStyle.getFillBgColor();
        Font font = cellStyle.getFont();

        getTextComponent().setBackground(SwingUtil.convert(fg));
        getTextComponent().setForeground(SwingUtil.convert(bg));
        setTextFont(font.scaled(scale.sy()));

        final RichText text;
        if (!eval && cell.getCellType() == CellType.FORMULA) {
            text = RichText.valueOf("=" + cell.getFormula());
        } else {
            text = cell.getAsText(getLocale());
        }

        setText(text);

        revalidate();
        repaint();
    }

}
