package com.dua3.meja.ui;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.SheetEvent;
import com.dua3.utility.concurrent.AutoLock;
import com.dua3.utility.data.Color;
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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.IntFunction;

/**
 * A delegate that combines basic data and functionality for SheetView implementations.
 * <p>
 * A delegate is used instead of an abstract base class because user interface components might have to be derived from
 * existing UI classes.
 */
public abstract class SheetViewDelegate implements Flow.Subscriber<SheetEvent> {
    private static final Logger LOG = LogManager.getLogger(SheetViewDelegate.class);

    /**
     * The default stroke width used for selection outlines in graphical components or UI elements.
     * This value defines the thickness of the border drawn to indicate selection, measured in pixels.
     */
    public static final int DEFAULT_SELECTION_STROKE_WIDTH = 2;
    /**
     * Horizontal padding.
     */
    private static final float PADDING_X_IN_POINTS = 2;
    /**
     * Vertical padding.
     */
    private static final float PADDING_Y_IN_POINTS = 1;
    private static final int SPLIT_LINE_PIXELS = 1;
    private final SheetView owner;
    /**
     * The sheet displayed.
     */
    private final Sheet sheet;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    /**
     * Width of the selection rectangle borders.
     */
    private float selectionStrokeWidth = DEFAULT_SELECTION_STROKE_WIDTH;
    private boolean layoutChanged = true;
    /**
     * Color used to draw the selection rectangle.
     */
    private Color selectionColor = Color.GREEN;
    /**
     * Array with column positions (dx-axis) in points.
     */
    private float[] columnPos = {0};
    /**
     * Array with column positions (dy-axis) in points.
     */
    private float[] rowPos = {0};
    private float sheetHeightInPoints;
    private float sheetWidthInPoints;
    private float rowLabelWidth;
    private float columnLabelHeightInPoints;
    private Font labelFont = FontUtil.getInstance().getDefaultFont().withSize(8);
    private Color labelBackgroundColor = Color.WHITESMOKE;
    private Color labelBorderColor = labelBackgroundColor.darker();
    private float labelBorderWidthInPixels = 1.0f;
    private float pixelWidthInPoints = 1.0f;
    private float pixelHeightInPoints = 1.0f;
    private int splitColumn;
    private int splitRow;
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
     * Rhe background color of the sheet.
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

    /**
     * Creates a new SheetViewDelegate instance.
     * This delegate handles the common functionality for different sheet view implementations.
     * It initializes the view with the owner's display scale, updates the layout,
     * and subscribes to sheet events to receive notifications of changes.
     *
     * @param sheet the sheet model to be displayed and managed
     * @param owner the sheet view component that owns this delegate
     */
    protected SheetViewDelegate(Sheet sheet, SheetView owner) {
        this.sheet = sheet;
        this.owner = owner;
        this.displayScale = owner.getDisplayScale();
        updateLayout();
        sheet.subscribe(this);
    }

    /**
     * Updates the layout calculations for the entire sheet view.
     * This method is called when the layout is marked as changed and performs the following:
     * - Updates pixel-to-point conversion ratios based on current scale
     * - Calculates cumulative row positions and total sheet height
     * - Calculates cumulative column positions and total sheet width
     * - Computes row label width based on the maximum row number width
     * - Computes column label height based on font metrics
     * <p>
     * If the layout hasn't changed since the last update, the method returns early
     * to avoid unnecessary recalculations.
     */
    public void updateLayout() {
        try (var __ = writeLock("SheetViewDelegate.updateLayout()")) {
            if (!layoutChanged) {
                LOG.trace("updateLayout() - layout is clean, nothing to do");
                return;
            }
            layoutChanged = false;

            this.pixelWidthInPoints = 1.0f / scale.sx();
            this.pixelHeightInPoints = 1.0f / scale.sy();

            // determine row and column positions
            sheetHeightInPoints = 0;
            rowPos = new float[2 + sheet.getRowCount() - 1];
            rowPos[0] = 0;
            for (int i = 1; i < rowPos.length; i++) {
                sheetHeightInPoints += sheet.getRowHeight(i - 1);
                rowPos[i] = sheetHeightInPoints;
            }

            sheetWidthInPoints = 0;
            columnPos = new float[2 + sheet.getColumnCount() - 1];
            columnPos[0] = 0;
            for (int j = 1; j < columnPos.length; j++) {
                sheetWidthInPoints += sheet.getColumnWidth(j - 1);
                columnPos[j] = sheetWidthInPoints;
            }

            // create a string with the maximum number of digits needed to
            // represent the highest row number, using only the digit '9'.
            String sMax = "9".repeat(String.valueOf(sheet.getRowCount() - 1).length());
            Rectangle2f dim = FontUtil.getInstance().getTextDimension(sMax, getLabelFont());
            rowLabelWidth = dim.width() + 2 * PADDING_X_IN_POINTS;
            columnLabelHeightInPoints = dim.height() + 2 * PADDING_Y_IN_POINTS;
        }
    }

