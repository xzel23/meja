package com.dua3.meja.model;

import com.dua3.meja.util.RectangularRegion;
import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.Dimension2f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The AbstractSheet class represents the abstract base class for a sheet in a spreadsheet.
 * It provides common functionality for working with sheets and is designed to be extended by
 * specific implementations for different spreadsheet formats.
 */
public abstract class AbstractSheet implements Sheet {

    private static final Logger LOG = LogManager.getLogger(AbstractSheet.class);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<RectangularRegion> mergedRegions = new ArrayList<>();

    private final SubmissionPublisher<SheetEvent> publisher = new SubmissionPublisher<>();

    /**
     * Constructor.
     */
    protected AbstractSheet() {
    }

    @Override
    public void subscribe(Flow.Subscriber<SheetEvent> subscriber) {
        publisher.subscribe(subscriber);
    }

    private void submit(SheetEvent event) {
        LOG.trace("submit: {}", event);
        publisher.submit(event);
    }

    /**
     * Notifies subscribers that the active cell in the sheet has changed.
     * This method is called internally when there is a change in the active cell of the sheet.
     * Subscribers can listen to this event and perform any necessary actions or updates.
     *
     * @param old the previously active cell
     * @param arg the new active cell
     */
    protected void activeCellChanged(@Nullable Cell old, @Nullable Cell arg) {
        submit(new SheetEvent.ActiveCellChanged(this, old, arg));
    }

    /**
     * Notifies subscribers that the cell style has changed.
     *
     * @param cell the cell that has changed its style
     * @param old the previous style of the cell
     * @param arg additional argument related to the style change
     */
    protected void cellStyleChanged(Cell cell, Object old, Object arg) {
        submit(new SheetEvent.CellStyleChanged(this, cell, old, arg));
    }

    /**
     * Notifies subscribers that the value of a cell has been changed.
     *
     * @param cell the cell whose value has changed
     * @param old the old value of the cell
     * @param arg additional argument (optional)
     */
    protected void cellValueChanged(Cell cell, @Nullable Object old, @Nullable Object arg) {
        submit(new SheetEvent.CellValueChanged(this, cell, old, arg));
    }

    /**
     * Notifies subscribers that the layout of the sheet has changed.
     * This method is called internally when there is a change in the layout of the sheet.
     * Subscribers can listen to this event and perform any necessary actions or updates.
     */
    protected void layoutChanged() {
        submit(new SheetEvent.LayoutChanged(this));
    }

    /**
     * Broadcast event: rows added.
     *
     * @param first the index (inclusive) of the first added row
     * @param last the index (exclusive) of the last added row
     */
    protected void rowsAdded(int first, int last) {
        submit(new SheetEvent.RowsAdded(this, first, last));
    }

    /**
     * Broadcast event: columns added.
     *
     * @param first the index (inclusive) of the first added column
     * @param last the index (exclusive) of the last added column
     */
    protected void columnsAdded(int first, int last) {
        submit(new SheetEvent.ColumnsAdded(this, first, last));
    }

    /**
     * Broadcasts an event indicating a change in the zoom factor of the sheet.
     *
     * @param valueOld the previous zoom factor
     * @param valueNew the new zoom factor
     */
    protected void zoomChanged(double valueOld, double valueNew) {
        submit(new SheetEvent.ZoomChanged(this, valueOld, valueNew));
    }

    /**
     * Notifies subscribers that the split (freeze pane) in the sheet has changed.
     *
     * @param oldSplit the old split coordinates as a Pair of integers representing the row and column
     * @param newSplit the new split coordinates as a Pair of integers representing the row and column
     */
    protected void splitChanged(Pair<Integer, Integer> oldSplit, Pair<Integer, Integer> newSplit) {
        submit(new SheetEvent.SplitSet(this, oldSplit, newSplit));
    }

    @Override
    public Lock readLock() {
        return lock.readLock();
    }

    @Override
    public Lock writeLock() {
        return lock.writeLock();
    }

