package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Row;
import com.dua3.meja.ui.SegmentView;
import com.dua3.utility.math.geometry.Dimension2f;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.VirtualFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.IntStream;

public class FxSegmentView extends Control implements SegmentView {

    private static final Logger LOG = LogManager.getLogger(FxSegmentView.class);

    private class FxSegmentViewSkin extends SkinBase<FxSegmentView> {
        protected FxSegmentViewSkin(FxSegmentView control) {
            super(control);
            getChildren().add(control.flow);
        }
    }

    public enum Quadrant {
        TOP_LEFT(true, true),
        TOP_RIGHT(true, false),
        BOTTOM_LEFT(false, true),
        BOTTOM_RIGHT(false, false);

        private final boolean isTop;
        private final boolean isLeft;

        Quadrant(boolean isTop, boolean isLeft) {
            this.isTop = isTop;
            this.isLeft = isLeft;
        }

        public boolean isTop() {
            return isTop;
        }

        public boolean isLeft() {
            return isLeft;
        }

        public int startColumn(int columnCount, int splitColumn) {
            return isLeft ? 0 : splitColumn;
        }

        public int endColumn(int columnCount, int splitColumn) {
            return isLeft ? splitColumn: columnCount;
        }

        public int startRow(int rowCount, int splitRow) {
            return isTop ? 0 : splitRow;
        }

        public int endRow(int rowCount, int splitRow) {
            return isTop ? splitRow: rowCount;
        }

        public Dimension2f getDimension(FxSheetViewDelegate delegate) {
            int nc = delegate.getColumnCount();
            int sc = delegate.getSplitColumn();
            int nr = delegate.getRowCount();
            int sr = delegate.getSplitRow();

            float w = delegate.getColumnPos(endColumn(nc, sc)) - delegate.getColumnPos(startColumn(nc, sc));
            float h = delegate.getRowPos(endRow(nr, sr)) - delegate.getRowPos(startRow(nr, sr));

            return new Dimension2f(w, h);
        }

        /**
         * Get an {@link ObservableList} containing the rows belonging to this quadrant
         * @param rows all rows
         * @param splitRow the split row, i.e., rows above this row belong to the upper half
         * @return the filtered {@link ObservableList} of rows
         */
        public ObservableList<Row> filterRows(ObservableList<Row> rows, int splitRow) {
            return new FilteredList<>(rows, row -> isTop == (row.getRowNumber() < splitRow));
        }

        /**
         * Get a stream of the column numbers for this quadrant.
         * @param columnCount the total column count
         * @param splitColumn the split column
         * @return IntStream containing the column indexes for this quadrant
         */
        public IntStream filterColumns(int columnCount, int splitColumn) {
            return isLeft ? IntStream.range(0, splitColumn) : IntStream.range(splitColumn, columnCount);
        }
    }

    private final FxSheetViewDelegate svDelegate;
    private final Quadrant quadrant;
    private final VirtualFlow<FxRow> flow;
    private final ObservableList<Row> rows;

    public FxSegmentView(FxSheetViewDelegate svDelegate, Quadrant quadrant, ObservableList<Row> sheetRows) {
        this.svDelegate = svDelegate;
        this.quadrant = quadrant;
        this.rows = quadrant.filterRows(sheetRows, svDelegate.getSplitRow());
        this.flow = new VirtualFlow<>();

        setSkin(new FxSegmentViewSkin(this));

        svDelegate.getSheet().ifPresent(sheet -> {
            double width = IntStream.range(
                    quadrant.startColumn(svDelegate.getColumnCount(), svDelegate.getSplitColumn()),
                    quadrant.endColumn(svDelegate.getColumnCount(), svDelegate.getSplitColumn())
            ).mapToDouble(sheet::getColumnWidth).sum();
            double height = IntStream.range(
                    quadrant.startRow(svDelegate.getRowCount(), svDelegate.getSplitRow()),
                    quadrant.endRow(svDelegate.getRowCount(), svDelegate.getSplitRow())
            ).mapToDouble(sheet::getRowHeight).sum();

            flow.setCellFactory(f -> new FxRow(rows, svDelegate));
            flow.setCellCount(rows.size());

            switch (quadrant) {
                case TOP_LEFT -> {
                    setMinSize(width, height);
                    setMaxSize(width, height);
                    setPrefSize(width, height);
                }
                case TOP_RIGHT -> {
                    setMinHeight(height);
                    setMaxHeight(height);
                    setPrefHeight(height);
                }
                case BOTTOM_LEFT -> {
                    setMinWidth(width);
                    setMaxWidth(width);
                    setPrefWidth(width);
                }
                case BOTTOM_RIGHT -> {
                    // nop
                }
            }
        });

        if (quadrant != Quadrant.BOTTOM_RIGHT) {
            flow.setStyle(".scroll-bar { -fx-max-width: 0; -fx-max-height: 0; }");
        }
    }

    private void init() {
        // TODO listen to mouse events
        /*
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                translateMousePosition(p);
                svDelegate.onMousePressed(p.x, p.y);
            }
        });
         */
    }

    @Override
    public void setViewSizeOnDisplay(float w, float h) {

    }
}
