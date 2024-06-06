package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Row;
import com.dua3.meja.ui.CellRenderer;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Skin;

public class FxRowSkin implements Skin<FxRow> {
    private final FxRow skinnable;

    private Canvas canvas = new Canvas();

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
        render(fxRow);
        return canvas;
    }

    private void render(FxRow fxRow) {
        FxSheetViewDelegate delegate = fxRow.getDelegate();

        float w = (float) fxRow.getRowWidth();
        float h = (float) fxRow.getRowHeight();

        float s = delegate.getScale();
        canvas.setWidth(w * s);
        canvas.setHeight(h * s);

        CellRenderer cr = new CellRenderer(delegate);

        FxGraphics g = new FxGraphics(canvas.getGraphicsContext2D(), (float) canvas.getWidth(), (float) canvas.getHeight());

        // clear background
        g.setColor(delegate.getBackground());
        g.fillRect(g.getBounds());

        Row row = fxRow.getItem();
        if (row == null) {
            return;
        }

        // draw grid lines
        g.setColor(delegate.getGridColor());
        g.strokeLine(0, h, w, h);
        g.strokeLine(0, 0, w, 0);
        for (int j=0; j<delegate.getColumnCount(); j++) {
            float x = delegate.getColumnPos(j);
            g.strokeLine(x, 0, x, h);
        }
        g.strokeLine(w, 0, w, h);

        //  draw cells
        g.translate(0, -delegate.getRowPos(row.getRowNumber()));
        for (int j=0; j<delegate.getColumnCount(); j++) {
            row.getCellIfExists(j).ifPresent(cell -> {
                cr.drawCellBackground(g, cell);
                cr.drawCellBorder(g, cell);
                cr.drawCellForeground(g, cell);
            });
        }
    }

    @Override
    public void dispose() {
    }
}