    /**
     * Acquires a write lock on the sheet and returns an AutoLock instance that
     * manages the lock with an associated description.
     *
     * @param name the name of the lock
     * @return an AutoLock instance that wraps the write lock of the sheet and
     * provides a textual description of the lock, including the sheet name.
     */
    public AutoLock writeLock(String name) {
        return AutoLock.of(lock.writeLock(), name);
    }

    /**
     * Gets the font used for rendering row and column labels in the sheet view.
     * This font determines the appearance of row numbers and column letters in
     * the sheet's header areas.
     *
     * @return the current font used for row and column labels
     */
    public Font getLabelFont() {
        return labelFont;
    }

    /**
     * Sets the font to be used for rendering row and column labels in the sheet view.
     * Changing this font affects the appearance of row numbers and column letters, and
     * may also affect the layout dimensions of the header areas.
     *
     * @param labelFont the new font to use for row and column labels. Must not be null.
     */
    public void setLabelFont(Font labelFont) {
        try (var __ = writeLock("SheetViewDelegate.setLabelFont()")) {
            this.labelFont = labelFont;
            markLayoutChanged();
        }
    }

    /**
     * Marks the layout as changed, indicating that a layout update is needed.
     * This method is thread-safe and uses a write lock to ensure proper
     * synchronization when multiple threads access the sheet view.
     * The actual layout update will be performed the next time updateLayout()
     * is called. The method logs a trace message for debugging purposes.
     */
    private void markLayoutChanged() {
        LOG.trace("markLayoutChanged()");
        try (var __ = writeLock("SheetViewDelegate.markLayoutChanged()")) {
            this.layoutChanged = true;
        }
    }

    /**
     * Acquires an automatic read lock on the underlying sheet, ensuring the lock is held while
     * the returned {@link AutoLock} instance is in use, and releases it automatically when the
     * instance is closed.
     *
     * @param name the name of the lock
     * @return an {@link AutoLock} instance representing the read lock on the sheet
     */
    public AutoLock readLock(String name) {
        return AutoLock.of(lock.readLock(), name);
    }

    /**
     * Updates the view's display settings based on the provided DPI (dots per inch).
     * This method performs a complete update of the view, including:
     * - Adjusting the display and zoom scales for the new DPI
     * - Updating row and column counts
     * - Updating split positions
     * - Recalculating the layout
     *
     * @param dpi the new dots per inch value for the display
     */
    public void update(int dpi) {
        LOG.trace("update - {} DPI", dpi);
        try (var __ = writeLock("SheetViewDelegate.update()")) {
            setDisplayScale(getDisplayScale());
            setScale(new Scale2f(sheet.getZoom() * dpi / 72.0f));
            onSetRowCount(sheet.getRowCount());
            onSetColumnCount(sheet.getColumnCount());
            setSplitRow(sheet.getSplitRow());
            setSplitColumn(sheet.getSplitColumn());
            updateLayout();
        }
    }

    /**
     * Gets the total width of the sheet in pixels.
     * This is calculated by applying the current pixel-to-point ratio to the
     * point-based width. The value changes when the display DPI changes.
     *
     * @return the width of the sheet in pixels
     */
    public float getSheetWidthInPixels() {
        return getSheetWidthInPoints() * pixelWidthInPoints;
    }

    /**
     * Gets the total width of the sheet in points (1/72 inch).
     * This represents the logical width of the sheet content, excluding headers
     * and other decorations.
     *
     * @return the width of the sheet in points
     */
    public float getSheetWidthInPoints() {
        return sheetWidthInPoints;
    }

    /**
     * Gets the total height of the sheet in pixels.
     * This is calculated by applying the current pixel-to-point ratio to the
     * point-based height. The value changes when the display DPI changes.
     *
     * @return the height of the sheet in pixels
     */
    public float getSheetHeightInPixels() {
        return getSheetHeightInPoints() * pixelHeightInPoints;
    }

    /**
     * Gets the total height of the sheet in points (1/72 inch).
     * This represents the logical height of the sheet content, excluding headers
     * and other decorations.
     *
     * @return the height of the sheet represented in points
     */
    public float getSheetHeightInPoints() {
        return sheetHeightInPoints;
    }

    /**
     * Gets the x-coordinate of the vertical split line in points (1/72 inch).
     * This coordinate is calculated as the left edge position of the split column.
     *
     * @return dx coordinate of split, measured from the left edge of the sheet
     */
    public float getSplitXInPoints() {
        return getColumnPos(getSplitColumn());
    }

    /**
     * Get the x-coordinate (in points) of the left edge of the specified column.
     *
     * @param j the column number (0-based)
     * @return the x-coordinate in points. For column numbers beyond the last column,
     * the position is calculated by extrapolating using the default column width.
     * Negative column numbers are clamped to 0.
     */
    public float getColumnPos(int j) {
        if (j >= columnPos.length) {
            return columnPos[columnPos.length - 1] + (j - columnPos.length + 1) * getDefaultColumnWidthInPoints();
        }
        return columnPos[Math.max(0, j)];
    }

