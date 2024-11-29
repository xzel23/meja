package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Row;
import com.dua3.meja.ui.SegmentView;
import com.dua3.meja.ui.SegmentViewDelegate;
import com.dua3.meja.ui.SheetView;
import com.dua3.utility.math.geometry.Scale2f;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.VirtualFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * FxSegmentView is a JavaFX control that implements the {@link SegmentView} interface,
 * representing a view of a segment of a sheet focused within a specific quadrant.
 * The class manages layout and interactions for a section of rows displayed in a grid-like
 * interface with customization options for scaling and displaying without scrollbars.
 */
public class FxSegmentView extends Control implements SegmentView {

    private static final Logger LOG = LogManager.getLogger(FxSegmentView.class);

    public void refresh() {
        flow.refresh();
    }

    /**
     * Requests a layout update for a specific row within the segment view.
     *
     * @param i the index of the row for which the layout update is requested
     */
    public void requestLayoutForRow(int i) {
        int flowIndex = getFlowIndex(i);
        if (flowIndex >= 0) {
            flow.getCell(flowIndex).requestLayout();
        }
    }

    /**
     * Retrieves the index of the flow for a given row index.
     *
     * @param rowIndex the index of the row for which to get the flow index.
     * @return the flow index if the row is found; -1 otherwise.
     */
    private int getFlowIndex(int rowIndex) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).getRowNumber() == rowIndex) {
                return i;
            }
        }
        return -1;
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

        /**
         * Retrieves the horizontal scrollbar associated with the virtual flow.
         *
         * @return the horizontal ScrollBar instance used in the VirtualFlow.
         */
        public ScrollBar getHScrollbar() {
            return getHbar();
        }

        /**
         * Retrieves the vertical scrollbar associated with this virtual flow.
         *
         * @return the vertical ScrollBar object.
         */
        public ScrollBar getVScrollbar() {
            return getVbar();
        }

        /**
         * Refreshes the virtual flow by rebuilding its cells.
         * This method is typically called to update the visual representation of the
         * cells in the virtual flow after changes have been made. It ensures that all
         * cells are reconstructed and displayed according to the current data and state.
         */
        public void refresh() {
            rebuildCells();
        }
    }

    private final FxSheetViewDelegate sheetDelegate;
    private final SegmentViewDelegate segmentDelegate;
    private final SheetView.Quadrant quadrant;
    final VirtualFlowWithHiddenScrollBars<FxRow> flow;
    private final ObservableList<Row> rows;

    public FxSegmentView(FxSheetViewDelegate sheetDelegate, SheetView.Quadrant quadrant, ObservableList<Row> sheetRows) {
        this.quadrant = quadrant;
        this.sheetDelegate = sheetDelegate;
        this.segmentDelegate = new SegmentViewDelegate(this, sheetDelegate, quadrant);
        this.rows = filterRows(sheetRows, quadrant, sheetDelegate.getSplitRow());
        this.flow = new VirtualFlowWithHiddenScrollBars<>(quadrant);

        updateLayout();

        flow.setCellFactory(f -> new FxRow(rows, segmentDelegate));
        flow.setCellCount(rows.size());

        FxSegmentViewSkin skin = new FxSegmentViewSkin(this);
        setSkin(skin);

        setFocusTraversable(false);
    }

    /**
     * Get an {@link ObservableList} containing the rows belonging to this quadrant
     *
     * @param rows     all rows
     * @param splitRow the split row, i.e., rows above this row belong to the upper half
     * @return the filtered {@link ObservableList} of rows
     */
    private static ObservableList<Row> filterRows(ObservableList<Row> rows, SheetView.Quadrant quadrant, int splitRow) {
        return new FilteredList<>(rows, row -> quadrant.isTop() == (row.getRowNumber() < splitRow));
    }

    /**
     * Update layout, i.e., when the scale or row and/or column sizes change.
     */
    private void updateLayout() {
        assert Platform.isFxApplicationThread() : "not on FXApplicationThread";

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
        });
    }

    @Override
    public void setViewSizeOnDisplay(float widthInPoints, float heightIPoints) {
        Scale2f scale = sheetDelegate.getScale();
        setPrefSize(widthInPoints * scale.sx(), heightIPoints * scale.sy());
    }
}
