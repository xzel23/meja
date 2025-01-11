package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.ui.CellRenderer;
import com.dua3.meja.ui.SegmentViewDelegate;
import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxGraphics;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.fx.PlatformHelper;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Scale2f;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;
import javafx.scene.control.Skinnable;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Affine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.stream.IntStream;

/**
 * A custom cell implementation for rendering rows in a spreadsheet. It extends the IndexedCell class and overrides several methods to customize the appearance and behavior of the
 *  cell.
 */
public class FxRow extends IndexedCell<RowProxy> {
    private static final Logger LOG = LogManager.getLogger(FxRow.class);
    public static final Affine IDENTITY_TRANSFORMATION = FxUtil.convert(AffineTransformation2f.IDENTITY);

    private final ObservableList<Row> rows;

    private final FxSheetViewDelegate sheetViewDelegate;
    private final SegmentViewDelegate segmentViewDelegate;

    /**
     * A single row in a sheet view.
     *
     * @param rows the list of rows in the sheet view
     * @param svDelegate the segment view delegate for the sheet view
     */
    public FxRow(ObservableList<Row> rows, SegmentViewDelegate svDelegate) {
        LOG.trace("instance created");

        this.rows = rows;
        this.segmentViewDelegate = svDelegate;
        this.sheetViewDelegate = (FxSheetViewDelegate) svDelegate.getSheetViewDelegate();

        setText(null);
        setGraphic(new Canvas(1, 1));

        setOnMouseClicked(this::onMouseClicked);
    }

    /**
     * Returns the height of a row in pixels.
     *
     * @return the height of the row in pixels
     */
    public float getRowHeightInPixels() {
        return sheetViewDelegate.getScale().sy() * getRowHeightInPoints();
    }

    /**
     * Returns the height of a row in points.
     *
     * @return the height of the row in points
     */
    public float getRowHeightInPoints() {
        PlatformHelper.checkApplicationThread();

        sheetViewDelegate.updateLayout();
        int idx = getIndex();

        if (getSegmentViewDelegate().isAboveSplit()) {
            if (idx < 0) {
                return getSheetViewDelegate().getDefaultRowHeightInPoints();
            } else if (idx == 0) {
                return sheetViewDelegate.getColumnLabelHeightInPoints();
            } else {
                idx--;
                if (idx < rows.size()) {
                    return sheetViewDelegate.getRowHeightInPoints(rows.get(idx).getRowNumber());
                } else {
                    return sheetViewDelegate.getDefaultRowHeightInPoints();
                }
            }
        } else {
            return (idx < 0 || idx >= rows.size()
                    ? sheetViewDelegate.getDefaultRowHeightInPoints()
                    : sheetViewDelegate.getRowHeightInPoints(rows.get(idx).getRowNumber()));
        }
    }

    @Override
    protected double computePrefHeight(double v) {
        return Math.round(getRowHeightInPixels());
    }

    @Override
    protected double computePrefWidth(double v) {
        return segmentViewDelegate.getWidthInPixels();
    }

    @Override
    protected double computeMinHeight(double width) {
        return Math.max(1, super.computeMinHeight(width));
    }

    @Override
    public void updateIndex(int i) {
        LOG.trace("updateIndex({})", i);

        super.updateIndex(i);

        if (i < 0) {
            // empty row
            updateItem(RowProxy.ROW_PROXY_EMPTY, true);
            return;
        } else if (segmentViewDelegate.isAboveSplit()) {
            // row is above split
            if (i == 0) {
                // row 0 is the column headers
                updateItem(RowProxy.ROW_PROXY_CLOLUMN_LABELS, false);
                return;
            } else {
                i--; // adjust i because of inserted column header row
                if (i == rows.size()) {
                    // row is the split line
                    updateItem(RowProxy.ROW_PROXY_SPLIT_LINE, false);
                    return;
                }
            }
        }

        if (i < rows.size()) {
            updateItem(RowProxy.row(rows.get(i)), false);
        } else {
            int j = i + 1 - rows.size();
            int rowNumber = j + (rows.isEmpty() ? 0 : rows.get(rows.size() - 1).getRowNumber());
            updateItem(RowProxy.virtualRow(rowNumber), false);
        }
    }

    @Override
    protected void updateItem(@Nullable RowProxy item, boolean empty) {
        LOG.trace("updateItem({}, {})", item, empty);

        assert Platform.isFxApplicationThread() : "not on FxApplication thread";

        //noinspection DataFlowIssue
        super.updateItem(item, empty);

        render();
    }

