package com.dua3.meja.ui;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.SheetEvent;
import com.dua3.utility.concurrent.AutoLock;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.MathUtil;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Scale2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.ui.Graphics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
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
    public static final int DEFAULT_SELECTION_STROKE_WIDTH = 2;

    private final SheetView owner;

    private boolean layoutChanged = true;

    /**
     * The sheet displayed.
     */
    private final Sheet sheet;

    /**
     * Horizontal padding.
     */
    private static final float PADDING_X_IN_POINTS = 2;

    /**
     * Vertical padding.
     */
    private static final float PADDING_Y_IN_POINTS = 1;

    /**
     * Color used to draw the selection rectangle.
     */
    private Color selectionColor = Color.GREEN;

    /**
     * Width of the selection rectangle borders.
     */
    private float selectionStrokeWidth = DEFAULT_SELECTION_STROKE_WIDTH;

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
    private float columnLabelHeightInPoints;
    private float defaultRowHeightInPoints = 12.0f; // TODO get from workbook
    private Font labelFont = FontUtil.getInstance().getDefaultFont().withSize(8);
    private Color labelBackgroundColor = Color.WHITESMOKE;
    private Color labelBorderColor = labelBackgroundColor.darker();
    private float labelBorderWidthInPixels = 1.0f;
    private float pixelWidthInPoints = 1.0f;
    private float pixelHeightInPoints = 1.0f;
    private int splitColumn;
    private int splitRow;

    /**
     * Retrieves the read lock for the underlying sheet.
     *
     * @return the read lock instance associated with the underlying sheet
     */
    public Lock readLock() {
        return sheet.readLock();
    }

    /**
     * Acquires an automatic read lock on the underlying sheet, ensuring the lock is held while
     * the returned {@link AutoLock} instance is in use, and releases it automatically when the
     * instance is closed.
     *
     * @return an {@link AutoLock} instance representing the read lock on the sheet
     */
    public AutoLock automaticReadLock() {
        return AutoLock.of(sheet.readLock(), () -> "readLock() [%s]".formatted(sheet.getSheetName()));
    }

    /**
     * Retrieves the write lock for the underlying sheet.
     *
     * @return the write lock instance associated with the underlying sheet
     */
    public Lock writeLock() {
        return sheet.writeLock();
    }

    /**
     * Acquires a write lock on the sheet and returns an AutoLock instance that
     * manages the lock with an associated description.
     *
     * @return an AutoLock instance that wraps the write lock of the sheet and
     *         provides a textual description of the lock, including the sheet name.
     */
    public AutoLock automaticWriteLock() {
        return AutoLock.of(sheet.writeLock(), () -> "writeLock() [%s]".formatted(sheet.getSheetName()));
    }

    public void update(int dpi) {
        try (var __ = automaticWriteLock()) {
            setDisplayScale(getDisplayScale());
            setScale(new Scale2f(sheet.getZoom() * dpi / 72.0f));
            setRowCount(sheet.getRowCount());
            setColumnCount(sheet.getColumnCount());
            setSplitRow(sheet.getSplitRow());
            setSplitColumn(sheet.getSplitColumn());
            updateLayout();
        }
    }

    /**
     * Retrieves the height of the sheet in points.
     *
     * @return the height of the sheet represented in points
     */
    public float getSheetHeightInPoints() {
        return sheetHeightInPoints;
    }

    /**
     * Retrieves the width of the sheet in points.
     *
     * @return the width of the sheet in points
     */
    public float getSheetWidthInPoints() {
        return sheetWidthInPoints;
    }

    /**
     * Get dx-coordinate of split.
     *
     * @return dx coordinate of split
     */
    public float getSplitX() {
        return getColumnPos(getSplitColumn());
    }

    /**
     * Get dy-coordinate of split.
     *
     * @return dy coordinate of split
     */
    public float getSplitY() {
        return getRowPos(getSplitRow());
    }

    /**
     * Retrieves the color used for highlighting the selection in the sheet view.
     *
     * @return the {@link Color} instance representing the selection highlight color
     */
    protected Color getSelectionColor() {
        return selectionColor;
    }

    /**
     * Sets the color used to indicate the selection in the sheet view.
     *
     * @param selectionColor the {@link Color} to be used for the selection highlight
     */
    protected void setSelectionColor(Color selectionColor) {
        this.selectionColor = selectionColor;
    }

    /**
     * Retrieves the stroke width used for the selection outline.
     *
     * @return the stroke width of the selection rectangle as a floating-point value
     */
    public float getSelectionStrokeWidth() {
        return selectionStrokeWidth;
    }

    /**
     * Flow-API {@link java.util.concurrent.Flow.Subscription} instance.
     */
    private Flow.@Nullable Subscription subscription;

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
    private Scale2f scale = Scale2f.identity();
    /**
     * The scale used to calculate screen sizes dependent of display resolution.
     */
    private Scale2f displayScale;
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

    public SheetViewDelegate(Sheet sheet, SheetView owner) {
        this.sheet = sheet;
        this.owner = owner;
        this.displayScale = owner.getDisplayScale();
        updateLayout();
        sheet.subscribe(this);
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

    public Cell getCellAt(float x, float y) {
        int i = getRowNumberFromY(y);
        int j = getColumnNumberFromX(x);
        return sheet.getCell(i, j);
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
        return columnPos[MathUtil.clamp(0, columnPos.length - 1, j)];
    }

    /**
     * Get the number of rows for the currently loaded sheet.
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
        if (i < rowPos.length) {
            return rowPos[i];
        } else {
            return rowPos[rowPos.length - 1] + (i - rowPos.length + 1) * getDefaultRowHeightInPoints();
        }
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
        LOG.trace("onNext() - {}", item);

        try (var __ = automaticWriteLock()) {
            switch (item.type()) {
                case SheetEvent.ZOOM_CHANGED, SheetEvent.LAYOUT_CHANGED, SheetEvent.ROWS_ADDED,
                     SheetEvent.COLUMNS_ADDED -> {
                    owner.updateContent();
                }
                case SheetEvent.SPLIT_CHANGED -> {
                    owner.updateContent();
                    owner.scrollToCurrentCell();
                }
                case SheetEvent.ACTIVE_CELL_CHANGED -> {
                    SheetEvent.ActiveCellChanged evt = (SheetEvent.ActiveCellChanged) item;

                    Cell oldCell = evt.oldValue();
                    Cell newCell = evt.newValue();

                    if (oldCell != null) {
                        owner.repaintCell(oldCell);
                    }
                    owner.scrollToCurrentCell();
                    if (newCell != null) {
                        owner.repaintCell(newCell);
                    }
                }
                case SheetEvent.CELL_VALUE_CHANGED, SheetEvent.CELL_STYLE_CHANGED -> {
                    owner.repaintCell(((SheetEvent.CellChanged<?>) item).cell());
                }
                default -> {
                }
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
        return sheet.getCurrentCell().getColumnNumber();
    }

    /**
     * Retrieves the current sheet associated with this view delegate.
     *
     * @return the currently loaded sheet
     */
    public Sheet getSheet() {
        return sheet;
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

    public void updateLayout() {
        if (!layoutChanged) {
            LOG.trace("updateLayout() - layout is clean, nothing to do");
            return;
        }

        LOG.trace("updateLayout()");
        try (var __ = automaticWriteLock()) {
            layoutChanged = false;

            this.pixelWidthInPoints = 1.0f / scale.sx();
            this.pixelHeightInPoints = 1.0f / scale.sy();

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
            // represent the highest row number, using only the digit '9'.
            String sMax = "9".repeat(String.valueOf(sheet.getLastRowNum()).length());
            Rectangle2f dim = calculateLabelDimension(sMax);
            rowLabelWidth = dim.width() + 2 * PADDING_X_IN_POINTS;
            columnLabelHeightInPoints = dim.height() + 2 * PADDING_Y_IN_POINTS;
        }
    }

    /**
     * Get the current row number.
     *
     * @return row number of the selected cell
     */
    public int getCurrentRowNum() {
        return getSheet().getCurrentCell().getRowNumber();
    }

    public Color getGridColor() {
        return gridColor;
    }

    public void setGridColor(Color gridColor) {
        LOG.trace("setGridColor({})", gridColor);
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
        LOG.trace("setEditable({})", editable);
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
        LOG.trace("setEditing({})", editing);
        this.editing = editing;
    }

    public void setColumnNames(IntFunction<String> columnNames) {
        LOG.trace("setColumnNames()");
        try (var __ = automaticWriteLock()) {
            this.columnNames = columnNames;
            markLayoutChanged();
        }
    }

    public int getSplitColumn() {
        return splitColumn;
    }

    public int getSplitRow() {
        return splitRow;
    }

    public boolean setCurrentCell(int i, int j) {
        LOG.trace("setCurrentCell({}, {})", i, j);
        try (var __ = automaticWriteLock()) {
            return sheet.setCurrentCell(i, j);
        }
    }

    public boolean setCurrentCell(Cell cell) {
        LOG.trace("setCurrentCell({})", cell::getCellRef);
        try (var __ = automaticWriteLock()) {
            return sheet.setCurrentCell(cell);
        }
    }

    public void setRowNames(IntFunction<String> rowNames) {
        LOG.trace("setRowNames()");
        try (var __ = automaticWriteLock()) {
            this.rowNames = rowNames;
            markLayoutChanged();
        }
    }

    public Scale2f getScale() {
        return scale;
    }

    public void setScale(Scale2f scale) {
        LOG.trace("setScale({})", scale);
        try (var __ = automaticReadLock()) {
            if (!scale.equals(this.scale)) {
                this.scale = scale;
                markLayoutChanged();
            }
        }
    }

    public void setDisplayScale(Scale2f displayScale) {
        LOG.trace("setDisplayScale({})", displayScale);
        try (var __ = automaticReadLock()) {
            if (!displayScale.equals(this.displayScale)) {
                this.displayScale = displayScale;
                markLayoutChanged();
            }
        }
    }

    private void markLayoutChanged() {
        LOG.trace("markLayoutChanged()");
        this.layoutChanged = true;
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        LOG.trace("setBackground({})", background);
        this.background = background;
    }

    public void move(Direction d) {
        LOG.trace("move({})", d);
        try (var __ = automaticWriteLock()) {
            Cell cell = getCurrentLogicalCell();
            switch (d) {
                case NORTH -> setCurrentRowNum(cell.getRowNumber() - 1);
                case SOUTH -> setCurrentRowNum(cell.getRowNumber() + cell.getVerticalSpan());
                case WEST -> setCurrentColNum(cell.getColumnNumber() - 1);
                case EAST -> setCurrentColNum(cell.getColumnNumber() + cell.getHorizontalSpan());
            }
        }
    }

    /**
     * Move the selection rectangle to an adjacent cell.
     *
     * @param d direction
     */
    public void movePage(Direction d) {
        LOG.trace("movePage()");
        try (var __ = automaticWriteLock()) {
            Cell cell = getCurrentLogicalCell();
            Rectangle2f cellRect = getCellRect(cell.getLogicalCell());
            switch (d) {
                case NORTH -> setCurrentRowNum(cell.getRowNumber() - 1);
                case SOUTH -> setCurrentRowNum(cell.getRowNumber() + cell.getVerticalSpan());
                case WEST -> setCurrentColNum(cell.getColumnNumber() - 1);
                case EAST -> setCurrentColNum(cell.getColumnNumber() + cell.getHorizontalSpan());
            }
        }
    }

    public Cell getCurrentLogicalCell() {
        return getSheet().getCurrentCell().getLogicalCell();
    }

    public void setCurrentColNum(int colNum) {
        LOG.trace("setCurrentColNum()");
        try (var __ = automaticWriteLock()) {
            int rowNum = sheet.getCurrentCell().getRowNumber();
            setCurrentCell(sheet.getCell(rowNum, Math.max(0, colNum)));
        }
    }

    public void setCurrentRowNum(int rowNum) {
        LOG.trace("setCurrentRowNum()");
        try (var __ = automaticWriteLock()) {
            int colNum = sheet.getCurrentCell().getColumnNumber();
            setCurrentCell(Math.max(0, rowNum), colNum);
       }
    }

    /**
     * Move the selection rectangle to the bottom right cell.
     */
    public void moveEnd() {
        LOG.trace("moveEnd()");
        try (var __ = automaticWriteLock()) {
        int row = sheet.getLastRowNum();
        int col = sheet.getLastColNum();
        setCurrentCell(row, col);
        }
    }

    /**
     * Move the selection rectangle to the top left cell.
     */
    public void moveHome() {
        LOG.trace("moveHome()");
        try (var __ = automaticWriteLock()) {
            int row = sheet.getFirstRowNum();
            int col = sheet.getFirstColNum();
            setCurrentCell(row, col);
        }
    }

    public void onMousePressed(Cell cell) {
        LOG.debug("onMousePressed({})", cell::getCellRef);

        // make the cell the current cell
        boolean currentCellChanged = setCurrentCell(cell);
        requestFocus();

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

    public void requestFocus() {
        LOG.trace("requestFocusInWindow()");
        owner.focusView();
    }

    /**
     * Get the horizontal padding.
     *
     * @return horizontal padding
     */
    public float getPaddingX() {
        return PADDING_X_IN_POINTS;
    }

    /**
     * Get the vertical padding.
     *
     * @return vertical padding
     */
    public float getPaddingY() {
        return PADDING_Y_IN_POINTS;
    }

    protected Rectangle2f calculateLabelDimension(String text) {
        return FontUtil.getInstance().getTextDimension(text, getLabelFont());
    }

    /**
     * Get the label font.
     *
     * @return the width of row labels in points
     */
    public Font getLabelFont() {
        return labelFont;
    }

    public void setLabelFont(Font labelFont) {
        LOG.trace("setLabelFont()");
        try (var __ = automaticWriteLock()) {
            this.labelFont = labelFont;
            markLayoutChanged();
        }
    }

    /**
     * Get the width of Row labels in points.
     *
     * @return the width of row labels in points
     */
    public float getRowLabelWidthInPoints() {
        return rowLabelWidth;
    }

    /**
     * Get the height of column labels in points.
     *
     * @return the height of cloumn labels in points
     */
    public float getColumnLabelHeightInPoints() {
        return columnLabelHeightInPoints;
    }

    public float getRowHeightInPoints(int i) {
        return i + 1 < rowPos.length ? rowPos[i + 1] - rowPos[i] : defaultRowHeightInPoints;
    }

    public float getColumnLabelHeightInPixels() {
        return columnLabelHeightInPoints * scale.sy();
    }

    public float getColumnWidthInPoints(int j) {
        return columnPos[j + 1] - columnPos[j];
    }

    public float getDefaultRowHeightInPoints() {
        return defaultRowHeightInPoints;
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

    public float getLabelBorderWidthInPixels() {
        return labelBorderWidthInPixels;
    }

    public float getLabelBorderWidthInPoints() {
        return labelBorderWidthInPixels * get1PxWidthInPoints();
    }

    public void setLabelBorderWidthInPixels(float labelBorderWidthInPixels) {
        this.labelBorderWidthInPixels = labelBorderWidthInPixels;
    }

    public AffineTransformation2f getTransformation() {
        return AffineTransformation2f.scale(getScale());
    }

    public float get1PxWidthInPoints() {
        return pixelWidthInPoints;
    }

    public float get1PxHeightInPoints() {
        return pixelHeightInPoints;
    }

    public float getDefaultRowHeightInPixels() {
        return getScale().sy() * defaultRowHeightInPoints;
    }

    public Scale2f getDisplayScale() {
        return displayScale;
    }

    /**
     * Returns the area tapen up by the sheet and decorations in view coordinates.
     * @return the total area used to display the sheet, and its labels
     */
    public Rectangle2f getTotalArea() {
        float labelHeight = getColumnLabelHeightInPoints();
        float labelWidth = getRowLabelWidthInPoints();
        float x = -labelWidth;
        float y = -labelHeight;
        float w = labelWidth + getSheetWidthInPoints();
        float h = labelHeight + getSheetHeightInPoints();
        return new Rectangle2f(x, y, w, h);
    }

    public void drawLabel(Graphics g, Rectangle2f r, String text) {
        g.setFill(getLabelBackgroundColor());
        g.fillRect(r);

        g.setStroke(getLabelBorderColor(), getLabelBorderWidthInPixels() * get1PxWidthInPoints());
        g.strokeRect(r);

        g.setFont(getLabelFont());
        g.drawText(text, r.xCenter(), r.yCenter(), Graphics.HAnchor.CENTER, Graphics.VAnchor.MIDDLE);
    }

    public SheetView.SheetArea getSheetArea(Rectangle2f r) {
        return new SheetView.SheetArea(
                Math.max(0, getRowNumberFromY(r.yMin())),
                Math.max(0, getColumnNumberFromX(r.xMin())),
                getRowNumberFromY(r.yMax()) + 1,
                getColumnNumberFromX(r.xMax()) + 1
        );
    }

    public void setRowCount(int rowCount) {
        if (rowCount != rowPos.length - 1) {
            markLayoutChanged();
        }
    }

    public void setColumnCount(int columnCount) {
        if (columnCount != columnPos.length - 1) {
            markLayoutChanged();
        }
    }

    public void setSplitRow(int i) {
        if (i != splitRow) {
            splitRow = i;
            markLayoutChanged();
        }
    }

    public void setSplitColumn(int j) {
        if (j != splitColumn) {
            splitColumn = j;
            markLayoutChanged();
        }
    }
}