package com.dua3.meja.ui;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.SheetEvent;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.concurrent.locks.Lock;
import java.util.function.IntFunction;

/**
 * A delegate that combines basic data and functionality for SheetView implementations.
 * <p>
 * A delegate is used instead of an abstract base class because user interface components might have to be derived from
 * existing UI classes.
 */
public abstract class SheetViewDelegate implements Flow.Subscriber<SheetEvent> {
    private static final Logger LOG = LogManager.getLogger(SheetViewDelegate.class);

    private final SheetView owner;

    /**
     * The sheet displayed.
     */
    private transient Sheet sheet;

    /**
     * Horizontal padding.
     */
    private static final float PADDING_X = 2;

    /**
     * Vertical padding.
     */
    private static final float PADDING_Y = 1;

    /**
     * Color used to draw the selection rectangle.
     */
    private Color selectionColor = Color.GREEN;

    /**
     * Width of the selection rectangle borders.
     */
    private float SELECTION_STROKE_WIDTH = 2;

    /**
     * Array with column positions (dx-axis) in pixels.
     */
    private float[] columnPos = {0};

    /**
     * Array with column positions (dy-axis) in pixels.
     */
    private float[] rowPos = {0};

    private float sheetHeightInPoints;

    private float sheetWidthInPoints;
    private float rowLabelWidth;
    private float columnLabelHeight;
    private float defaultRowHeight = 12f;
    private Font labelFont = new Font().withSize(8);
    private Color labelBackgroundColor = Color.WHITESMOKE;
    private Color labelBorderColor = labelBackgroundColor.darker();

    public float getSheetHeightInPoints() {
        return sheetHeightInPoints;
    }

    public float getSheetWidthInPoints() {
        return sheetWidthInPoints;
    }

    /**
     * Get dx-coordinate of split.
     *
     * @return dx coordinate of split
     */
    public float getSplitX() {
        return getColumnPos(sheet.getSplitColumn());
    }

    /**
     * Get dy-coordinate of split.
     *
     * @return dy coordinate of split
     */
    public float getSplitY() {
        return getRowPos(sheet.getSplitRow());
    }

    protected Color getSelectionColor() {
        return selectionColor;
    }

    protected void setSelectionColor(Color selectionColor) {
        this.selectionColor = selectionColor;
    }

    protected float getSelectionStrokeWidth() {
        return SELECTION_STROKE_WIDTH;
    }

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
    private float scale = 1f;
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

    public SheetViewDelegate(SheetView owner) {
        this.owner = owner;
        updateLayout();
    }

    /**
     * Get number of columns for the currently loaded sheet.
     *
     * @return number of columns
     */
    public int getColumnCount() {
        return columnPos.length - 1;
    }

    /**
     * Get the column number that the given dx-coordinate belongs to.
     *
     * @param x dx-coordinate
     * @return <ul>
     *         <li>-1, if the first column is displayed to the right of the given
     *         coordinate
     *         <li>number of columns, if the right edge of the last column is
     *         displayed to the left of the given coordinate
     *         <li>the number of the column that belongs to the given coordinate
     *         </ul>
     */
    public int getColumnNumberFromX(double x) {
        return getPositionIndexFromCoordinate(columnPos, x, sheetWidthInPoints);
    }

    public int getColumnNumberFromX(float x) {
        return getPositionIndexFromCoordinate(columnPos, x, sheetWidthInPoints);
    }

    /**
     * Get the row number that the given dy-coordinate belongs to.
     *
     * @param y dy-coordinate
     * @return <ul>
     *         <li>-1, if the first row is displayed below the given coordinate
     *         <li>number of rows, if the lower edge of the last row is displayed
     *         above the given coordinate
     *         <li>the number of the row that belongs to the given coordinate
     *         </ul>
     */
    public int getRowNumberFromY(double y) {
        return getPositionIndexFromCoordinate(rowPos, y, sheetHeightInPoints);
    }

    public int getRowNumberFromY(float y) {
        return getPositionIndexFromCoordinate(rowPos, y, sheetHeightInPoints);
    }

    private int getPositionIndexFromCoordinate(float[] positions, double coord, float sizeInPoints) {
        if (positions.length == 0) {
            return 0;
        }

        // guess position
        int j = (int) (positions.length * coord / sizeInPoints);
        if (j < 0) {
            j = 0;
        } else if (j >= positions.length) {
            j = positions.length - 1;
        }

        // linear search from here
        if (positions[Math.min(positions.length - 1, j)] > coord) {
            while (j > 0 && positions[j - 1] > coord) {
                j--;
            }
        } else {
            while (j < positions.length && positions[Math.min(positions.length - 1, j)] <= coord) {
                j++;
            }
        }

        return j - 1;
    }

