/*
 *
 */
package com.dua3.meja.ui.javafx;

import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public class CornerHeader extends HeaderBase {

    public CornerHeader(JfxSheetView sheetView) {
        super(sheetView, () -> -1, () -> 0);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CornerHeaderSkin(this);
    }

}
