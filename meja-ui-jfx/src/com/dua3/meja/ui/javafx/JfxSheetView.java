/*
 *
 */
package com.dua3.meja.ui.javafx;

import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SheetView;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.function.IntSupplier;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.layout.GridPane;

/**
 *
 * @author axel
 */
public class JfxSheetView extends Control implements SheetView, PropertyChangeListener {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Sheet sheet = null;
    private final Node headerTopLeft, columnHeaderLeft, columnHeaderRight,
            rowHeaderTop, leftTopChart, rightTopChart,
            rowHeaderBottom, leftBottomChart, rightBottomChart;

    public JfxSheetView() {
        final GridPane gridPane = new GridPane();

        gridPane.setGridLinesVisible(true); // FIXME

        // define row and column ranges
        final IntSupplier startColumn = () -> 0;
        final IntSupplier splitColumn = () -> getSplitColumn();
        final IntSupplier endColumn = () -> getColumnCount();

        final IntSupplier startRow = () -> 0;
        final IntSupplier splitRow = () -> getSplitRow();
        final IntSupplier endRow = () -> getColumnCount();

        headerTopLeft = new CornerHeader(this);
        columnHeaderLeft = new ColumnHeader(this, startColumn, splitColumn);
        columnHeaderRight = new ColumnHeader(this, splitColumn, endColumn);

        rowHeaderTop = new RowHeader(this, startRow, splitRow);
        leftTopChart = new SegmentView(this, startRow, splitRow, startColumn, splitColumn);
        rightTopChart = new SegmentView(this, startRow, splitRow, splitColumn, endColumn);

        rowHeaderBottom = new RowHeader(this, splitRow, endRow);
        leftBottomChart = new SegmentView(this, splitRow, endRow, startColumn, splitColumn);
        rightBottomChart = new SegmentView(this, splitRow, endRow, splitColumn, endColumn);

        gridPane.addRow(1, headerTopLeft, columnHeaderLeft, columnHeaderRight);
        gridPane.addRow(2, rowHeaderTop, leftTopChart, rightTopChart);
        gridPane.addRow(3, rowHeaderBottom, leftBottomChart, rightBottomChart);

        getChildren().setAll(gridPane);
    }

    private int getColumnCount() {
        return sheet == null ? 0 : sheet.getColumnCount();
    }

    private int getSplitColumn() {
        return sheet == null ? 0 : sheet.getSplitColumn();
    }

    private int getSplitRow() {
        return sheet == null ? 0 : sheet.getSplitRow();
    }

    @Override
    public Sheet getSheet() {
        return sheet;
    }

    @Override
    public void setSheet(Sheet sheet) {
        if (this.sheet != null) {
            this.sheet.removePropertyChangeListener(this);
        }

        if (sheet != this.sheet) {
            Sheet oldSheet = this.sheet;
            this.sheet = sheet;

            if (this.sheet != null) {
                sheet.addPropertyChangeListener(this);
            }

            pcs.firePropertyChange(PROPERTY_SHEET, oldSheet, sheet);
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() != sheet) {
            return;
        }

        // FIXME
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

}
