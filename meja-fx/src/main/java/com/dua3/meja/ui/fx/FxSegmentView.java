package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Row;
import com.dua3.meja.ui.SegmentView;
import com.dua3.meja.ui.SegmentViewDelegate;
import com.dua3.meja.ui.SheetView;
import com.dua3.utility.fx.PlatformHelper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

/**
 * FxSegmentView is a JavaFX control that implements the {@link SegmentView} interface,
 * representing a view of a segment of a sheet focused within a specific quadrant.
 * The class manages layout and interactions for a section of rows displayed in a grid-like
 * interface with customization options for scaling and displaying without scrollbars.
 */
public class FxSegmentView extends Control implements SegmentView {

    private static final Logger LOG = LogManager.getLogger(FxSegmentView.class);

    private final FxSheetView sheetView;

    /**
     * Refreshes the segment view by invoking the underlying flow's refresh method.
     * This method ensures the visual representation of the segment view remains
     * up to date by rebuilding the virtual flow's cells. It is typically used
     * after updates to the data or layout to guarantee the correctness of the displayed content.
     */
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
            Row row = rows.get(i);
            if (row != null && row.getRowNumber() == rowIndex) {
                return i;
            }
        }
        return -1;
    }

    public boolean isAboveSplit() {
        return segmentDelegateLeft.isAboveSplit();
    }

    private static class FxSegmentViewSkin extends SkinBase<FxSegmentView> {
        protected FxSegmentViewSkin(FxSegmentView control) {
            super(control);
            getChildren().add(control.flow);
        }
    }

    private final SegmentViewDelegate segmentDelegateLeft;
    private final SegmentViewDelegate segmentDelegateRight;
    final VirtualFlowWithHiddenScrollBars<FxRow> flow;
    private final ObservableList<@Nullable Row> rows;

    /**
     * Constructs an instance of FxSegmentView, responsible for displaying either the top (above split) or
     * bottom (below split) part of a sheet in the user interface.
     *
     * @param fxSheetView the {@link FxSheetView} this segment belongs to
     * @param side        use {@link Side#TOP} for the top segment, {@link Side#BOTTOM} for the bottom segment
     * @param svDelegate  the delegate responsible for accessing data specific to the {@link SheetView}
     * @param rows        the observable list of the rows to display in this quadrant.
     */
    public FxSegmentView(FxSheetView fxSheetView, Side side, FxSheetViewDelegate svDelegate, ObservableList<Row> rows) {
        LOG.trace("FxSegmentView()");

        SheetView.Quadrant quadrantLeft = switch (side) {
            case TOP -> SheetView.Quadrant.TOP_LEFT;
            case BOTTOM -> SheetView.Quadrant.BOTTOM_LEFT;
            default -> throw new IllegalStateException("invalid side: " + side);
        };
        SheetView.Quadrant quadrantRight = switch (side) {
            case TOP -> SheetView.Quadrant.TOP_RIGHT;
            case BOTTOM -> SheetView.Quadrant.BOTTOM_RIGHT;
            default -> throw new IllegalStateException("invalid side: " + side);
        };

        this.sheetView = fxSheetView;
        this.segmentDelegateLeft = new SegmentViewDelegate(this, svDelegate, quadrantLeft);
        this.segmentDelegateRight = new SegmentViewDelegate(this, svDelegate, quadrantRight);
        this.rows = rows;
        this.flow = new VirtualFlowWithHiddenScrollBars<>(true, true);

        updateLayout();

        flow.setCellFactory(f -> new FxRow(this));
        flow.setCellCount(rows.size());

        this.rows.addListener((ListChangeListener<? super Row>) change -> {
            PlatformHelper.runLater(() -> {
                try (var __ = svDelegate.readLock("FxSegmentView - rows changed")) {
                    flow.setCellCount(rows.size());
                    flow.refresh();
                }
            });
        });

        FxSegmentViewSkin skin = new FxSegmentViewSkin(this);
        setSkin(skin);

        setFocusTraversable(false);
    }

    /**
     * Update layout, i.e., when the scale or row and/or column sizes change.
     */
    public void updateLayout() {
        PlatformHelper.checkApplicationThread();
        segmentDelegateLeft.updateLayout();
        segmentDelegateRight.updateLayout();
    }

    @Override
    public void updateViewSize(float w, float h) {
        if (segmentDelegateLeft.isAboveSplit()) {
            setMinHeight(h);
            setPrefHeight(h);
        }
    }

    SegmentViewDelegate getSegmentDelegateLeft() {
        return segmentDelegateLeft;
    }

    SegmentViewDelegate getSegmentDelegateRight() {
        return segmentDelegateRight;
    }

    public FxSheetView getSheetView() {
        return sheetView;
    }

}
