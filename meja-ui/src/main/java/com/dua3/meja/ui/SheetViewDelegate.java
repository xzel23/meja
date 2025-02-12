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

    private static final int SPLIT_LINE_PIXELS = 1;

    /**
     * Color used to draw the selection rectangle.
     */
    private Color selectionColor = Color.GREEN;

    /**
     * Width of the selection rectangle borders.
     */
    private float selectionStrokeWidth = DEFAULT_SELECTION_STROKE_WIDTH;

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

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

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
     * Acquires a write lock on the sheet and returns an AutoLock instance that
     * manages the lock with an associated description.
     *
     * @param name the name of the lock
     * @return an AutoLock instance that wraps the write lock of the sheet and
     *         provides a textual description of the lock, including the sheet name.
     */
    public AutoLock writeLock(String name) {
        return AutoLock.of(lock.writeLock(), name);
    }

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
     * Retrieves the width of the sheet in pixels.
     *
     * @return the width of the sheet in pixels
     */
    public float getSheetWidthInPixels() {
        return getSheetWidthInPoints() * pixelWidthInPoints;
    }

    /**
     * Retrieves the height of the sheet in pixels.
     *
     * @return the height of the sheet in pixels
     */
    public float getSheetHeightInPixels() {
        return getSheetHeightInPoints() * pixelHeightInPoints;
    }

    /**
     * Get dx-coordinate of split.
     *
     * @return dx coordinate of split
     */
    public float getSplitXInPoints() {
        return getColumnPos(getSplitColumn());
    }

    /**
     * Get dy-coordinate of split.
     *
     * @return dy coordinate of split
     */
    public float getSplitYInPoints() {
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
    public int getColumnNumberFromX(float x, boolean clipToSheet) {
        return getPositionIndexFromCoordinate(columnPos, x, getDefaultColumnWidthInPoints(), clipToSheet);
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
    public int getRowNumberFromY(float y, boolean clipToSheet) {
        return getPositionIndexFromCoordinate(rowPos, y, getDefaultRowHeightInPoints(), clipToSheet);
    }

    public Cell getCellAt(float x, float y) {
        int i = getRowNumberFromY(y, false);
        int j = getColumnNumberFromX(x, false);
        return sheet.getCell(i, j);
    }

    /**
     * Determines the position index corresponding to a given coordinate value
     * based on an array of precomputed position values. The method can also
     * calculate an extrapolated position index if the coordinate is beyond
     * the bounds of the provided positions array.
     *
     * @param positions An array of precomputed position values representing discrete points.
     *                  The array must have at least one value for the method to function correctly.
     * @param coord     The coordinate value for which a corresponding position index is to be found.
     * @param defaultItemSizeInPixels
     *                  The assumed default size of an item in pixels, used for extrapolation
     *                  when the coordinate is outside the bounds of the positions array.
     * @param clipToExisting
     *                  A flag indicating whether to clip the calculated position index to the
     *                  existing bounds of the positions array or to extrapolate beyond its range.
     *                  If true, the index is clipped to the bounds of the array.
     * @return The position index corresponding to the given coordinate.
     *         If the coordinate is outside bounds, the returned index may be extrapolated
     *         or clipped based on the clipToExisting parameter.
     */
    private int getPositionIndexFromCoordinate(
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
     * @param j the column number
     * @return the columnPos
     */
    public float getColumnPos(int j) {
        if (j >= columnPos.length) {
            return columnPos[columnPos.length - 1] + (j - columnPos.length + 1) * getDefaultColumnWidthInPoints();
        }
        return columnPos[Math.max(0, j)];
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
        if (i >= rowPos.length) {
            return rowPos[rowPos.length - 1] + (i - rowPos.length + 1) * getDefaultRowHeightInPoints();
        }
        return rowPos[Math.max(0, i)];
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
        LOG.trace("updateLayout()");
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
            Rectangle2f dim = FontUtil.getInstance().getTextDimension(sMax, getLabelFont());
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
        try (var __ = writeLock("SheetViewDelegate.setGridColor()")) {
            this.gridColor = gridColor;
        }
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
        try (var __ = writeLock("SheetViewDelegate.setEditable()")) {
            this.editable = editable;
        }
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
        try (var __ = writeLock("SheetViewDelegate.setEditing()")) {
            this.editing = editing;
        }
    }

    public void setColumnNames(IntFunction<String> columnNames) {
        try (var __ = writeLock("SheetViewDelegate.setColumnNames()")) {
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
        try (var __ = writeLock("SheetViewDelegate.setCurrentCell()")) {
            return sheet.setCurrentCell(i, j);
        }
    }

    public boolean setCurrentCell(Cell cell) {
        try (var __ = writeLock("SheetViewDelegate.setCurrentCell()")) {
            return sheet.setCurrentCell(cell);
        }
    }

    public void setRowNames(IntFunction<String> rowNames) {
        try (var __ = writeLock("SheetViewDelegate.setRowNames()")) {
            this.rowNames = rowNames;
            markLayoutChanged();
        }
    }

    public Scale2f getScale() {
        return scale;
    }

    public void setScale(Scale2f scale) {
        try (var __ = writeLock("SheetViewDelegate.setScale()")) {
            if (!scale.equals(this.scale)) {
                this.scale = scale;
                markLayoutChanged();
            }
        }
    }

    public void setDisplayScale(Scale2f displayScale) {
        try (var __ = writeLock("SheetViewDelegate.setDisplayScale()")) {
            if (!displayScale.equals(this.displayScale)) {
                this.displayScale = displayScale;
                markLayoutChanged();
            }
        }
    }

    private void markLayoutChanged() {
        LOG.trace("markLayoutChanged()");
        try (var __ = writeLock("SheetViewDelegate.markLayoutChanged()")) {
            this.layoutChanged = true;
        }
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        try (var __ = writeLock("SheetViewDelegate.setBackground()")) {
            this.background = background;
        }
    }

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
     * Move the selection rectangle to an adjacent cell.
     *
     * @param d direction
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

    public Cell getCurrentLogicalCell() {
        return getSheet().getCurrentCell().getLogicalCell();
    }

    public void setCurrentColNum(int colNum) {
        try (var __ = writeLock("SheetViewDelegate.setCurrentColNum()")) {
            int rowNum = sheet.getCurrentCell().getRowNumber();
            setCurrentCell(sheet.getCell(rowNum, Math.max(0, colNum)));
        }
    }

    public void setCurrentRowNum(int rowNum) {
        try (var __ = writeLock("SheetViewDelegate.setCurrentRowNum()")) {
            int colNum = sheet.getCurrentCell().getColumnNumber();
            setCurrentCell(Math.max(0, rowNum), colNum);
        }
    }

    /**
     * Move the selection rectangle to the bottom right cell.
     */
    public void moveEnd() {
        try (var __ = writeLock("SheetViewDelegate.moveEnd()")) {
            int row = sheet.getLastRowNum();
            int col = sheet.getLastColNum();
            setCurrentCell(row, col);
        }
    }

    /**
     * Move the selection rectangle to the top left cell.
     */
    public void moveHome() {
        try (var __ = writeLock("SheetViewDelegate.moveHome()")) {
            int row = sheet.getFirstRowNum();
            int col = sheet.getFirstColNum();
            setCurrentCell(row, col);
        }
    }

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

    public void requestFocus() {
        LOG.trace("requestFocus()");
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

    /**
     * Get the label font.
     *
     * @return the width of row labels in points
     */
    public Font getLabelFont() {
        return labelFont;
    }

    public void setLabelFont(Font labelFont) {
        try (var __ = writeLock("SheetViewDelegate.setLabelFont()")) {
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
     * Get the width of Row labels in points.
     *
     * @return the width of row labels in points
     */
    public float getRowLabelWidthInPixels() {
        return rowLabelWidth * scale.sx();
    }

    /**
     * Get the height of column labels in points.
     *
     * @return the height of cloumn labels in points
     */
    public float getColumnLabelHeightInPoints() {
        return columnLabelHeightInPoints;
    }

    public float getRowHeightInPixels(int rowNumber) {
        return getRowHeightInPoints(rowNumber) * scale.sy();
    }

    public float getRowHeightInPoints(int i) {
        return i + 1 < rowPos.length ? rowPos[i + 1] - rowPos[i] : getDefaultRowHeightInPoints();
    }

    public float getColumnLabelHeightInPixels() {
        return columnLabelHeightInPoints * scale.sy();
    }

    public float getColumnWidthInPoints(int j) {
        return columnPos[j + 1] - columnPos[j];
    }

    public float getDefaultRowHeightInPoints() {
        return sheet.getDefaultRowHeight();
    }

    public float getDefaultColumnWidthInPoints() {
        return sheet.getDefaultColumnWidth();
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
        return getDefaultRowHeightInPoints() / get1PxHeightInPoints();
    }

    public float getDefaultColumnWidthInPixels() {
        return getDefaultColumnWidthInPoints() / get1PxWidthInPoints();
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
        float w = labelWidth + getScale().sx() * getSheetWidthInPoints();
        float h = labelHeight + getScale().sy() * getSheetHeightInPoints();
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

    public void setSplitRow(int i) {
        try (var __ = writeLock("SheetViewDelegate.setSplitRow()")) {
            if (i != splitRow) {
                splitRow = i;
                markLayoutChanged();
            }
        }
    }

    public void setSplitColumn(int j) {
        try (var __ = writeLock("SheetViewDelegate.setSplitColumn()")) {
            if (j != splitColumn) {
                splitColumn = j;
                markLayoutChanged();
            }
        }
    }

    public float getSplitLineWidthInPoints() {
        return SPLIT_LINE_PIXELS / scale.sx();
    }

    public float getSplitLineHeightInPoints() {
        return SPLIT_LINE_PIXELS / scale.sy();
    }

    public double getSplitXInPixels() {
        return getRowLabelWidthInPixels() + columnPos[splitColumn] * scale.sx();
    }

    public double getSplitYInPixels() {
        return getColumnLabelHeightInPixels() + rowPos[splitRow] * scale.sy();
    }

    public double getSplitLineWidth() {
        return SPLIT_LINE_PIXELS;
    }

    public double getSplitLineHeight() {
        return SPLIT_LINE_PIXELS;
    }
}