    @Override
    public void addMergedRegion(RectangularRegion cells) {
        // check that all cells are unmerged
        for (RectangularRegion rr : mergedRegions) {
            LangUtil.check(!rr.intersects(cells), "New merged region overlaps with an existing one.");
        }

        // update cell data
        int spanX = cells.lastColumn() - cells.firstColumn() + 1;
        int spanY = cells.lastRow() - cells.firstRow() + 1;
        AbstractCell topLeftCell = getCell(cells.firstRow(), cells.firstColumn());
        for (int i = 0; i < spanY; i++) {
            for (int j = 0; j < spanX; j++) {
                AbstractCell cell = getCell(cells.firstRow() + i, cells.firstColumn() + j);
                cell.addedToMergedRegion(topLeftCell, spanX, spanY);
            }
        }

        // add to list
        mergedRegions.add(cells);

        LOG.debug("added merged region: {}", cells);
    }

    @Override
    public List<RectangularRegion> getMergedRegions() {
        return Collections.unmodifiableList(mergedRegions);
    }

    @Override
    public Optional<RectangularRegion> getMergedRegion(int rowNum, int colNum) {
        return mergedRegions.stream().filter(rr -> rr.contains(rowNum, colNum)).findFirst();
    }

    /**
     * Removes a merged region from the sheet based on the given row number and column number.
     *
     * @param rowNumber    the row number of the merged region to be removed
     * @param columnNumber the column number of the merged region to be removed
     */
    protected void removeMergedRegion(int rowNumber, int columnNumber) {
        for (int idx = 0; idx < mergedRegions.size(); idx++) {
            RectangularRegion rr = mergedRegions.get(idx);
            if (rr.firstRow() == rowNumber && rr.firstColumn() == columnNumber) {
                mergedRegions.remove(idx--);
                for (int i = rr.firstRow(); i <= rr.lastRow(); i++) {
                    AbstractRow row = getRow(i);
                    for (int j = rr.firstColumn(); j <= rr.lastColumn(); j++) {
                        row.getCellIfExists(j).ifPresent(AbstractCell::removedFromMergedRegion);
                    }
                }
            }
        }
        LOG.debug("removed merged region at [{},{}]", rowNumber, columnNumber);
    }

    @Override
    public abstract AbstractRow getRow(int i);

    @Override
    public abstract Optional<? extends AbstractRow> getRowIfExists(int i);

    @Override
    public abstract AbstractCell getCell(int i, int j);

    @Override
    public abstract Optional<? extends AbstractCell> getCellIfExists(int i, int j);

    @Override
    public abstract AbstractWorkbook getWorkbook();

    @Override
    public void autoSizeColumn(int j) {
        float colWidth = (float) rows().flatMap(Row::cells)
                .filter(cell -> cell.getColumnNumber() == j && cell.getCellType() != CellType.BLANK)
                .map(Cell::calcCellDimension)
                .mapToDouble(Dimension2f::width)
                .max()
                .orElse(0.0);
        setColumnWidth(j, colWidth);
    }

    @Override
    public void autoSizeColumns() {
        final int n = getColumnCount();

        float[] colWidth = new float[n];
        Arrays.fill(colWidth, 0.0f);

        rows().flatMap(Row::cells)
                .filter(cell -> !cell.isEmpty())
                .forEach(cell -> {
                    int j = cell.getColumnNumber();
                    float width = cell.calcCellDimension().width();
                    colWidth[j] = Math.max(colWidth[j], width);
                });

        for (int j = 0; j < n; j++) {
            setColumnWidth(j, colWidth[j]);
        }
        layoutChanged();
    }

    @Override
    public void autoSizeRow(int i) {
        getRowIfExists(i).ifPresent(row -> {
            float rowHeight = (float) row.cells()
                    .filter(cell -> !cell.isEmpty())
                    .map(Cell::calcCellDimension)
                    .mapToDouble(Dimension2f::height)
                    .max()
                    .orElse(0.0);
            setRowHeight(i, rowHeight);
        });
    }

}
