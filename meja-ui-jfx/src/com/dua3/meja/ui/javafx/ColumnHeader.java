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
public class ColumnHeader extends HeaderBase {

    ColumnHeader(JfxSheetView sheetView, IntSupplier firstColumn, IntSupplier lastColumn) {
        super(sheetView, firstColumn, lastColumn);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ColumnHeaderSkin(this);
    }

}