    /**
     * @param j the column number
     * @return the columnPos
     */
    public float getColumnPos(int j) {
        return columnPos[Math.min(columnPos.length - 1, j)];
    }

    /**
     * Get number of rows for the currently loaded sheet.
     *
     * @return number of rows
     */
    public int getRowCount() {
        return rowPos.length - 1;
    }

    /**
     * @param i the row number
     * @return the rowPos
     */
    public float getRowPos(int i) {
        return rowPos[Math.min(rowPos.length - 1, i)];
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

    /**
     * Calculate the rectangle the cell occupies on screen.
     *
     * @param cell the cell whose area is requested
     * @return the rectangle the cell takes up in screen coordinates
     */
    public Rectangle2f getCellRect(Cell cell) {
        final int i = cell.getRowNumber();
        final int j = cell.getColumnNumber();

        final float x = getColumnPos(j);
        final float w = getColumnPos(j + cell.getHorizontalSpan()) - x;
        final float y = getRowPos(i);
        final float h = getRowPos(i + cell.getVerticalSpan()) - y;

        return new Rectangle2f(x, y, w, h);
    }

    public void setSheet(@Nullable Sheet sheet) {
        //noinspection ObjectEquality
        if (sheet != this.sheet) {
            if (this.subscription != null) {
                this.subscription.cancel();
                this.subscription = null;
            }

            this.sheet = sheet;

            updateLayout();
            owner.updateContent();

            LOG.debug("sheet changed");
        }
    }

    private void updateLayout() {
        if (sheet == null) {
            sheetWidthInPoints = 0;
            sheetHeightInPoints = 0;
            rowPos = new float[]{0};
            columnPos = new float[]{0};
        } else {
            Lock lock = sheet.readLock();
            lock.lock();
            try {
                // determine row and column positions
                sheetHeightInPoints = 0;
                rowPos = new float[2 + sheet.getLastRowNum()];
                rowPos[0] = 0;
                for (int i = 1; i < rowPos.length; i++) {
                    sheetHeightInPoints += sheet.getRowHeight(i - 1);
                    rowPos[i] = sheetHeightInPoints;
                }

                sheetWidthInPoints = 0;
                columnPos = new float[2 + sheet.getLastColNum()];
                columnPos[0] = 0;
                for (int j = 1; j < columnPos.length; j++) {
                    sheetWidthInPoints += sheet.getColumnWidth(j - 1);
                    columnPos[j] = sheetWidthInPoints;
                }

                // create a string with the maximum number of digits needed to
                // represent the highest row number, using only the digit 9.
                String sMax = "9".repeat(String.valueOf(getRowLabelWidth()).length());
                Rectangle2f dim = calculateLabelDimension(sMax);
                rowLabelWidth = dim.width() + 2 * PADDING_X;
                columnLabelHeight = dim.height() + 2 * PADDING_Y;

                // subscribe to the Flow API
                this.sheet.subscribe(this);
            } finally {
              lock.unlock();
            }
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

    public void onMousePressed(int x, int y) {
        // make the cell under pointer the current cell
        int row = getRowNumberFromY(yD2S(y));
        int col = getColumnNumberFromX(xD2S(x));
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
     * Get the horizontal padding.
     *
     * @return horizontal padding
     */
    public float getPaddingX() {
        return PADDING_X;
    }

    /**
     * Get the vertical padding.
     *
     * @return vertical padding
     */
    public float getPaddingY() {
        return PADDING_Y;
    }

    protected Rectangle2f calculateLabelDimension(String text) {
        return FontUtil.getInstance().getTextDimension(text, getLabelFont());
    }

    public Font getLabelFont() {
        return labelFont;
    }

    public void setLabelFont(Font labelFont) {
        this.labelFont = labelFont;
    }

    public float getRowLabelWidth() {
        return rowLabelWidth;
    }

    public float getColumnLabelHeight() {
        return columnLabelHeight;
    }

    public float getRowHeightInPoints(int i) {
        return rowPos[i+1] - rowPos[i];
    }

    public float getColumnWidthInPoints(int j) {
        return columnPos[j+1] - columnPos[j];
    }

    public double getDefaultRowHeight() {
        return defaultRowHeight;
    }

    public Locale getLocale() {
        return owner.getLocale();
    }

    public Color getLabelBorderColor() {
        return labelBorderColor;
    }

    public void setLabelBorderColor(Color labelBorderColor) {
        this.labelBorderColor = labelBorderColor;
    }

    public Color getLabelBackgroundColor() {
        return labelBackgroundColor;
    }

    public void setLabelBackgroundColor(Color labelBackgroundColor) {
        this.labelBackgroundColor = labelBackgroundColor;
    }
}