    /**
     * Gets the column number where the sheet is vertically split.
     * The split column divides the sheet into left and right panes.
     *
     * @return the column number (0-based) where the vertical split occurs
     */
    public int getSplitColumn() {
        return splitColumn;
    }

    /**
     * Gets the default width for columns in points (1/72 inch).
     * This value is used for new columns and for columns beyond the sheet bounds.
     * Delegates to the underlying sheet's default column width setting.
     *
     * @return the default column width in points
     */
    public float getDefaultColumnWidthInPoints() {
        return sheet.getDefaultColumnWidth();
    }

    /**
     * Sets the column where the sheet should be vertically split.
     * If the split position changes, triggers a layout update.
     * The method is thread-safe and uses a write lock to ensure proper synchronization.
     *
     * @param j the column number (0-based) where the vertical split should occur
     */
    public void setSplitColumn(int j) {
        try (var __ = writeLock("SheetViewDelegate.setSplitColumn()")) {
            if (j != splitColumn) {
                splitColumn = j;
                markLayoutChanged();
            }
        }
    }

    /**
     * Gets the y-coordinate of the horizontal split line in points (1/72 inch).
     * This coordinate is calculated as the top edge position of the split row.
     *
     * @return dy coordinate of split, measured from the top edge of the sheet
     */
    public float getSplitYInPoints() {
        return getRowPos(getSplitRow());
    }

    /**
     * Get the y-coordinate (in points) of the top edge of the specified row.
     *
     * @param i the row number (0-based)
     * @return the y-coordinate in points. For row numbers beyond the last row,
     * the position is calculated by extrapolating using the default row height.
     * Negative row numbers are clamped to 0.
     */
    public float getRowPos(int i) {
        if (i >= rowPos.length) {
            return rowPos[rowPos.length - 1] + (i - rowPos.length + 1) * getDefaultRowHeightInPoints();
        }
        return rowPos[Math.max(0, i)];
    }

    /**
     * Gets the row number where the sheet is horizontally split.
     * The split row divides the sheet into upper and lower panes.
     *
     * @return the row number (0-based) where the horizontal split occurs
     */
    public int getSplitRow() {
        return splitRow;
    }

    /**
     * Gets the default height for rows in points (1/72 inch).
     * This value is used for new rows and for rows beyond the sheet bounds.
     * Delegates to the underlying sheet's default row height setting.
     *
     * @return the default row height in points
     */
    public float getDefaultRowHeightInPoints() {
        return sheet.getDefaultRowHeight();
    }

