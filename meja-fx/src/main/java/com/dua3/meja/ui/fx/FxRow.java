package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.ui.CellRenderer;
import com.dua3.meja.ui.SegmentViewDelegate;
import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxGraphics;
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
        LOG.trace("FxRow()");
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
        assert Platform.isFxApplicationThread() : "not on FxApplication thread";

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
        } else if (segmentViewDelegate.isAboveSplit()) {
            // row is above split
            if (i == 0) {
                // row 0 is the column headers
                updateItem(RowProxy.ROW_PROXY_CLOLUMN_LABELS, false);
            } else {
                i--; // adjust i because of inserted column header row
                if (i == rows.size()) {
                    updateItem(RowProxy.ROW_PROXY_SPLIT_LINE, false);
                } else {
                    Row row = i < rows.size() ? rows.get(i) : null;
                    updateItem(RowProxy.row(row), row == null);
                }
            }
        } else {
            // row is below split
            Row row = i < rows.size() ? rows.get(i) : null;
            updateItem(RowProxy.row(row), row == null);
        }
    }

    @Override
    protected void updateItem(@Nullable RowProxy item, boolean empty) {
        LOG.trace("updateItem({}, {})", item, empty);

        assert Platform.isFxApplicationThread() : "not on FxApplication thread";

        //noinspection DataFlowIssue
        super.updateItem(item, empty);

        render();

        LOG.trace("bounds: {}", getLayoutBounds());
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
        RowProxy item = getItem();
        switch (item.getType()) {
            case ROW -> renderRow(item.getRow());
            case EMPTY -> {}
            case CLOUMN_LABELS -> renderColumnLabels();
            case SPLIT_LINE -> renderSplitLine();
        }
    }

    private void renderRow(Row row) {
        float w = segmentViewDelegate.getWidthInPixels();
        float h = getRowHeightInPixels();

        Canvas canvas = (Canvas) getGraphic();

        canvas.setWidth(w);
        canvas.setHeight(h);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        FxSheetViewDelegate sheetViewDelegate = getSheetViewDelegate();
        sheetViewDelegate.updateLayout();

        Scale2f s = sheetViewDelegate.getScale();

        float translateX = s.sx() * getSegmentViewDelegate().getXOffset();
        float translateY = segmentViewDelegate.isAboveSplit()
                ? -s.sy() * sheetViewDelegate.getRowPos(getSegmentViewDelegate().getStartRow())
                : 0;
        gc.setTransform(s.sx(), 0, 0, s.sy(), translateX, translateY);

        CellRenderer cellRenderer = new CellRenderer(sheetViewDelegate);

        try (FxGraphics g = new FxGraphics(gc, (float) canvas.getWidth(), (float) canvas.getHeight())) {
            // clear background
            g.setFill(sheetViewDelegate.getBackground());
            g.fillRect(g.getBounds());

            float widthInPoints = segmentViewDelegate.getWidthInPoints();
            float rowHeightInPoints = getRowHeightInPoints();

            // draw grid lines
            g.setStroke(sheetViewDelegate.getGridColor(), sheetViewDelegate.get1PxHeightInPoints());
            float x = getSheetViewDelegate().getColumnPos(segmentViewDelegate.getStartColumn());
            float y = rowHeightInPoints;
            g.strokeLine(x, y, widthInPoints, y);

            g.setStroke(sheetViewDelegate.getGridColor(), sheetViewDelegate.get1PxWidthInPoints());
            for (int j = segmentViewDelegate.getStartColumn(); j <= segmentViewDelegate.getEndColumn(); j++) {
                x = sheetViewDelegate.getColumnPos(j);
                g.strokeLine(x, 0, x, h);
            }

            int i = row.getRowNumber();

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
                row.getCellIfExists(j).ifPresent(cell -> cellRenderer.drawCell(g, cell.getLogicalCell()));
            }

            if (segmentViewDelegate.hasVLine()) {
                g.setStroke(Color.BLACK, sheetViewDelegate.get1PxWidthInPoints());
                x = getSheetViewDelegate().getColumnPos(segmentViewDelegate.getEndColumn()) + getSheetViewDelegate().get1PxWidthInPoints();
                y = sheetViewDelegate.getRowPos(i);
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

        Canvas canvas = (Canvas) getGraphic();

        canvas.setWidth(w);
        canvas.setHeight(h);

        Scale2f s = sheetViewDelegate.getScale();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        float translateX = s.sx() * getSegmentViewDelegate().getXOffset();
        float translateY = 0;
        gc.setTransform(s.sx(), 0, 0, s.sy(), translateX, translateY);

        try (FxGraphics g = new FxGraphics(gc, (float) canvas.getWidth(), (float) canvas.getHeight())) {
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

        Canvas canvas = (Canvas) getGraphic();

        canvas.setWidth(w);
        canvas.setHeight(h);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        try (FxGraphics g = new FxGraphics(gc, (float) canvas.getWidth(), (float) canvas.getHeight())) {
            g.setFill(Color.BLACK);
            g.fillRect(g.getBounds());
        }
    }

    private void onMouseClicked(MouseEvent evt) {
        LOG.debug("onMouseClicked({})", evt);

        double xSheet = getSheetViewDelegate().getColumnPos(segmentViewDelegate.getStartColumn()) + evt.getX() / sheetViewDelegate.getScale().sx();

        //noinspection OptionalOfNullableMisuse - false positive
        int i = Optional.ofNullable(getItem()).map(RowProxy::getRow).map(Row::getRowNumber).orElse(-1);
        int j = IntStream.range(segmentViewDelegate.getStartColumn(), segmentViewDelegate.getEndColumn())
                .filter(k -> getSheetViewDelegate().getColumnPos(k) <= xSheet && xSheet <= getSheetViewDelegate().getColumnPos(k + 1))
                .findFirst()
                .orElse(-1);
        LOG.trace("onMouseClicked(): row #{}, column #{}, cell={}", i, j, sheetViewDelegate.getCurrentLogicalCell().map(Cell::getCellRef));

        if (i >= 0 && j >= 0) {
            sheetViewDelegate.setCurrentCell(i, j);
            LOG.debug("onMouseClicked(): set current cell to {}", sheetViewDelegate.getCurrentLogicalCell().map(Cell::getCellRef));
        }
    }
}
