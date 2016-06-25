/*
 *
 */
package com.dua3.meja.ui.javafx;

import java.util.function.IntSupplier;
import javafx.scene.control.Skin;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public class RowHeader extends HeaderBase {

    RowHeader(JfxSheetView sheetView, IntSupplier firstRow, IntSupplier lastRow) {
        super(sheetView, firstRow, lastRow);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new RowHeaderSkin(this);
    }

}