    /**
     * Retrieves the sheet view delegate associated with this FxRow instance.
     *
     * @return the sheet view delegate associated with this FxRow instance
     */
    public FxSheetViewDelegate getSheetViewDelegate() {
        return sheetViewDelegate;
    }

    /**
     * Retrieves the SegmentViewDelegate associated with this FxRow instance.
     *
     * @return the segment view delegate associated this FxRow instance
     */
    public SegmentViewDelegate getSegmentViewDelegate() {
        return segmentViewDelegate;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new Skin<>() {
            @Override
            public Skinnable getSkinnable() {
                return FxRow.this;
            }

            @Override
            public Node getNode() {
                return getGraphic();
            }

            public void dispose() {
            }
        };
    }

    private void render() {
        sheetViewDelegate.readLock().lock();
        try {
            RowProxy item = getItem();
            switch (item.getType()) {
                case ROW -> renderRow(item.getRowNumber(), item.getRow());
                case EMPTY -> renderEmpty();
                case CLOUMN_LABELS -> renderColumnLabels();
                case SPLIT_LINE -> renderSplitLine();
            }
        } finally {
            sheetViewDelegate.readLock().unlock();
        }
    }

    private void renderEmpty() {
        Canvas canvas = (Canvas) getGraphic();
        canvas.setVisible(false);
    }

    private GraphicsContext prepareGraphicsContext(float w, float h) {
        Canvas canvas = (Canvas) getGraphic();

        canvas.setWidth(w);
        canvas.setHeight(h);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setTransform(IDENTITY_TRANSFORMATION);

        gc.clearRect(0, 0, w, h);

        canvas.setVisible(true);

        return gc;
    }

    private void renderRow(int rowNumber, @Nullable Row row) {
        float w = segmentViewDelegate.getWidthInPixels();
        float h = getRowHeightInPixels();

        GraphicsContext gc = prepareGraphicsContext(w, h);

        FxSheetViewDelegate sheetViewDelegate = getSheetViewDelegate();
        sheetViewDelegate.updateLayout();

        Scale2f s = sheetViewDelegate.getScale();

        float translateX = s.sx() * getSegmentViewDelegate().getXOffset();
        float translateY = segmentViewDelegate.isAboveSplit()
                ? -s.sy() * sheetViewDelegate.getRowPos(getSegmentViewDelegate().getStartRow())
                : 0;
        gc.setTransform(s.sx(), 0, 0, s.sy(), translateX, translateY);

        CellRenderer cellRenderer = new CellRenderer(sheetViewDelegate);

        try (FxGraphics g = new FxGraphics(gc, w, h)) {
            float sheetX = sheetViewDelegate.getColumnPos(segmentViewDelegate.getStartColumn());
            float sheetWidth = sheetViewDelegate.getSheetWidthInPoints();
            float rowHeight = sheetViewDelegate.getRowHeightInPoints(rowNumber);

            // clear background
            g.setFill(sheetViewDelegate.getBackground());
            g.fillRect(sheetX, 0, sheetWidth, rowHeight);

            float rowHeightInPoints = getRowHeightInPoints();

            // draw grid lines
            g.setStroke(sheetViewDelegate.getGridColor(), sheetViewDelegate.get1PxHeightInPoints());
            g.strokeLine(sheetX, rowHeightInPoints, sheetWidth, rowHeightInPoints);

            g.setStroke(sheetViewDelegate.getGridColor(), sheetViewDelegate.get1PxWidthInPoints());
            for (int j = segmentViewDelegate.getStartColumn(); j <= segmentViewDelegate.getEndColumn(); j++) {
                float x = sheetViewDelegate.getColumnPos(j);
                g.strokeLine(x, 0, x, rowHeight);
            }

            int i = rowNumber;

            g.setTransformation(AffineTransformation2f.translate(0, -sheetViewDelegate.getRowPos(i)));

            //  draw row label
            Rectangle2f r = new Rectangle2f(
                    -sheetViewDelegate.getRowLabelWidthInPoints(),
                    sheetViewDelegate.getRowPos(i),
                    sheetViewDelegate.getRowLabelWidthInPoints(),
                    sheetViewDelegate.getRowPos(i + 1) - sheetViewDelegate.getRowPos(i)
            );

            sheetViewDelegate.drawLabel(g, r, sheetViewDelegate.getRowName(i));

            //  iterate over columns
            for (int j = segmentViewDelegate.getStartColumn(); j < segmentViewDelegate.getEndColumn(); j++) {
                //  draw row label
                r = new Rectangle2f(
                        sheetViewDelegate.getColumnPos(j),
                        -sheetViewDelegate.getColumnLabelHeightInPoints(),
                        sheetViewDelegate.getColumnPos(j + 1) - sheetViewDelegate.getColumnPos(j),
                        sheetViewDelegate.getColumnLabelHeightInPoints()
                );
                sheetViewDelegate.drawLabel(g, r, sheetViewDelegate.getColumnName(j));

                // draw cell
                if (row != null) {
                    row.getCellIfExists(j).ifPresent(cell -> cellRenderer.drawCell(g, cell.getLogicalCell()));
                }
            }

            if (segmentViewDelegate.hasVLine()) {
                g.setStroke(Color.BLACK, sheetViewDelegate.get1PxWidthInPoints());
                float x = getSheetViewDelegate().getColumnPos(segmentViewDelegate.getEndColumn()) + getSheetViewDelegate().get1PxWidthInPoints();
                float y = sheetViewDelegate.getRowPos(i);
                g.strokeLine(x, y, x, y + getRowHeightInPoints());
            }

            sheetViewDelegate.getCurrentLogicalCell().ifPresent(cell -> {
                if (cell.getRowNumber() - 1 <= i && cell.getRowNumber() + cell.getVerticalSpan() >= i) {
                    cellRenderer.drawSelection(g, cell);
                }
            });
        }
    }

