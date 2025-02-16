package com.dua3.meja.ui.fx;

import javafx.event.Event;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.skin.VirtualFlow;

class VirtualFlowWithHiddenScrollBars<T extends IndexedCell<?>> extends VirtualFlow<T> {
    VirtualFlowWithHiddenScrollBars(boolean hideHScrollbar, boolean hideVScrollbar) {
        getChildren().remove(getVbar());
        getChildren().remove(getHbar());
        getVbar().setDisable(true);
        getHbar().setDisable(true);

        setPannable(false);
        setOnScroll(Event::consume);
    }
}
