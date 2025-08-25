package com.dua3.meja.model;

import com.dua3.meja.util.RectangularRegion;
import com.dua3.utility.concurrent.AutoLock;
import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The AbstractSheet class represents the abstract base class for a sheet in a spreadsheet.
 * It provides common functionality for working with sheets and is designed to be extended by
 * specific implementations for different spreadsheet formats.
 *
 * @param <S> the concrete type of Sheet (self-referential type parameter), extending AbstractSheet
 * @param <R> the concrete type of Row that this sheet contains, extending AbstractRow
 * @param <C> the concrete type of Cell that this sheet contains, extending AbstractCell
 */
public abstract class AbstractSheet<S extends AbstractSheet<S, R, C>, R extends AbstractRow<S, R, C>, C extends AbstractCell<S, R, C>> implements Sheet {

    private static final Logger LOG = LogManager.getLogger(AbstractSheet.class);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<RectangularRegion> mergedRegions = new ArrayList<>();

    private final SubmissionPublisher<SheetEvent> publisher = new SubmissionPublisher<>();
    private final Map<Flow.Subscriber<SheetEvent>, Flow.Subscription> subscriptions = new ConcurrentHashMap<>();

    /**
     * Constructor.
     */
    protected AbstractSheet() {
    }

    /**
     * Retrieves the abstract workbook associated with the sheet.
     *
     * @return the {@link AbstractWorkbook} instance associated with this sheet.
     */
    protected abstract AbstractWorkbook<S, R, C> getAbstractWorkbook();

    /**
     * Retrieves the abstract row at the specified index.
     *
     * @param rowIndex the index of the row to retrieve
     * @return the abstract row at the specified index
     */
    protected abstract R getAbstractRow(int rowIndex);

    /**
     * Retrieves the abstract cell located at the specified row and column
     * indices within the sheet.
     *
     * @param i the zero-based index of the row
     * @param j the zero-based index of the column
     * @return the abstract cell located at the specified row and column indices
     */
    protected C getAbstractCell(int i, int j) {
        return getAbstractRow(i).getAbstractCell(j);
    }

    /**
     * Retrieves the currently active abstract cell within the sheet.
     *
     * @return the currently active abstract cell of type {@code C}.
     */
    protected abstract C getCurrentAbstractCell();

    @Override
    public void subscribe(Flow.Subscriber<SheetEvent> subscriber) {
        Flow.Subscriber<SheetEvent> wrapper = new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscriptions.put(subscriber, subscription);
                subscriber.onSubscribe(subscription);
            }

            @Override
            public void onNext(SheetEvent item) {
                subscriber.onNext(item);
            }

            @Override
            public void onError(Throwable throwable) {
                subscriptions.remove(subscriber);
                subscriber.onError(throwable);
            }

            @Override
            public void onComplete() {
                subscriptions.remove(subscriber);
                subscriber.onComplete();
            }
        };
        publisher.subscribe(wrapper);
    }

    @Override
    public void unsubscribe(Flow.Subscriber<SheetEvent> subscriber) {
        Flow.Subscription s = subscriptions.remove(subscriber);
        if (s != null) {
            s.cancel();
        }
    }

    private void submit(SheetEvent event) {
        LOG.trace("submitting event: {}", event);
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
    protected void zoomChanged(float valueOld, float valueNew) {
        submit(new SheetEvent.ZoomChanged(this, valueOld, valueNew));
    }

    /**
     * Notifies subscribers that the split (freeze pane) in the sheet has changed.
     *
     * @param oldSplit the old split coordinates as a Pair of integers representing the row and column
     * @param newSplit the new split coordinates as a Pair of integers representing the row and column
     */
    protected void splitChanged(Pair<Integer, Integer> oldSplit, Pair<Integer, Integer> newSplit) {
        submit(new SheetEvent.SplitChanged(this, oldSplit, newSplit));
    }

    @Override
    public AutoLock readLock(String name) {
        return AutoLock.of(lock.readLock(), name);
    }

    @Override
    public AutoLock writeLock(String name) {
        return AutoLock.of(lock.writeLock(), name);
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
        C topLeftCell = getAbstractCell(cells.firstRow(), cells.firstColumn());
        for (int i = 0; i < spanY; i++) {
            for (int j = 0; j < spanX; j++) {
                C cell = getAbstractCell(cells.firstRow() + i, cells.firstColumn() + j);
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
    public Optional<RectangularRegion> getMergedRegion(int rowIndex, int colIndex) {
        return mergedRegions.stream().filter(rr -> rr.contains(rowIndex, colIndex)).findFirst();
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
                    R row = getAbstractRow(i);
                    for (int j = rr.firstColumn(); j <= rr.lastColumn(); j++) {
                        C cell = row.getAbstractCellOrNull(j);
                        if (cell != null) {
                            cell.removedFromMergedRegion();
                        }
                    }
                }
            }
        }
        LOG.debug("removed merged region at [{},{}]", rowNumber, columnNumber);
    }

    @Override
    public final Workbook getWorkbook() {
        return getAbstractWorkbook();
    }

    @Override
    public final Row getRow(int rowIndex) {
        return getAbstractRow(rowIndex);
    }

    @Override
    public final Cell getCell(int rowIndex, int colIndex) {
        LangUtil.checkArg(rowIndex >= 0, "invalid row number: %d", rowIndex);
        LangUtil.checkArg(colIndex >= 0, "invalid column number: %d", colIndex);
        return getAbstractCell(rowIndex, colIndex);
    }

    @Override
    public final Cell getCurrentCell() {
        return getCurrentAbstractCell();
    }

    @Override
    public final void autoSizeColumn(int colIndex) {
        float colWidth = (float) rows()
                .mapMultiToDouble((row, downstream) ->
                        row.cells().forEach(cell -> {
                            if (cell.getColumnNumber() == colIndex && !cell.isEmpty()) {
                                downstream.accept(cell.calcCellDimension().width());
                            }
                        }))
                .max()
                .orElse(0.0);
        setColumnWidth(colIndex, colWidth);
    }

    @Override
    public final void autoSizeColumns() {
        final int n = getColumnCount();

        float[] colWidth = new float[n];
        Arrays.fill(colWidth, 0.0f);

        rows().forEach(row ->
                row.cells().forEach(cell -> {
                    if (!cell.isEmpty()) {
                        int j = cell.getColumnNumber();
                        colWidth[j] = Math.max(colWidth[j], cell.calcCellDimension().width());
                    }
                }));

        for (int j = 0; j < n; j++) {
            setColumnWidth(j, colWidth[j]);
        }
        layoutChanged();
    }

    @Override
    public final void autoSizeRow(int rowIndex) {
        getRowIfExists(rowIndex).ifPresent(row -> {
            float rowHeight = (float) row.cells()
                    .mapMultiToDouble((cell, downstream) -> {
                        if (!cell.isEmpty()) {
                            downstream.accept(cell.calcCellDimension().height());
                        }
                    })
                    .max()
                    .orElse(0.0);
            setRowHeight(rowIndex, rowHeight);
        });
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "{" +
                "name=" + getSheetName() +
                '}';
    }
}
