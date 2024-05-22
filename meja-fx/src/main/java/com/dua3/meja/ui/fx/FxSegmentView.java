package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SegmentView;
import com.dua3.meja.ui.SegmentViewDelegate;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.stream.IntStream;

public class FxSegmentView extends TableView<Row> implements SegmentView {

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

    public FxSegmentView(FxSheetViewDelegate delegate, Quadrant quadrant, ObservableList<Row> rows, int splitRow, int splitColumn) {
        super(quadrant.filterRows(rows, delegate.getSplitRow()));

        this.svDelegate = delegate;
        this.quadrant = quadrant;

        getColumns().clear();

        if (quadrant.isLeft) {
            TableColumn<Row, Integer> colRowNumber = new TableColumn<>("");
            colRowNumber.setCellValueFactory(cdf -> new SimpleObjectProperty<>(cdf.getValue().getRowNumber() + 1));
            getColumns().add(colRowNumber);
        }

        svDelegate.getSheet().ifPresent(sheet -> {
            getColumns().addAll(
                    quadrant.filterColumns(delegate.getColumnCount(), delegate.getSplitColumn())
                        .mapToObj(j -> {
                            TableColumn<Row, Cell> col = new TableColumn<>(Sheet.getColumnName(j));
                            col.setCellValueFactory(cdf -> new SimpleObjectProperty<>(cdf.getValue().getCell(j)));
                            return col;
                        })
                        .toList()
            );
        });
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
    public SegmentViewDelegate getDelegate() {
        return null;
    }

    @Override
    public Sheet getSheet() {
        return null;
    }

    @Override
    public void setViewSizeOnDisplay(float w, float h) {

    }
}
