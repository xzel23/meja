/*
 *
 */
package com.dua3.meja.ui.javafx;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SheetView;
import java.awt.Color;
import javafx.scene.layout.Pane;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

/**
 *
 * @author axel
 */
public class JfxSheetView extends Pane implements SheetView {

    private Sheet sheet = null;
    private final SpreadsheetView view;

    public JfxSheetView() {
        super(new SpreadsheetView());
        view = (SpreadsheetView) getChildren().get(0);
    }

    @Override
    public Color getGridColor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Sheet getSheet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isEditable() {
        return view.isEditable();
    }

    @Override
    public boolean isEditing() {
        return view.isEditable() && view.getEditingCell() != null;
    }

    @Override
    public void scrollToCurrentCell() {
        Cell cell = sheet.getCurrentCell();
        view.scrollToColumnIndex(cell.getColumnNumber());
        view.scrollToRow(cell.getRowNumber());
    }

    @Override
    public boolean setCurrentCell(int rowNum, int colNum) {
        // FIXME
        return false;
    }

    @Override
    public void setEditable(boolean editable) {
        view.setEditable(editable);
    }

    @Override
    public void setGridColor(Color gridColor) {
        // fixme
    }

    @Override
    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
        view.setGrid(MejaJfxHelper.getGrid(sheet));
    }

    @Override
    public void stopEditing(boolean commit) {
        // fixme
    }

    @Override
    public void updateContent() {
        // fixme
    }
}
