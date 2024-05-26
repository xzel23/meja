package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Row;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;

public class FxRowSkin implements Skin<FxRow> {
    private final FxRow skinnable;

    private Label node = new Label();

    public FxRowSkin(FxRow skinnable) {
        this.skinnable = skinnable;
    }

    @Override
    public FxRow getSkinnable() {
        return skinnable;
    }

    @Override
    public Node getNode() {
        FxRow fxRow = getSkinnable();
        Row row = fxRow.getItem();
        node.setPrefWidth(fxRow.getRowWidth());
        node.setPrefHeight(fxRow.getRowHeight());
        node.setText(row == null ? "no row" : fxRow.getText());
        return node;
    }

    @Override
    public void dispose() {
        // nop
    }
}
