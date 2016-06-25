/*
 *
 */
package com.dua3.meja.ui.javafx;

import com.dua3.meja.model.Sheet;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public class RowHeaderSkin extends HeaderSkinBase<RowHeader> {

    static double getDefaultWidth() {
        return 40;
    }

    public RowHeaderSkin(RowHeader rowHeader) {
        super(rowHeader);
        init(rowHeader);
        redraw();
    }

    @Override
    protected double getPreferredWidth() {
        return getDefaultWidth();
    }


    @Override
    protected double getPreferredHeight() {
        RowHeader rowHeader = (RowHeader) getNode();
        Sheet sheet = rowHeader.getSheet();

        if (sheet == null) {
            return 0;
        }

        double h = 0;
        for (int i = rowHeader.getBegin(); i <= rowHeader.getEnd(); i++) {
            h += sheet.getRowHeight(i);
        }
        return h;
    }

    @Override
    protected String getName(int i) {
        return Integer.toString(i+1);
    }

    @Override
    protected double getWidth(Sheet sheet, int i) {
        return getPreferredWidth();
    }

    @Override
    protected double getHeight(Sheet sheet, int i) {
        return sheet == null ? 0 : sheet.getRowHeight(i);
    }

    @Override
    protected double nextY(double y, double h) {
        return y+h;
    }

}
