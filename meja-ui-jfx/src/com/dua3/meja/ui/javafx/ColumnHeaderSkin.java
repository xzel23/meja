/*
 *
 */
package com.dua3.meja.ui.javafx;

import com.dua3.meja.model.Sheet;
import com.dua3.meja.util.MejaHelper;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public class ColumnHeaderSkin extends HeaderSkinBase<ColumnHeader> {


    public ColumnHeaderSkin(ColumnHeader columnHeader) {
        super(columnHeader);
        init(columnHeader);
        redraw();
    }

    @Override
    protected double getPreferredWidth() {
        ColumnHeader columnHeader = (ColumnHeader) getNode();
        Sheet sheet = columnHeader.getSheet();

        if (sheet == null) {
            return 0;
        }

        double w = 0;
        for (int j = columnHeader.getBegin(); j <= columnHeader.getEnd(); j++) {
            w += sheet.getColumnWidth(j);
        }
        return w;
    }

    @Override
    protected double getPreferredHeight() {
        return getDefaultHeight();
    }

    static double getDefaultHeight() {
        return 20;
    }

    @Override
    protected String getName(int i) {
        return MejaHelper.getColumnName(i);
    }

    @Override
    protected double getWidth(Sheet sheet, int i) {
        return sheet == null ? 0 : sheet.getColumnWidth(i);
    }

    @Override
    protected double getHeight(Sheet sheet, int i) {
        return getPreferredHeight();
    }

    @Override
    protected double nextX(double x, double w) {
        return x+w;
    }

}
