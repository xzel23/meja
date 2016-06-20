/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dua3.meja.ui.controlsfx;

import com.dua3.meja.model.Cell;
import com.sun.javafx.event.EventHandlerManager;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;

/**
 *
 * @author a5xysq1
 */
public class SpreadSheetCellImpl implements SpreadsheetCell {

    private final Cell cell;
    private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);

    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>();

    private final StringProperty STYLE_PROPERTY = new StringPropertyBase() {
        @Override
        public Object getBean() {
            return cell;
        }

        @Override
        public String getName() {
            return "style";
        }
    };

    private final ReadOnlyStringProperty TEXT_PROPERTY = new ReadOnlyStringPropertyBase() {
        @Override
        public String get() {
            return cell.toString();
        }

        @Override
        public Object getBean() {
            return cell;
        }

        @Override
        public String getName() {
            return "text";
        }
    };

    public SpreadSheetCellImpl(Cell cell) {
        this.cell = cell;
    }

    @Override
    public boolean match(SpreadsheetCell cell) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setItem(Object value) {
        cell.set(value);
    }

    @Override
    public Object getItem() {
        return cell.get();
    }

    @Override
    public ObjectProperty<Object> itemProperty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isEditable() {
        // FIXME
        return true;
    }

    @Override
    public void setEditable(boolean editable) {
        // FIXME
    }

    @Override
    public boolean isWrapText() {
        return cell.getCellStyle().isWrap();
    }

    @Override
    public void setWrapText(boolean wrapText) {
        cell.getCellStyle().setWrap(wrapText);
    }

    @Override
    public void setStyle(String style) {
        cell.setStyle(style);
    }

    @Override
    public String getStyle() {
        return cell.getCellStyle().getName();
    }

    @Override
    public StringProperty styleProperty() {
        return STYLE_PROPERTY;
    }

    @Override
    public void activateCorner(CornerPosition position) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deactivateCorner(CornerPosition position) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isCornerActivated(CornerPosition position) {
        // FIXME
        return false;
    }

    @Override
    public StringProperty formatProperty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getFormat() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setFormat(String format) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ReadOnlyStringProperty textProperty() {
        return TEXT_PROPERTY;
    }

    @Override
    public String getText() {
        return cell.toString();
    }

    @Override
    public SpreadsheetCellType getCellType() {
        switch (cell.getCellType()) {
            case NUMERIC:
                return SpreadsheetCellType.DOUBLE;
            default:
                return SpreadsheetCellType.OBJECT;
        }
    }

    @Override
    public int getRow() {
        return cell.getRowNumber();
    }

    @Override
    public int getColumn() {
        return cell.getColumnNumber();
    }

    @Override
    public int getRowSpan() {
        return cell.getVerticalSpan();
    }

    @Override
    public void setRowSpan(int rowSpan) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getColumnSpan() {
        return cell.getHorizontalSpan();
    }

    @Override
    public void setColumnSpan(int columnSpan) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObservableSet<String> getStyleClass() {
        // FIXME
        return FXCollections.emptyObservableSet();
    }

    @Override
    public ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    @Override
    public void setGraphic(Node graphic) {
        this.graphic.setValue(graphic);
    }

    @Override
    public Node getGraphic() {
        return graphic.getValue();
    }

    @Override
    public Optional<String> getTooltip() {
        return Optional.empty(); // FIXME
    }

    @Override
    public void addEventHandler(EventType<Event> eventType, EventHandler<Event> eventHandler) {
         eventHandlerManager.addEventHandler(eventType, eventHandler);
    }

    @Override
    public void removeEventHandler(EventType<Event> eventType, EventHandler<Event> eventHandler) {
         eventHandlerManager.removeEventHandler(eventType, eventHandler);
    }

}