    /**
     * Sets the row where the sheet should be horizontally split.
     * If the split position changes, triggers a layout update.
     * The method is thread-safe and uses a write lock to ensure proper synchronization.
     *
     * @param i the row number (0-based) where the horizontal split should occur
     */
    public void setSplitRow(int i) {
        try (var __ = writeLock("SheetViewDelegate.setSplitRow()")) {
            if (i != splitRow) {
                splitRow = i;
                markLayoutChanged();
            }
        }
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
     * Sets the stroke width of the selection.
     *
     * @param selectionStrokeWidth the width of the stroke to be used for selection,
     *                             specified as a float value.
     */
    public void setSelectionStrokeWidth(float selectionStrokeWidth) {
        this.selectionStrokeWidth = selectionStrokeWidth;
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
     * Gets the cell at the specified coordinates in the sheet.
     *
     * @param x x-coordinate in points from the left edge of the sheet
     * @param y y-coordinate in points from the top edge of the sheet
     * @return the cell at the specified coordinates. If the coordinates are outside
     * the sheet bounds, returns the cell at the nearest valid position as
     * the coordinates are not clipped to the sheet bounds
     */
    public Cell getCellAt(float x, float y) {
        int i = getRowNumberFromY(y, false);
        int j = getColumnNumberFromX(x, false);
        return sheet.getCell(i, j);
    }

    /**
     * Get the row number that the given dy-coordinate belongs to.
     *
     * @param y           dy-coordinate
     * @param clipToSheet if true, restrict the returned row number to the current maximum row number,
     *                    otherwise calculate the row number based on the assumption that missing rows
     *                    all have the default row height
     * @return <ul>
     * <li>-1, if the first row is displayed below the given coordinate
     * <li>number of rows, if the lower edge of the last row is displayed
     * above the given coordinate
     * <li>the number of the row that belongs to the given coordinate
     * </ul>
     */
    public int getRowNumberFromY(float y, boolean clipToSheet) {
        return getPositionIndexFromCoordinate(rowPos, y, getDefaultRowHeightInPoints(), clipToSheet);
    }

    /**
     * Get the column number that corresponds to the given x-coordinate in points.
     *
     * @param x           x-coordinate in points from the left edge of the sheet
     * @param clipToSheet if true, the returned column number will be clipped to the existing
     *                    column range; if false, the method will extrapolate beyond the last
     *                    column using the default column width
     * @return the column number where:
     * <ul>
     * <li>-1: if the coordinate is to the left of the first column
     * <li>number of columns: if the coordinate is to the right of the last column
     *     (only when clipToSheet is false)
     * <li>0 to (number of columns - 1): the column number containing the coordinate
     * </ul>
     */
    public int getColumnNumberFromX(float x, boolean clipToSheet) {
        return getPositionIndexFromCoordinate(columnPos, x, getDefaultColumnWidthInPoints(), clipToSheet);
    }

    /**
     * Determines the position index corresponding to a given coordinate value
     * based on an array of precomputed position values. The method can also
     * calculate an extrapolated position index if the coordinate is beyond
     * the bounds of the provided positions array.
     *
     * @param positions               An array of precomputed position values representing discrete points.
     *                                The array must have at least one value for the method to function correctly.
     * @param coord                   The coordinate value for which a corresponding position index is to be found.
     * @param defaultItemSizeInPixels The assumed default size of an item in pixels, used for extrapolation
     *                                when the coordinate is outside the bounds of the positions array.
     * @param clipToExisting          A flag indicating whether to clip the calculated position index to the
     *                                existing bounds of the positions array or to extrapolate beyond its range.
     *                                If true, the index is clipped to the bounds of the array.
     * @return The position index corresponding to the given coordinate.
     * If the coordinate is outside bounds, the returned index may be extrapolated
     * or clipped based on the clipToExisting parameter.
     */
    private static int getPositionIndexFromCoordinate(
            float[] positions,
            float coord,
            float defaultItemSizeInPixels,
            boolean clipToExisting) {
        if (positions.length < 2) {
            return 0;
        }
        float lastPosition = positions[positions.length - 1];
        if (coord > lastPosition) {
            if (clipToExisting) {
                return positions.length - 2;
            } else {
                return positions.length - 1 + (int) Math.floor((coord - lastPosition) / defaultItemSizeInPixels);
            }
        }

        // guess position
        int j = (int) (positions.length * coord / positions[positions.length - 1]);
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

        return Math.max(0, j - 1);
    }

    /**
     * Get the number of rows for the currently loaded sheet.
     *
     * @return number of rows
     */
    public int getRowCount() {
        return rowPos.length - 1;
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
            case SheetEvent.ZOOM_CHANGED, SheetEvent.LAYOUT_CHANGED, SheetEvent.ROWS_ADDED,
                 SheetEvent.COLUMNS_ADDED -> owner.updateContent();
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
            case SheetEvent.CELL_VALUE_CHANGED, SheetEvent.CELL_STYLE_CHANGED ->
                    owner.repaintCell(((SheetEvent.CellChanged<?>) item).cell());
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

    /**
     * Gets the display name for a column.
     * The name is generated using the current column naming function.
     *
     * @param j the column number (0-based)
     * @return the display name for the specified column
     */
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
     * Sets the current column number while maintaining the current row selection.
     * This method is typically used for horizontal navigation in the sheet.
     *
     * @param colNum the column number to select (0-based). Negative values will
     *               be clamped to 0.
     */
    public void setCurrentColNum(int colNum) {
        try (var __ = writeLock("SheetViewDelegate.setCurrentColNum()")) {
            int rowNum = sheet.getCurrentCell().getRowNumber();
            setCurrentCell(sheet.getCell(rowNum, Math.max(0, colNum)));
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

    /**
     * Retrieves the current sheet associated with this view delegate.
     *
     * @return the currently loaded sheet
     */
    public Sheet getSheet() {
        return sheet;
    }

    /**
     * Sets the current row number while maintaining the current column selection.
     * This method is typically used for vertical navigation in the sheet.
     *
     * @param rowNum the row number to select (0-based). Negative values will
     *               be clamped to 0.
     */
    public void setCurrentRowNum(int rowNum) {
        try (var __ = writeLock("SheetViewDelegate.setCurrentRowNum()")) {
            int colNum = sheet.getCurrentCell().getColumnNumber();
            setCurrentCell(Math.max(0, rowNum), colNum);
        }
    }

    /**
     * Gets the color used for drawing grid lines in the sheet view.
     * Grid lines separate cells visually in the sheet.
     *
     * @return the current color used for grid lines
     */
    public Color getGridColor() {
        return gridColor;
    }

    /**
     * Sets the color to be used for drawing grid lines in the sheet view.
     * This affects the visual appearance of cell boundaries.
     * The method is thread-safe and uses a write lock to ensure proper synchronization.
     *
     * @param gridColor the new color for grid lines. Must not be null.
     */
    public void setGridColor(Color gridColor) {
        try (var __ = writeLock("SheetViewDelegate.setGridColor()")) {
            this.gridColor = gridColor;
        }
    }

    /**
     * Gets the display name for a row.
     * The name is generated using the current row naming function.
     *
     * @param i the row number (0-based)
     * @return the display name for the specified row
     */
    public String getRowName(int i) {
        return rowNames.apply(i);
    }

    /**
     * Sets the function used to generate column names.
     * This function is called to create display names for columns in the sheet view.
     * The method is thread-safe and triggers a layout update since column names
     * may affect the layout dimensions.
     *
     * @param columnNames a function that takes a column number (0-based) and returns
     *                    the display name for that column. Must not be null.
     */
    public void setColumnNames(IntFunction<String> columnNames) {
        try (var __ = writeLock("SheetViewDelegate.setColumnNames()")) {
            this.columnNames = columnNames;
            markLayoutChanged();
        }
    }

    /**
     * Sets the function used to generate row names.
     * This function is called to create display names for rows in the sheet view.
     * The method is thread-safe and triggers a layout update since row names
     * may affect the layout dimensions.
     *
     * @param rowNames a function that takes a row number (0-based) and returns
     *                 the display name for that row. Must not be null.
     */
    public void setRowNames(IntFunction<String> rowNames) {
        try (var __ = writeLock("SheetViewDelegate.setRowNames()")) {
            this.rowNames = rowNames;
            markLayoutChanged();
        }
    }

    /**
     * Gets the background color used for the sheet view.
     * This color is used to fill the sheet's background area.
     *
     * @return the current background color
     */
    public Color getBackground() {
        return background;
    }

    /**
     * Sets the background color for the sheet view.
     * This affects the appearance of the sheet's background area.
     * The method is thread-safe and uses a write lock to ensure proper synchronization.
     *
     * @param background the new background color to use. Must not be null.
     */
    public void setBackground(Color background) {
        try (var __ = writeLock("SheetViewDelegate.setBackground()")) {
            this.background = background;
        }
    }

    /**
     * Moves the current cell selection in the specified direction, taking into account
     * merged cells (spans). The movement is based on the logical cell, which represents
     * the top-left cell of a merged region.
     *
     * @param d the direction to move (NORTH, SOUTH, EAST, or WEST)
     */
    public void move(Direction d) {
        try (var __ = writeLock("SheetViewDelegate.move()")) {
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
     * Gets the logical cell for the current selection. For merged cells, this returns
     * the top-left cell of the merged region, which is considered the logical cell
     * for the entire merged area.
     *
     * @return the logical cell representing the current selection. For non-merged cells,
     * this is the same as the current cell. For merged cells, this is the
     * top-left cell of the merged region.
     */
    public Cell getCurrentLogicalCell() {
        return getSheet().getCurrentCell().getLogicalCell();
    }

    /**
     * Sets the current (selected) cell using row and column indices.
     *
     * @param i the row index of the cell to select (0-based)
     * @param j the column index of the cell to select (0-based)
     * @return true if the cell selection was changed, false if the specified cell
     * was already selected or the indices were out of bounds
     */
    public boolean setCurrentCell(int i, int j) {
        try (var __ = writeLock("SheetViewDelegate.setCurrentCell()")) {
            return sheet.setCurrentCell(i, j);
        }
    }

    /**
     * Sets the current (selected) cell using a Cell object.
     *
     * @param cell the cell to select. Must be a cell from this sheet.
     * @return true if the cell selection was changed, false if the specified cell
     * was already selected or the cell was invalid (null or from a different sheet)
     */
    public boolean setCurrentCell(Cell cell) {
        try (var __ = writeLock("SheetViewDelegate.setCurrentCell()")) {
            return sheet.setCurrentCell(cell);
        }
    }

    /**
     * Moves the current cell selection by a page in the specified direction, taking into
     * account merged cells (spans). The movement is based on the logical cell and considers
     * the current view area.
     *
     * @param d the direction to move (NORTH, SOUTH, EAST, or WEST). The movement distance
     *          depends on the current view area and cell spans.
     */
    public void movePage(Direction d) {
        try (var __ = writeLock("SheetViewDelegate.movePage()")) {
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

    /**
     * Moves the selection to the bottom-right cell of the sheet. This selects
     * the cell at the intersection of the last row and last column that contain data.
     */
    public void moveEnd() {
        try (var __ = writeLock("SheetViewDelegate.moveEnd()")) {
            int row = sheet.getRowCount() - 1;
            int col = sheet.getColumnCount() - 1;
            setCurrentCell(row, col);
        }
    }

    /**
     * Moves the selection to the top-left cell of the sheet. This selects
     * the cell at the intersection of the first row and first column that contain data.
     */
    public void moveHome() {
        try (var __ = writeLock("SheetViewDelegate.moveHome()")) {
            setCurrentCell(0, 0);
        }
    }

    /**
     * Handles mouse press events on a cell in the sheet. This method manages both
     * cell selection and editing states:
     * <p>
     * If clicking a different cell:
     * 1. Sets the clicked cell as the current cell
     * 2. If currently editing, stops the editing
     * <p>
     * If clicking the current cell:
     * 1. If the sheet is editable, starts editing the cell
     * <p>
     * In all cases, the sheet view receives focus.
     * The method logs the cell reference at trace level for debugging purposes.
     *
     * @param cell the cell that was clicked. Must be a cell from this sheet.
     */
    public void onMousePressed(Cell cell) {
        LOG.trace("onMousePressed({})", cell::getCellRef);

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

    /**
     * Requests keyboard focus for this sheet view. This ensures that keyboard
     * events (like typing or navigation keys) are directed to this sheet view.
     * The actual focus request is delegated to the owner component through its
     * focusView() method. The request is logged at trace level for debugging.
     */
    public void requestFocus() {
        LOG.trace("requestFocus()");
        owner.focusView();
    }

    /**
     * Checks whether a cell is currently being edited.
     * This indicates whether the sheet view is in cell editing mode,
     * where the user can directly modify the content of the current cell.
     *
     * @return true if a cell is currently being edited, false otherwise
     */
    public boolean isEditing() {
        return editing;
    }

    /**
     * Checks whether the sheet view allows editing of cell content.
     * This setting controls whether users can modify cell values through the UI.
     *
     * @return true if editing is enabled, false if the sheet view is read-only
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Sets whether the sheet view allows editing of cell content.
     * When editing is disabled, users cannot modify cell values through the UI.
     * The method is thread-safe and uses a write lock to ensure proper synchronization.
     *
     * @param editable true to enable editing, false to make the sheet view read-only
     */
    public void setEditable(boolean editable) {
        LOG.trace("setEditable({})", editable);
        try (var __ = writeLock("SheetViewDelegate.setEditable()")) {
            this.editable = editable;
        }
    }

    /**
     * Sets the editing state of the sheet view.
     * This method is called internally to manage the cell editing state.
     * The method is thread-safe and uses a write lock to ensure proper synchronization.
     *
     * @param editing true to indicate that cell editing is in progress,
     *                false to indicate that editing has ended
     */
    public void setEditing(boolean editing) {
        LOG.trace("setEditing({})", editing);
        try (var __ = writeLock("SheetViewDelegate.setEditing()")) {
            this.editing = editing;
        }
    }

    /**
     * Gets the horizontal padding used for cell content layout. This padding is applied
     * to both the left and right sides of cell content to prevent text from touching
     * cell borders.
     *
     * @return horizontal padding in points (1/72 inch)
     */
    public float getPaddingX() {
        return PADDING_X_IN_POINTS;
    }

    /**
     * Gets the vertical padding used for cell content layout. This padding is applied
     * to both the top and bottom of cell content to prevent text from touching
     * cell borders.
     *
     * @return vertical padding in points (1/72 inch)
     */
    public float getPaddingY() {
        return PADDING_Y_IN_POINTS;
    }

    /**
     * Gets the height of a specific row in pixels, taking into account the current scale.
     *
     * @param rowNumber the row number (0-based)
     * @return the height of the specified row in pixels
     */
    public float getRowHeightInPixels(int rowNumber) {
        return getRowHeightInPoints(rowNumber) * scale.sy();
    }

    /**
     * Gets the height of a specific row in points (1/72 inch).
     * For rows within the sheet bounds, calculates the height as the difference between
     * the start positions of the current and next row. For the last row or rows beyond
     * the sheet bounds, returns the default row height.
     *
     * @param i the row number (0-based)
     * @return the height of the specified row in points
     */
    public float getRowHeightInPoints(int i) {
        return i + 1 < rowPos.length ? rowPos[i + 1] - rowPos[i] : getDefaultRowHeightInPoints();
    }

    /**
     * Gets the width of a specific column in points (1/72 inch).
     * The width is calculated as the difference between the start positions of
     * the current column and the next column.
     *
     * @param j the column number (0-based)
     * @return the width of the specified column in points
     */
    public float getColumnWidthInPoints(int j) {
        return columnPos[j + 1] - columnPos[j];
    }

    /**
     * Gets the locale used for formatting and displaying content in the sheet view.
     * Delegates to the owner component's locale setting.
     *
     * @return the current locale used for text formatting and display
     */
    public Locale getLocale() {
        return owner.getLocale();
    }

    /**
     * Gets the width of label borders in points (1/72 inch).
     * This is calculated by converting the pixel width using the current pixel-to-point ratio.
     *
     * @return the current border width in points
     */
    public float getLabelBorderWidthInPoints() {
        return labelBorderWidthInPixels * get1PxWidthInPoints();
    }

    /**
     * Gets the width of one pixel in points (1/72 inch).
     * This value is used for converting between pixel and point measurements.
     *
     * @return the width of one pixel in points
     */
    public float get1PxWidthInPoints() {
        return pixelWidthInPoints;
    }

    /**
     * Gets the current transformation matrix used for coordinate conversions
     * between the model and view space. Creates a new scale transformation
     * based on the current scale settings.
     *
     * @return a new affine transformation representing the current scale
     */
    public AffineTransformation2f getTransformation() {
        return AffineTransformation2f.scale(getScale());
    }

    /**
     * Gets the current scale factor applied to the sheet content.
     * This scale determines how the sheet content is sized relative to its logical dimensions.
     *
     * @return the current scale factor for both horizontal and vertical dimensions
     */
    public Scale2f getScale() {
        return scale;
    }

    /**
     * Sets the scale factor for the sheet content.
     * This affects the size of all sheet elements including cells, text, and decorations.
     * The method is thread-safe and triggers a layout update only if the scale actually changes.
     *
     * @param scale the new scale factor to apply. Must not be null.
     */
    public void setScale(Scale2f scale) {
        try (var __ = writeLock("SheetViewDelegate.setScale()")) {
            if (!scale.equals(this.scale)) {
                this.scale = scale;
                markLayoutChanged();
            }
        }
    }

    /**
     * Gets the default height for rows in pixels.
     * This value is used for new rows and for rows beyond the sheet bounds.
     * Converts the point-based default height to pixels using the current pixel-to-point ratio.
     *
     * @return the default row height in pixels
     */
    public float getDefaultRowHeightInPixels() {
        return getDefaultRowHeightInPoints() / get1PxHeightInPoints();
    }

    /**
     * Gets the height of one pixel in points (1/72 inch).
     * This value is used for converting between pixel and point measurements.
     *
     * @return the height of one pixel in points
     */
    public float get1PxHeightInPoints() {
        return pixelHeightInPoints;
    }

    /**
     * Gets the default width for columns in pixels.
     * This value is used for new columns and for columns beyond the sheet bounds.
     * Converts the point-based default width to pixels using the current pixel-to-point ratio.
     *
     * @return the default column width in pixels
     */
    public float getDefaultColumnWidthInPixels() {
        return getDefaultColumnWidthInPoints() / get1PxWidthInPoints();
    }

    /**
     * Gets the current display scale factor applied to the sheet view.
     * This scale affects how the sheet content is rendered on screen and
     * is used for zooming operations.
     *
     * @return the current display scale factor
     */
    public Scale2f getDisplayScale() {
        return displayScale;
    }

    /**
     * Sets the display scale factor for the sheet view.
     * This scale is used to adjust the view for different display resolutions or zoom levels.
     * Unlike setScale(), which affects the content size, this affects how the scaled content
     * is displayed on the screen. The method is thread-safe and triggers a layout update
     * only if the display scale actually changes.
     *
     * @param displayScale the new display scale factor to apply. Must not be null.
     */
    public void setDisplayScale(Scale2f displayScale) {
        try (var __ = writeLock("SheetViewDelegate.setDisplayScale()")) {
            if (!displayScale.equals(this.displayScale)) {
                this.displayScale = displayScale;
                markLayoutChanged();
            }
        }
    }

    /**
     * Returns the area taken up by the sheet and decorations in view coordinates.
     * This includes the main sheet area, row headers (numbers), column headers (letters),
     * and any split lines if the sheet is split.
     *
     * @return the total area used to display the sheet and its labels, in points
     */
    public Rectangle2f getTotalArea() {
        float labelHeight = getColumnLabelHeightInPoints();
        float labelWidth = getRowLabelWidthInPoints();
        float x = -labelWidth;
        float y = -labelHeight;
        float w = labelWidth + getScale().sx() * getSheetWidthInPoints();
        float h = labelHeight + getScale().sy() * getSheetHeightInPoints();
        return new Rectangle2f(x, y, w, h);
    }

    /**
     * Gets the height of column labels (column letters) in points (1/72 inch).
     * This height is used to determine the size of the column header area in the sheet view.
     * The height is constant in points but may vary in pixels depending on the current scale.
     *
     * @return the height of column labels in points
     */
    public float getColumnLabelHeightInPoints() {
        return columnLabelHeightInPoints;
    }

    /**
     * Gets the width of row labels (row numbers) in points (1/72 inch).
     * This width is used to determine the size of the row header area in the sheet view.
     * The width is constant in points but may vary in pixels depending on the current scale.
     *
     * @return the width of row labels in points
     */
    public float getRowLabelWidthInPoints() {
        return rowLabelWidth;
    }

    /**
     * Draws a label with text in the specified rectangle using the current label styling.
     * The label includes:
     * - A background filled with the label background color
     * - A border with the label border color and specified width
     * - Centered text using the label font
     *
     * @param g    the graphics context to draw on
     * @param r    the rectangle defining the boundaries of the label
     * @param text the text to be displayed in the label, centered both horizontally and vertically
     */
    public void drawLabel(Graphics g, Rectangle2f r, String text) {
        g.setFill(getLabelBackgroundColor());
        g.fillRect(r);

        g.setStroke(getLabelBorderColor(), getLabelBorderWidthInPixels() * get1PxWidthInPoints());
        g.strokeRect(r);

        g.setFont(getLabelFont());
        g.drawText(text, r.xCenter(), r.yCenter(), Graphics.HAnchor.CENTER, Graphics.VAnchor.MIDDLE);
    }

    /**
     * Gets the background color used for row and column labels.
     *
     * @return the current background color used for labels
     */
    public Color getLabelBackgroundColor() {
        return labelBackgroundColor;
    }

    /**
     * Gets the color used for drawing borders around row and column labels.
     *
     * @return the current color used for label borders
     */
    public Color getLabelBorderColor() {
        return labelBorderColor;
    }

    /**
     * Sets the color to be used for drawing borders around row and column labels.
     * This affects the visual appearance of the sheet's header areas.
     *
     * @param labelBorderColor the new color for label borders. Must not be null.
     */
    public void setLabelBorderColor(Color labelBorderColor) {
        this.labelBorderColor = labelBorderColor;
    }

    /**
     * Gets the width of label borders in pixels.
     * This is the actual width used for rendering borders around row and column labels.
     *
     * @return the current border width in pixels
     */
    public float getLabelBorderWidthInPixels() {
        return labelBorderWidthInPixels;
    }

    /**
     * Sets the width of label borders in pixels.
     * This affects the visual appearance of borders around row and column labels.
     *
     * @param labelBorderWidthInPixels the new border width in pixels. Must be positive.
     */
    public void setLabelBorderWidthInPixels(float labelBorderWidthInPixels) {
        this.labelBorderWidthInPixels = labelBorderWidthInPixels;
    }

    /**
     * Sets the background color to be used for row and column labels.
     * This affects the visual appearance of the sheet's header areas.
     *
     * @param labelBackgroundColor the new background color for labels. Must not be null.
     */
    public void setLabelBackgroundColor(Color labelBackgroundColor) {
        this.labelBackgroundColor = labelBackgroundColor;
    }

    /**
     * Calculates the sheet area (range of rows and columns) that intersects with
     * the given rectangle in the view coordinates.
     *
     * @param r           the rectangle in view coordinates (points) to calculate
     *                    the intersecting sheet area for
     * @param clipToSheet if true, the end row and column will be clipped to the
     *                    sheet bounds; if false, the area may extend beyond the
     *                    sheet's actual dimensions
     * @return a SheetArea object containing the start/end row and column numbers
     * that intersect with the given rectangle. Row and column numbers
     * start at 0, and negative start values are clamped to 0. End values
     * are exclusive (one past the last included row/column).
     */
    public SheetView.SheetArea getSheetArea(Rectangle2f r, boolean clipToSheet) {
        int startRow = Math.max(0, getRowNumberFromY(r.yMin(), false));
        int startColumn = Math.max(0, getColumnNumberFromX(r.xMin(), false));
        int endRow = getRowNumberFromY(r.yMax(), clipToSheet) + 1;
        int endColumn = getColumnNumberFromX(r.xMax(), clipToSheet) + 1;

        return new SheetView.SheetArea(
                r,
                startRow,
                startColumn,
                endRow,
                endColumn
        );
    }

    private void onSetRowCount(int rowCount) {
        if (rowCount != rowPos.length - 1) {
            markLayoutChanged();
        }
    }

    private void onSetColumnCount(int columnCount) {
        if (columnCount != columnPos.length - 1) {
            markLayoutChanged();
        }
    }

    /**
     * Gets the width of the split line in points (1/72 inch).
     * The split line is used to visually separate different panes when the sheet is split.
     *
     * @return the width of the split line in points, adjusted for the current horizontal scale
     */
    public float getSplitLineWidthInPoints() {
        return SPLIT_LINE_PIXELS / scale.sx();
    }

    /**
     * Gets the height of the split line in points (1/72 inch).
     * The split line is used to visually separate different panes when the sheet is split.
     *
     * @return the height of the split line in points, adjusted for the current vertical scale
     */
    public float getSplitLineHeightInPoints() {
        return SPLIT_LINE_PIXELS / scale.sy();
    }

    /**
     * Gets the x-coordinate of the vertical split line in pixels.
     * This is calculated as the sum of the row label width and the scaled position
     * of the split column.
     *
     * @return the x-coordinate of the vertical split line in pixels
     */
    public double getSplitXInPixels() {
        return getRowLabelWidthInPixels() + columnPos[splitColumn] * scale.sx();
    }

    /**
     * Gets the width of row labels (row numbers) in pixels.
     * This is the actual width used for rendering, calculated by applying the current
     * scale to the point-based width. This value changes when the view is zoomed.
     *
     * @return the width of row labels in pixels, based on the current scale
     */
    public float getRowLabelWidthInPixels() {
        return rowLabelWidth * scale.sx();
    }

    /**
     * Gets the y-coordinate of the horizontal split line in pixels.
     * This is calculated as the sum of the column label height and the scaled position
     * of the split row.
     *
     * @return the y-coordinate of the horizontal split line in pixels
     */
    public double getSplitYInPixels() {
        return getColumnLabelHeightInPixels() + rowPos[splitRow] * scale.sy();
    }

    /**
     * Gets the height of column labels (column letters) in pixels.
     * This is the actual height used for rendering, calculated by applying the current
     * scale to the point-based height. This value changes when the view is zoomed.
     *
     * @return the height of column labels in pixels, based on the current scale
     */
    public float getColumnLabelHeightInPixels() {
        return columnLabelHeightInPoints * scale.sy();
    }

    /**
     * Gets the width of the split line in device pixels.
     * This is a constant value that determines the visual thickness of the vertical split line.
     *
     * @return the width of the split line in pixels
     */
    public double getSplitLineWidth() {
        return SPLIT_LINE_PIXELS;
    }

    /**
     * Gets the height of the split line in device pixels.
     * This is a constant value that determines the visual thickness of the horizontal split line.
     *
     * @return the height of the split line in pixels
     */
    public double getSplitLineHeight() {
        return SPLIT_LINE_PIXELS;
    }
}
