package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Row;
import com.dua3.meja.ui.CellRenderer;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Scale2f;
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

        Scale2f s = delegate.getScale();
        canvas.setWidth(w * s.sx());
        canvas.setHeight(h * s.sy());
        canvas.getGraphicsContext2D().scale(fxRow.displayScale.sx(), fxRow.displayScale.sy());

        CellRenderer cr = new CellRenderer(delegate);

        FxGraphics g = new FxGraphics(canvas.getGraphicsContext2D(), (float) canvas.getWidth(), (float) canvas.getHeight());

        // clear background
        g.setFill(delegate.getBackground());
        g.fillRect(g.getBounds());

        // draw grid lines
        g.setStroke(delegate.getGridColor(), delegate.get1PxWidth());
        g.strokeLine(0, h, w, h);
        g.setStroke(delegate.getGridColor(), delegate.get1PxHeight());
        g.strokeLine(0, 0, w, 0);
        for (int j=0; j<delegate.getColumnCount(); j++) {
            float x = delegate.getColumnPos(j);
            g.setStroke(delegate.getGridColor(), delegate.get1PxWidth());
            g.strokeLine(x, 0, x, h);
        }
        g.strokeLine(w, 0, w, h);

        Row row = fxRow.getItem();
        if (row == null) {
            return;
        }

        //  draw cells
        g.setTransformation(AffineTransformation2f.translate(0, -delegate.getRowPos(row.getRowNumber())));
        for (int j=0; j<delegate.getColumnCount(); j++) {
            row.getCellIfExists(j).ifPresent(cell -> cr.drawCell(g, cell.getLogicalCell()));
        }
    }

    @Override
    public void dispose() {
    }
}
