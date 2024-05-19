package com.dua3.meja.ui;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.SheetEvent;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * A delegate that combines basic data and functionality for SheetView implementations.
 * <p>
 * A delegate is used instead of an abstract base class because user interface components might have to be derived from
 * existing UI classes.
 */
public abstract class SheetViewDelegate<GC, R> implements Flow.Subscriber<SheetEvent> {
    private static final Logger LOG = LogManager.getLogger(SheetViewDelegate.class);

    private final SheetView owner;
    private final SheetPainterBase<GC, R> sheetPainter;

    /**
     * Flow-API {@link java.util.concurrent.Flow.Subscription} instance.
     */
    private Flow.Subscription subscription;

    /**
     * Function that provides the column names.
     */
    private transient IntFunction<String> columnNames = Sheet::getColumnName;
    /**
     * Function that provides the row names.
     */
    private transient IntFunction<String> rowNames = Sheet::getRowName;
    /**
     *  Rhe background color of the sheet.
     */
    private Color background = Color.WHITE;
    /**
     * The scale used to calculate screen sizes dependent of display resolution.
     */
    private float scale = 1.0f;
    /**
     * The sheet displayed.
     */
    private transient Sheet sheet;
    /**
     * The color to use for the grid lines.
     */
    private transient Color gridColor = Color.LIGHTGRAY;
    /**
     * Read-only mode.
     */
    private boolean editable;
    /**
     * Editing state.
     */
    private boolean editing;

