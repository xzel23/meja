/*
 *
 */
package com.dua3.meja.ui.javafx;

import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SheetView;
import com.dua3.meja.util.MejaHelper;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.function.IntSupplier;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

/**
 *
 * @author axel
 */
public class JfxSheetView extends Control implements SheetView, PropertyChangeListener {

    class RowHeader extends HBox implements PropertyChangeListener {

        private final IntSupplier firstColumn;
        private final IntSupplier lastColumn;

        RowHeader(IntSupplier firstColumn, IntSupplier lastColumn) {
            this.firstColumn=firstColumn;
            this.lastColumn=lastColumn;

            JfxSheetView.this.addPropertyChangeListener(this);

            update();
        }

        public int getFirstColumn() {
            return firstColumn.getAsInt();
        }

        public int getLastColumn() {
            return lastColumn.getAsInt();
        }

        private void update() {
            final ObservableList<Node> children = getChildren();

            // make sure we have a label for every column
            int n = getLastColumn()-getFirstColumn();
            if (n<children.size()) {
                children.remove(n, getChildren().size());
            } else if (n>children.size()) {
                for (int j=getFirstColumn()+children.size(); j<n; j++) {
                    children.add(new Label(MejaHelper.getColumnName(j)));
                }
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            assert evt.getSource()==JfxSheetView.this;
            if (evt.getPropertyName().equals(PROPERTY_SHEET)) {
                update();
            }
        }
    }

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Sheet sheet = null;
    private final Node headerTopLeft, columnHeaderLeft, columnHeaderRight,
            rowHeaderTop, leftTopChart, rightTopChart,
            rowHeaderBottom, leftBottomChart, rightBottomChart;

    public JfxSheetView() {
        final GridPane gridPane = new GridPane();

        headerTopLeft = new Pane();
        columnHeaderLeft = new RowHeader(() -> 0, () -> getSplitColumn());
        columnHeaderRight = new RowHeader(() -> getSplitColumn(), () -> getColumnCount());
        rowHeaderTop = new Pane();
        leftTopChart = new Pane();
        rightTopChart = new Pane();
        rowHeaderBottom = new Pane();
        leftBottomChart = new Pane();
        rightBottomChart = new Pane();

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
