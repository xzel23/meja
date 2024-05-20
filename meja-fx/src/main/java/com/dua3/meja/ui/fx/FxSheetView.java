package com.dua3.meja.ui.fx;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SheetView;
import com.dua3.meja.ui.SheetViewDelegate;
import javafx.scene.layout.Pane;

public class FxSheetView extends Pane implements SheetView {

    private final SheetViewDelegate delegate;

    public FxSheetView() {
        this(null);
    }

    public FxSheetView(@Nullable Sheet sheet) {
        this.delegate = new FxSheetViewDelegate(this, FxSheetPainter::new);
        delegate.setSheet(sheet);
    }

    @Override
    public SheetViewDelegate getDelegate() {
        return delegate;
    }

    @Override
    public void scrollToCurrentCell() {

    }

    @Override
    public void stopEditing(boolean commit) {

    }

    @Override
    public void repaintCell(Cell cell) {

    }

    @Override
    public void updateContent() {

    }

    @Override
    public boolean requestFocusInWindow() {
        return false;
    }

    @Override
    public void copyToClipboard() {

    }

    @Override
    public void showSearchDialog() {

    }

    @Override
    public void startEditing() {

    }
}
