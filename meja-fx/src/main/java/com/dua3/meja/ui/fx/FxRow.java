package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.ui.CellRenderer;
import com.dua3.meja.ui.SegmentViewDelegate;
import com.dua3.meja.ui.SheetViewDelegate;
import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxGraphics;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.fx.PlatformHelper;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;
import javafx.scene.control.Skinnable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.transform.Affine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

/**
 * A custom cell implementation for rendering rows in a spreadsheet. It extends the IndexedCell class and overrides several methods to customize the appearance and behavior of the
 *  cell.
 */
public class FxRow extends IndexedCell<FxRow.Index> {
    private static final Logger LOG = LogManager.getLogger(FxRow.class);

    public static class FxRowGraphics extends AnchorPane {
        private final Canvas left;
        private final Canvas right;

        FxRowGraphics(FxRow fxRow) {
            super();
            left = new Canvas();
            right = new Canvas();

            getChildren().addAll(left, right);
//            getChildren().addAll(left, right);
            left.toFront();
            //right.toBack();

            right.translateXProperty().bind(
                    left.layoutXProperty()
                            .add(left.widthProperty())
                            .subtract(fxRow.fxSheetView.getHScrollbar().valueProperty()
                                    .multiply(fxRow.fxSheetView.sheetScaleXProperty())
                            )
            );
        }

        void setSize(SegmentViewDelegate segmentDelegateLeft, SegmentViewDelegate segmentDelegateRight, float rowY, float rowHeightInPoints) {
            SheetViewDelegate sheetViewDelegate = segmentDelegateLeft.getSheetViewDelegate();

            // --- left canvas settings ---
            left.setWidth(segmentDelegateLeft.getWidthInPixels());
            left.setHeight(rowHeightInPoints);

            float sx = sheetViewDelegate.getScale().sx();
            float sy = sheetViewDelegate.getScale().sy();
            float tx = sheetViewDelegate.getRowLabelWidthInPoints();
            float ty = -rowY;

            GraphicsContext g2dLeft = left.getGraphicsContext2D();

            g2dLeft.setTransform(IDENTITY_TRANSFORMATION);
            g2dLeft.scale(sx, sy);
            g2dLeft.translate(tx, ty);

            // --- right canvas settings ---
            right.setWidth(Math.max(getWidth() - segmentDelegateLeft.getWidthInPixels(), segmentDelegateRight.getWidthInPixels()));
            right.setHeight(rowHeightInPoints);

            tx = -segmentDelegateRight.getSheetViewDelegate().getSplitX();

            GraphicsContext g2dRight = right.getGraphicsContext2D();

            g2dRight.setTransform(IDENTITY_TRANSFORMATION);
            g2dRight.scale(sx, sy);
            g2dRight.translate(tx, ty);
        }

        /**
         * Updates the translation for the right part to create a scrolling effect within the viewport.
         *
         * @param scrollX The current scroll offset in the X direction.
         */
        void updateScrollX(double scrollX) {
            //translation.setX(-scrollX); // Apply negative scroll to translate
        }

        /**
         * draw a border around and a cross inside a Canvas for debugging.
         * @param canvas    the Canvas
         * @param color     the Color
         */
        private static void drawBorder(Canvas canvas, javafx.scene.paint.Color color) {
            GraphicsContext g2d = canvas.getGraphicsContext2D();
            Affine t = g2d.getTransform();
            g2d.setTransform(IDENTITY_TRANSFORMATION);
            g2d.setStroke(color);
            g2d.setLineWidth(1.0);
            g2d.strokeRect(0.0f, 0.0f, canvas.getWidth(), canvas.getHeight());
            g2d.strokeLine(0.0f, 0.0f, canvas.getWidth(), canvas.getHeight());
            g2d.strokeLine(0.0f, canvas.getHeight(), canvas.getWidth(), 0.0f);
            g2d.setTransform(t);
        }

        FxGraphics getGraphicsContext(Side side) {
            Canvas canvas = getCanvas(side);
            GraphicsContext g2d = canvas.getGraphicsContext2D();
            Affine transform = g2d.getTransform();
            FxGraphics fxGraphics = new FxGraphics(canvas) {
                @Override
                public void close() {
//                    drawBorder(left, javafx.scene.paint.Color.BLUE);
//                    drawBorder(right, javafx.scene.paint.Color.GREEN);
                    super.close();
                    g2d.setTransform(transform);
                }
            };
            fxGraphics.setStroke((side == Side.LEFT ? Color.GREEN : Color.BLUE), 1);
            fxGraphics.strokeRect(0.0f,0.0f, fxGraphics.getWidth(), fxGraphics.getHeight());

            return fxGraphics;
        }