    public SheetViewDelegate(SheetView owner, Function<? super SheetViewDelegate<GC,R>, ? extends SheetPainterBase<GC, R>> sheetPainterFactory) {
        this.owner = owner;
        this.sheetPainter = sheetPainterFactory.apply(this);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        if (this.subscription != null) {
            this.subscription.cancel();
        }
        this.subscription = subscription;
        this.subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(SheetEvent item) {
        switch (item.type()) {
            case SheetEvent.ZOOM_CHANGED, SheetEvent.LAYOUT_CHANGED, SheetEvent.ROWS_ADDED -> owner.updateContent();
            case SheetEvent.SPLIT_CHANGED -> {
                owner.updateContent();
                owner.scrollToCurrentCell();
            }
            case SheetEvent.ACTIVE_CELL_CHANGED -> {
                SheetEvent.ActiveCellChanged evt = (SheetEvent.ActiveCellChanged) item;
                owner.repaintCell(evt.valueOld());
                owner.scrollToCurrentCell();
                owner.repaintCell(evt.valueNew());
            }
            case SheetEvent.CELL_VALUE_CHANGED, SheetEvent.CELL_STYLE_CHANGED -> {
                owner.repaintCell(((SheetEvent.CellChanged<?>) item).cell());
            }
            default -> {
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
        LOG.error("error with subscription", throwable);
    }

    @Override
    public void onComplete() {
        LOG.debug("subscription completed");
        this.subscription = null;
    }

    public String getColumnName(int j) {
        return columnNames.apply(j);
    }

    /**
     * Get the current column number.
     *
     * @return column number of the selected cell
     */
    public int getCurrentColNum() {
        return getSheet().flatMap(Sheet::getCurrentCell).map(Cell::getColumnNumber).orElse(0);
    }

    public Optional<Sheet> getSheet() {
        return Optional.ofNullable(sheet);
    }

    public void setSheet(@Nullable Sheet sheet) {
        //noinspection ObjectEquality
        if (sheet != this.sheet) {
            if (this.subscription != null) {
                this.subscription.cancel();
                this.subscription = null;
            }

            this.sheet = sheet;
            LOG.debug("sheet changed");

            if (this.sheet != null) {
                this.sheet.subscribe(this);
            }

            owner.updateContent();
        }
    }

    /**
     * Get the current row number.
     *
     * @return row number of the selected cell
     */
    public int getCurrentRowNum() {
        return getSheet().flatMap(Sheet::getCurrentCell).map(Cell::getRowNumber).orElse(0);
    }

    public Color getGridColor() {
        return gridColor;
    }

    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
    }

    public String getRowName(int i) {
        return rowNames.apply(i);
    }

    public int getColumnCount() {
        return sheet == null ? 0 : 1 + sheet.getLastColNum();
    }

    public int getRowCount() {
        return sheet == null ? 0 : 1 + sheet.getLastRowNum();
    }


    /**
     * Check whether editing is enabled.
     *
     * @return true if this SwingSheetView allows editing.
     */
    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    /**
     * Check editing state.
     *
     * @return true, if a cell is being edited.
     */
    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    public void setColumnNames(IntFunction<String> columnNames) {
        this.columnNames = columnNames;
    }

    public int getSplitColumn() {
        return sheet == null ? 0 : sheet.getSplitColumn();
    }

    public int getSplitRow() {
        return sheet == null ? 0 : sheet.getSplitRow();
    }

    public boolean setCurrentCell(int rowNum, int colNum) {
        if (sheet == null) {
            return false;
        }

        Cell oldCell = sheet.getCurrentCell().orElse(null);
        int newRowNum = Math.max(sheet.getFirstRowNum(), Math.min(sheet.getLastRowNum(), rowNum));
        int newColNum = Math.max(sheet.getFirstColNum(), Math.min(sheet.getLastColNum(), colNum));
        sheet.setCurrentCell(newRowNum, newColNum);

        return sheet.getCurrentCell().orElse(null) != oldCell;
    }

    public void setRowNames(IntFunction<String> rowNames) {
        this.rowNames = rowNames;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public final float hD2S(float h) {
        return h / scale;
    }

    public final float hS2D(float h) {
        return scale * h;
    }

    public final int hS2Di(float h) {
        return Math.round(hS2D(h));
    }
    
    public final float wD2S(float w) {
        return w / scale;
    }

    public final float wS2D(float w) {
        return scale * w;
    }

    public final int wS2Di(float w) {
        return Math.round(wS2D(w));
    }
    
    public final float xD2S(float x) {
        return x / scale;
    }

    public final float xS2D(float x) {
        return Math.round(scale * x);
    }

    public final int xS2Di(float x) {
        return Math.round(xS2D(x));
    }

    public final float yD2S(float y) {
        return y / scale;
    }

    public final float yS2D(float y) {
        return scale * y;
    }
    
    public final int yS2Di(float y) {
        return Math.round(yS2D(y));
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public void move(Direction d) {
        getCurrentLogicalCell().ifPresent(cell -> {
            switch (d) {
                case NORTH -> setCurrentRowNum(cell.getRowNumber() - 1);
                case SOUTH -> setCurrentRowNum(cell.getRowNumber() + cell.getVerticalSpan());
                case WEST -> setCurrentColNum(cell.getColumnNumber() - 1);
                case EAST -> setCurrentColNum(cell.getColumnNumber() + cell.getHorizontalSpan());
            }
        });
    }

    public Optional<Cell> getCurrentLogicalCell() {
        return getSheet().flatMap(Sheet::getCurrentCell).map(Cell::getLogicalCell);
    }

    public void setCurrentColNum(int colNum) {
        getSheet().ifPresent(sheet -> {
            int rowNum = sheet.getCurrentCell().map(Cell::getRowNumber).orElse(0);
            setCurrentCell(rowNum, colNum);
        });
    }

    public void setCurrentRowNum(int rowNum) {
        getSheet().ifPresent(sheet -> {
            int colNum = sheet.getCurrentCell().map(Cell::getColumnNumber).orElse(0);
            setCurrentCell(rowNum, colNum);
        });
    }

    /**
     * Move the selection rectangle to the bottom right cell.
     */
    public void moveEnd() {
        getSheet().ifPresent(sheet -> {
            int row = sheet.getLastRowNum();
            int col = sheet.getLastColNum();
            setCurrentCell(row, col);
        });
    }

    /**
     * Move the selection rectangle to the top left cell.
     */
    public void moveHome() {
        getSheet().ifPresent(sheet -> {
            int row = sheet.getFirstRowNum();
            int col = sheet.getFirstColNum();
            setCurrentCell(row, col);
        });
    }

    /**
     * Move the selection rectangle to an adjacent cell.
     *
     * @param d direction
     */
    public void movePage(Direction d) {
        move(d); // TODO
    }

    public SheetPainterBase<GC, R> getSheetPainter() {
        return sheetPainter;
    }

    public abstract Rectangle2f rectD2S(R r);

    public abstract R rectS2D(Rectangle2f r);

    public void onMousePressed(int x, int y) {
        // make the cell under pointer the current cell
        int row = getSheetPainter().getRowNumberFromY(yD2S(y));
        int col = getSheetPainter().getColumnNumberFromX(xD2S(x));
        boolean currentCellChanged = setCurrentCell(row, col);
        requestFocusInWindow();

        if (currentCellChanged) {
            // if cell changed, stop cell editing
            if (isEditing()) {
                owner.stopEditing(true);
                setEditing(false);
            }
        } else {
            // otherwise start cell editing
            if (isEditable()) {
                owner.startEditing();
                setEditing(true);
            }
        }
    }

    public void requestFocusInWindow() {
        owner.requestFocusInWindow();
    }

    /**
     * Get x-coordinate of split.
     *
     * @return x coordinate of split
     */
    public float getSplitX() {
        return getSheet()
                .map(sheet -> getSheetPainter().getColumnPos(sheet.getSplitColumn()))
                .orElse(0f);
    }

    /**
     * Get y-coordinate of split.
     *
     * @return y coordinate of split
     */
    public float getSplitY() {
        return getSheet()
                .map(sheet -> getSheetPainter().getRowPos(sheet.getSplitRow()))
                .orElse(0f);
    }

}