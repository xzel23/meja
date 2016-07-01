/*
 *
 */
package com.dua3.meja.ui.javafx;

import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SheetView;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.control.Control;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public abstract class SheetControl extends Control implements PropertyChangeListener {

    public static final EventType<Event> EVENT_TYPE_LAYOUT_CHANGED = new EventType<>("layout changed");

    private final JfxSheetView sheetView;

    protected SheetControl(JfxSheetView sheetView) {
        this.sheetView = sheetView;

        sheetView.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        assert evt.getSource() == sheetView;
        if (evt.getPropertyName().equals(SheetView.PROPERTY_SHEET)) {
            layoutChanged();
        }
    }

    protected void layoutChanged() {
        fireEvent(new Event(EVENT_TYPE_LAYOUT_CHANGED));
    }

    public Sheet getSheet() {
        return sheetView.getSheet();
    }

    public JfxSheetPainter getSheetPainter() {
        return sheetView.getSheetPainter();
    }

}
