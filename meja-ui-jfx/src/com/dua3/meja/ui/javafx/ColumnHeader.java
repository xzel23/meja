/*
 *
 */
package com.dua3.meja.ui.javafx;

import com.dua3.meja.model.Sheet;
import static com.dua3.meja.ui.SheetView.PROPERTY_SHEET;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.function.IntSupplier;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public class ColumnHeader extends Control implements PropertyChangeListener {

    public static final EventType<Event> EVENT_TYPE_LAYOUT_CHANGED = new EventType<>("layout changed");

    private final JfxSheetView sheetView;
    private final IntSupplier firstColumn;
    private final IntSupplier lastColumn;

    ColumnHeader(JfxSheetView sheetView, IntSupplier firstColumn, IntSupplier lastColumn) {
        this.sheetView=sheetView;
        this.firstColumn=firstColumn;
        this.lastColumn=lastColumn;

        sheetView.addPropertyChangeListener(this);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);

        layout();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ColumnHeaderSkin(this);
    }

    public int getFirstColumn() {
        return firstColumn.getAsInt();
    }

    public int getLastColumn() {
        return lastColumn.getAsInt();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        assert evt.getSource()==sheetView;
        if (evt.getPropertyName().equals(PROPERTY_SHEET)) {
            fireEvent(new Event(EVENT_TYPE_LAYOUT_CHANGED));
        }
    }

    Sheet getSheet() {
        return sheetView.getSheet();
    }

}
