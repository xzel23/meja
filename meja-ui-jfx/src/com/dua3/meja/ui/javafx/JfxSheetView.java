/*
 *
 */
package com.dua3.meja.ui.javafx;

import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SheetView;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

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

        headerTopLeft = new Label("1");
        columnHeaderLeft = new ColumnHeader(this, () -> 0, () -> getSplitColumn());
        columnHeaderRight = new ColumnHeader(this, () -> getSplitColumn(), () -> getColumnCount());
        rowHeaderTop = new Label("4");
        leftTopChart = new Label("5");
        rightTopChart = new Label("6");
        rowHeaderBottom = new Label("7");
        leftBottomChart = new Label("8");
        rightBottomChart = new Label("9");

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

    @Override
    public Sheet getSheet() {
        return sheet;
    }

    @Override
    public void setSheet(Sheet sheet) {
        if (this.sheet!=null) {
            this.sheet.removePropertyChangeListener(this);
        }

        if (sheet != this.sheet) {
            Sheet oldSheet = this.sheet;
            this.sheet = sheet;

            if (this.sheet!=null) {
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
