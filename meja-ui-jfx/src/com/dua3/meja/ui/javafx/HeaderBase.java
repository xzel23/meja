/*
 *
 */
package com.dua3.meja.ui.javafx;

import java.util.function.IntSupplier;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public abstract class HeaderBase extends SheetControl {

    protected final IntSupplier first;
    protected final IntSupplier last;

    protected HeaderBase(JfxSheetView sheetView, IntSupplier first, IntSupplier last) {
        super(sheetView);
        this.first=first;
        this.last=last;

        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
    }


    public int getBegin() {
        return first.getAsInt();
    }

    public int getEnd() {
        return last.getAsInt();
    }

}