        private Canvas getCanvas(Side side) {
            return switch (side) {
                case LEFT -> left;
                case RIGHT -> right;
                default -> throw new IllegalStateException("invalid value for side: " + side);
            };
        }
    }

    /**
     * The {@code Index} class is a record representing the position of a row within a table
     * or sheet-like structure. It encapsulates two distinct pieces of information:
     *
     * @param rowIndex The internal index of the row, used to access the row from the filtered list of rows.
     * @param row       The row of the sheet.
     */
    public record Index(int rowIndex, int rowNumber, @Nullable Row row) {}

    private static final Affine IDENTITY_TRANSFORMATION = FxUtil.convert(AffineTransformation2f.IDENTITY);

    private final FxSheetView fxSheetView;
    private final FxSegmentView fxSegmentView;

    private static final int ROW_INDEX_UNUSED = -1;
    private static final int ROW_INDEX_COLUMN_LABELS = -2;
    private static final int ROW_INDEX_SPLIT_LINE = -3;

    /**
     * A single row in a sheet view.
     *
     * @param fxSegmentView the segment view this instance belongs to
     */
    public FxRow(FxSegmentView fxSegmentView) {
        LOG.trace("instance created");

        this.fxSheetView = fxSegmentView.getSheetView();
        this.fxSegmentView = fxSegmentView;

        setText(null);
        setGraphic(new FxRowGraphics(this));

        setOnMouseClicked(this::onMouseClicked);
    }

    /**
     * Returns the height of a row in pixels.
     *
     * @return the height of the row in pixels
     */
    public float getRowHeightInPixels() {
        return fxSheetView.getDelegate().getScale().sy() * getRowHeightInPoints();
    }

    public float getWidthInPixels() {
        return fxSegmentView.getSegmentDelegateLeft().getWidthInPixels() + fxSegmentView.getSegmentDelegateRight().getWidthInPixels();
    }

    public float getWidthInPixels(Side side) {
        return getSegmentViewDelegate(side).getWidthInPixels();
    }

    /**
     * Returns the height of a row in points.
     *
     * @return the height of the row in points
     */
    public float getRowHeightInPoints() {
        Index item = getItem();
        return switch (item.rowIndex()) {
            case ROW_INDEX_COLUMN_LABELS -> fxSheetView.getDelegate().getColumnLabelHeightInPoints();
            case ROW_INDEX_SPLIT_LINE -> fxSheetView.getDelegate().getSplitLineHeightInPoints();
            case ROW_INDEX_UNUSED -> 0.0f;
            default -> {
                Row row = item.row();
                yield row != null
                        ? row.getRowHeight()
                        : getSegmentViewDelegate(Side.LEFT).isAboveSplit() && getIndex() == 0
                        ? fxSheetView.getDelegate().getColumnLabelHeightInPoints()
                        : fxSheetView.getDelegate().getDefaultRowHeightInPoints();
            }
        };
    }

    private SegmentViewDelegate getSegmentViewDelegate(Side side) {
        return switch (side) {
            case LEFT -> fxSegmentView.getSegmentDelegateLeft();
            case RIGHT -> fxSegmentView.getSegmentDelegateRight();
            default -> throw new IllegalStateException("invalid value for side: " + side);
        };
    }

    @Override
    protected double computePrefHeight(double v) {
        return Math.round(getRowHeightInPixels());
    }

    @Override
    protected double computePrefWidth(double v) {
        return fxSegmentView.getSegmentDelegateLeft().getWidthInPixels() + fxSegmentView.getSegmentDelegateRight().getWidthInPixels();
    }

    @Override
    protected double computeMinHeight(double width) {
        return Math.max(1, super.computeMinHeight(width));
    }

    @Override
    public void updateIndex(int i) {
        LOG.trace("updateIndex({})", i);

        PlatformHelper.checkApplicationThread();

        super.updateIndex(i);

        int rowIdx = i;
        if (i < 0) {
            // empty row
            updateItem(new Index(ROW_INDEX_UNUSED, -1, null), true);
            return;
        } else if (fxSegmentView.isAboveSplit()) {
            // row is above split
            if (i == 0) {
                // row 0 is the column headers
                updateItem(new Index(ROW_INDEX_COLUMN_LABELS, -1, null), false);
                return;
            } else {
                rowIdx--;
                if (i - 1 == fxSheetView.getDelegate().getSplitRow()) {
                    // row is the split line
                    updateItem(new Index(ROW_INDEX_SPLIT_LINE, -1, null), false);
                    return;
                }
            }
        }

        Row row = fxSheetView.getDelegate().getSheet().getRow(fxSegmentView.getSegmentDelegateLeft().getStartRow() + rowIdx);
        updateItem(new Index(rowIdx, row.getRowNumber(), row), false);
    }

