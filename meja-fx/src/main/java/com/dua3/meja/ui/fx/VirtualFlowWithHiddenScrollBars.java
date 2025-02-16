package com.dua3.meja.ui.fx;

import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.ScrollEvent;

class VirtualFlowWithHiddenScrollBars<T extends IndexedCell<?>> extends VirtualFlow<T> {
    VirtualFlowWithHiddenScrollBars(boolean hideHScrollbar, boolean hideVScrollbar) {
        getChildren().remove(getVbar());
        getChildren().remove(getHbar());
        getVbar().setDisable(true);
        getHbar().setDisable(true);

        // disable scrolling and panning
        setPannable(false);
        addEventFilter(ScrollEvent.ANY, event -> event.consume());
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
        recreateCells();
    }


}