    private void renderColumnLabels() {
        FxSheetViewDelegate sheetViewDelegate = getSheetViewDelegate();
        sheetViewDelegate.updateLayout();

        float w = segmentViewDelegate.getWidthInPixels();
        float h = sheetViewDelegate.getColumnLabelHeightInPixels();

        GraphicsContext gc = prepareGraphicsContext(w, h);

        Scale2f s = sheetViewDelegate.getScale();

        float translateX = s.sx() * getSegmentViewDelegate().getXOffset();
        float translateY = 0;
        gc.setTransform(s.sx(), 0, 0, s.sy(), translateX, translateY);

        try (FxGraphics g = new FxGraphics(gc, w, h)) {
            // clear background
            g.setFill(sheetViewDelegate.getBackground());
            g.fillRect(g.getBounds());

            float heightInPoints = sheetViewDelegate.getColumnLabelHeightInPoints();

            //  iterate over columns
            for (int j = segmentViewDelegate.getStartColumn(); j < segmentViewDelegate.getEndColumn(); j++) {
                //  draw row label
                Rectangle2f r = new Rectangle2f(
                        sheetViewDelegate.getColumnPos(j),
                        0,
                        sheetViewDelegate.getColumnPos(j + 1) - sheetViewDelegate.getColumnPos(j),
                        heightInPoints
                );
                sheetViewDelegate.drawLabel(g, r, sheetViewDelegate.getColumnName(j));
            }

            // draw split line
            if (segmentViewDelegate.hasVLine()) {
                g.setStroke(Color.BLACK, sheetViewDelegate.get1PxWidthInPoints());
                float x = getSheetViewDelegate().getColumnPos(segmentViewDelegate.getEndColumn()) + getSheetViewDelegate().get1PxWidthInPoints();
                float y = 0;
                g.strokeLine(x, y, x, y + heightInPoints);
            }
        }
    }

    private void renderSplitLine() {
        float w = segmentViewDelegate.getWidthInPixels();
        float h = 1;

        GraphicsContext gc = prepareGraphicsContext(w, h);

        try (FxGraphics g = new FxGraphics(gc, w, h)) {
            g.setFill(Color.BLACK);
            g.fillRect(g.getBounds());
        }
    }

    private void onMouseClicked(MouseEvent evt) {
        LOG.debug("onMouseClicked({})", evt);

        sheetViewDelegate.requestFocus();

        double xSheet = getSheetViewDelegate().getColumnPos(segmentViewDelegate.getStartColumn()) + evt.getX() / sheetViewDelegate.getScale().sx();

        //noinspection OptionalOfNullableMisuse - false positive
        int i = Optional.ofNullable(getItem()).map(RowProxy::getRow).map(Row::getRowNumber).orElse(-1);
        int j = IntStream.range(segmentViewDelegate.getStartColumn(), segmentViewDelegate.getEndColumn())
                .filter(k -> getSheetViewDelegate().getColumnPos(k) <= xSheet && xSheet <= getSheetViewDelegate().getColumnPos(k + 1))
                .findFirst()
                .orElse(-1);
        LOG.trace("onMouseClicked(): row #{}, column #{}", i, j);

        if (i >= 0 && j >= 0) {
            sheetViewDelegate.setCurrentCell(i, j);
            LOG.debug("onMouseClicked(): set current cell to {}", () -> sheetViewDelegate.getCurrentLogicalCell().map(Cell::getCellRef));
        }
    }
}