    @Override
    protected void updateItem(Index item, boolean empty) {
        LOG.trace("updateItem({}, {})", item, empty);
        PlatformHelper.checkApplicationThread();
        super.updateItem(item, empty);
        render();
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
                setGraphic(null);
            }
        };
    }

    private void render() {
        LOG.trace("render()");
        PlatformHelper.checkApplicationThread();
        try (var __ = fxSheetView.getDelegate().readLock("FxRow.render()")) {
            if (!(getGraphic() instanceof FxRowGraphics fxrg)) {
                return;
            }

            Index item = getItem();
            float rowY = switch (item.rowIndex()) {
                case ROW_INDEX_COLUMN_LABELS, ROW_INDEX_SPLIT_LINE, ROW_INDEX_UNUSED -> 0;
                default -> fxSheetView.getDelegate().getRowPos(item.rowNumber());
            };

            fxrg.setSize(
                    fxSegmentView.getSegmentDelegateLeft(),
                    fxSegmentView.getSegmentDelegateRight(),
                    rowY,
                    getRowHeightInPixels()
            );
            switch (item.rowIndex()) {
                case ROW_INDEX_UNUSED -> renderEmpty(fxrg);
                case ROW_INDEX_COLUMN_LABELS -> renderColumnLabels(fxrg);
                case ROW_INDEX_SPLIT_LINE -> renderSplitLine(fxrg);
                default -> renderRow(fxrg);
            }

            drawBorder(fxrg.left, javafx.scene.paint.Color.GREEN);
            drawBorder(fxrg.right, javafx.scene.paint.Color.BLUE);
        }
    }

    private void renderRow(FxRowGraphics fxrg) {
        renderRow(Side.LEFT, fxrg);
        renderRow(Side.RIGHT, fxrg);
    }

    private void renderSplitLine(FxRowGraphics fxrg) {
        renderSplitLine(Side.LEFT, fxrg);
        renderSplitLine(Side.RIGHT, fxrg);
    }

    private void renderColumnLabels(FxRowGraphics fxrg) {
        renderColumnLabels(Side.LEFT, fxrg);
        renderColumnLabels(Side.RIGHT, fxrg);
    }

    private void renderEmpty(FxRowGraphics fxrg) {
        fxrg.setVisible(false);
    }

    private void renderRow(Side side, FxRowGraphics fxrg) {
        fxrg.setVisible(true);

        Index index = getItem();
        int idx = index.rowIndex();
        Row row = index.row();
        assert row != null;

        FxSheetViewDelegate sheetViewDelegate = fxSheetView.getDelegate();
        SegmentViewDelegate segmentViewDelegate = getSegmentViewDelegate(side);

        int i = index.rowNumber();
        float x = sheetViewDelegate.getColumnPos(segmentViewDelegate.getStartColumn());
        float w = segmentViewDelegate.getWidthInPoints();
        float y = sheetViewDelegate.getRowPos(i);
        float h = row.getRowHeight();

        try (FxGraphics g = fxrg.getGraphicsContext(side)) {
            // clear background
            g.setFill(sheetViewDelegate.getBackground());
            g.fillRect(x, y, w, h);

            // draw grid lines
            g.setStroke(sheetViewDelegate.getGridColor(), sheetViewDelegate.get1PxHeightInPoints());
            g.strokeLine(x, y + h, w, y + h); // horizontal

            g.setStroke(sheetViewDelegate.getGridColor(), sheetViewDelegate.get1PxWidthInPoints());
            for (int j = segmentViewDelegate.getStartColumn(); j <= segmentViewDelegate.getEndColumn(); j++) {
                float xj = sheetViewDelegate.getColumnPos(j);
                g.strokeLine(xj, y, xj, y + h); // vertical
            }

            //  draw row label
            if (segmentViewDelegate.getStartColumn() == 0) {
                float rowLabelWidthInPoints = sheetViewDelegate.getRowLabelWidthInPoints();
                Rectangle2f r = new Rectangle2f(
                        x - rowLabelWidthInPoints,
                        y,
                        rowLabelWidthInPoints,
                        h
                );
                sheetViewDelegate.drawLabel(g, r, sheetViewDelegate.getRowName(i));
            }

            //  iterate over columns and draw cells
            CellRenderer cellRenderer = new CellRenderer(sheetViewDelegate);
            for (int j = segmentViewDelegate.getStartColumn(); j < segmentViewDelegate.getEndColumn(); j++) {
                row.getCellIfExists(j).ifPresent(cell -> cellRenderer.drawCell(g, cell.getLogicalCell()));
            }

            // draw the vertical split line
            if (segmentViewDelegate.hasVLine()) {
                g.setFill(Color.BLACK);
                float xSplit = fxSheetView.getDelegate().getSplitX();
                g.fillRect(xSplit, y, sheetViewDelegate.getSplitLineWidthInPoints(), h);
            }

            Cell cell = sheetViewDelegate.getCurrentLogicalCell();
            if (cell.getRowNumber() - 1 <= i && cell.getRowNumber() + cell.getVerticalSpan() >= i) {
                cellRenderer.drawSelection(g, cell);
            }
        }
    }

    private void renderColumnLabels(Side side, FxRowGraphics fxrg) {
        SegmentViewDelegate segmentViewDelegate = getSegmentViewDelegate(side);

        try (FxGraphics g = fxrg.getGraphicsContext(side)) {
            float h = fxSheetView.getDelegate().getColumnLabelHeightInPoints();
            float y = 0;
            //  iterate over columns
            for (int j = segmentViewDelegate.getStartColumn(); j < segmentViewDelegate.getEndColumn(); j++) {
                //  draw row label
                float xj = fxSheetView.getDelegate().getColumnPos(j);
                float wj = fxSheetView.getDelegate().getColumnPos(j + 1) - xj;
                Rectangle2f r = new Rectangle2f(
                        xj,
                        y,
                        wj,
                        h
                );
                fxSheetView.getDelegate().drawLabel(g, r, fxSheetView.getDelegate().getColumnName(j));
            }

            // draw split line
            if (segmentViewDelegate.hasVLine()) {
                g.setFill(Color.BLACK);
                float xSplit = fxSheetView.getDelegate().getColumnPos(segmentViewDelegate.getEndColumn());
                g.fillRect(xSplit, y, fxSheetView.getDelegate().getSplitLineWidthInPoints(), h);
            }
        }
    }

    private void renderSplitLine(Side side, FxRowGraphics fxrg) {
        try (FxGraphics g = fxrg.getGraphicsContext(side)) {
            g.setFill(Color.BLACK);
            float sheetX = - getSegmentViewDelegate(side).getXOffset();
            float sheetWidth = fxSheetView.getDelegate().getSheetWidthInPoints();
            float rowHeight = getRowHeightInPoints();
            g.fillRect(sheetX, 0, sheetWidth, rowHeight);
        }
    }

    private void onMouseClicked(MouseEvent evt) {
        LOG.debug("onMouseClicked({})", evt);

        fxSheetView.requestFocus();
/*
        double xSheet = sheetViewDelegate.getColumnPos(segmentViewDelegate.getStartColumn()) + evt.getX() / sheetViewDelegate.getScale().sx();

        int i = getItem().rowNumber();
        int j = IntStream.range(segmentViewDelegate.getStartColumn(), segmentViewDelegate.getEndColumn())
                .filter(k -> {
                    if (!(sheetViewDelegate.getColumnPos(k) <= xSheet)) return false;
                    return xSheet <= sheetViewDelegate.getColumnPos(k + 1);
                })
                .findFirst()
                .orElse(-1);
        LOG.trace("onMouseClicked(): row #{}, column #{}", i, j);

        if (i >= 0 && j >= 0) {
            sheetViewDelegate.setCurrentCell(i, j);
            LOG.debug("onMouseClicked(): set current cell to {}", () -> sheetViewDelegate.getCurrentLogicalCell().getCellRef());
        }
 */
    }

    /**
     * draw a border around and a cross inside a Canvas for debugging.
     * @param canvas    the Canvas
     * @param color     the Color
     */
    private static void drawBorder(Canvas canvas, javafx.scene.paint.Color color) {
        GraphicsContext g2d = canvas.getGraphicsContext2D();
        Affine t = g2d.getTransform();
        g2d.setTransform(IDENTITY_TRANSFORMATION);
        g2d.setStroke(color);
        g2d.setLineWidth(1.0);
        g2d.strokeRect(0.0f, 0.0f, canvas.getWidth(), canvas.getHeight());
        g2d.strokeLine(0.0f, 0.0f, canvas.getWidth(), canvas.getHeight());
        g2d.strokeLine(0.0f, canvas.getHeight(), canvas.getWidth(), 0.0f);
        g2d.setTransform(t);
    }
}
