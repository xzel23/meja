package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Row;
import com.dua3.meja.ui.SegmentView;
import com.dua3.meja.ui.SegmentViewDelegate;
import com.dua3.meja.ui.SheetView;
import com.dua3.utility.math.geometry.Scale2f;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.VirtualFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FxSegmentView extends Control implements SegmentView {

    private static final Logger LOG = LogManager.getLogger(FxSegmentView.class);

    public void refresh() {
        flow.refresh();
    }

    private static class FxSegmentViewSkin extends SkinBase<FxSegmentView> {
        protected FxSegmentViewSkin(FxSegmentView control) {
            super(control);
            getChildren().add(control.flow);
        }
    }

    static class VirtualFlowWithHiddenScrollBars<T extends IndexedCell<?>> extends VirtualFlow<T> {
        VirtualFlowWithHiddenScrollBars(SheetView.Quadrant quadrant) {
            getHbar().setPrefHeight(0);
            getHbar().setOpacity(0);
            getVbar().setPrefWidth(0);
            getVbar().setOpacity(0);
        }

        public ScrollBar getHScrollbar() {
            return getHbar();
        }

        public ScrollBar getVScrollbar() {
            return getVbar();
        }

        public void refresh() {
            reconfigureCells();;
        }
    }

    private final FxSheetViewDelegate sheetDelegate;
    private final SegmentViewDelegate segmentDelegate;
    private final SheetView.Quadrant quadrant;
    final VirtualFlowWithHiddenScrollBars<FxRow> flow;
    private final ObservableList<Row> rows;

    public FxSegmentView(FxSheetViewDelegate sheetDelegate, SheetView.Quadrant quadrant, ObservableList<Row> sheetRows) {
        this.flow = new VirtualFlowWithHiddenScrollBars<>(quadrant);
        this.sheetDelegate = sheetDelegate;
        this.segmentDelegate = new SegmentViewDelegate(this, sheetDelegate, quadrant);
        this.quadrant = quadrant;
        this.rows = filterRows(sheetRows, sheetDelegate.getSplitRow());

        setSkin(new FxSegmentViewSkin(this));
        setFocusTraversable(false);

        updateLayout();
    }

    /**
     * Get an {@link ObservableList} containing the rows belonging to this quadrant
     *
     * @param rows     all rows
     * @param splitRow the split row, i.e., rows above this row belong to the upper half
     * @return the filtered {@link ObservableList} of rows
     */
    public ObservableList<Row> filterRows(ObservableList<Row> rows, int splitRow) {
        return new FilteredList<>(rows, row -> quadrant.isTop() == (row.getRowNumber() < splitRow));
    }

    /**
     * Update layout, i.e., when the scale or row and/or column sizes change.
     */
    private void updateLayout() {
        sheetDelegate.updateLayout();
        segmentDelegate.updateLayout();

        sheetDelegate.getSheet().ifPresent(sheet -> {
            double widthInPixels = segmentDelegate.getWidthInPixels();
            double heightInPixels = segmentDelegate.getHeightInPixels();

            switch (quadrant) {
                case TOP_LEFT -> {
                    setMinSize(widthInPixels, heightInPixels);
                    setMaxSize(widthInPixels, heightInPixels);
                    setPrefSize(widthInPixels, heightInPixels);
                }
                case TOP_RIGHT -> {
                    setMinHeight(heightInPixels);
                    setMaxHeight(heightInPixels);
                    setPrefHeight(heightInPixels);
                    setPrefWidth(widthInPixels);
                }
                case BOTTOM_LEFT -> {
                    setMinWidth(widthInPixels);
                    setMaxWidth(widthInPixels);
                    setPrefWidth(widthInPixels);
                }
                case BOTTOM_RIGHT -> {
                    // nop
                }
            }

            flow.setCellFactory(f -> new FxRow(rows, segmentDelegate));
            flow.setCellCount(rows.size());
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
                svDelegate.onMousePressed(p.dx, p.dy);
            }
        });
         */
    }

    @Override
    public void setViewSizeOnDisplay(float widthInPoints, float heightIPoints) {
        Scale2f scale = sheetDelegate.getScale();
        setPrefSize(widthInPoints * scale.sx(), heightIPoints * scale.sy());
    }
}
