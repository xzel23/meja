/*
 *
 */
package com.dua3.meja.ui.javafx;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SheetView;
import java.awt.Color;
import java.beans.PropertyChangeSupport;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

/**
 *
 * @author axel
 */
public class JfxSheetView extends BorderPane implements SheetView {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Sheet sheet = null;

    public JfxSheetView() {
    }

    @Override
    public Sheet getSheet() {
return sheet;
    }

    @Override
    public void setSheet(Sheet sheet) {
        if (sheet != this.sheet) {
            Sheet oldSheet = this.sheet;
            this.sheet = sheet;
            pcs.firePropertyChange("sheet", oldSheet, sheet);
        }
    }

    @Override
    public boolean isEditable() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isEditing() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void scrollToCurrentCell() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean setCurrentCell(int rowNum, int colNum) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setEditable(boolean editable) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stopEditing(boolean commit) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
