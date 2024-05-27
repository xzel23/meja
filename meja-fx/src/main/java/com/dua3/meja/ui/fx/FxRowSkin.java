package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Row;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.text.RichText;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;

import java.util.Locale;

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
        Row row = fxRow.getItem();
        render(fxRow);
        return canvas;
    }

    private void render(FxRow fxRow) {
        canvas.setWidth(fxRow.getRowWidth());
        canvas.setHeight(fxRow.getRowHeight());

        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        Row row = fxRow.getItem();
        if (row == null) {
            return;
        }

        FxSheetViewDelegate delegate = fxRow.getDelegate();

        int i = row.getRowNumber();
        float h = delegate.getRowHeightInPoints(i);

        for (int j=0; j<delegate.getColumnCount(); j++) {
            Cell cell = row.getCell(j);

            float x = delegate.getColumnPos(j);
            float w = delegate.getColumnWidthInPoints(j);

            CellStyle cellStyle = cell.getCellStyle();
            g.setFill(FxUtil.convert(cellStyle.getFillFgColor()));
            g.fillRect(x, 0, w, h);

            g.setStroke(Color.GRAY);
            g.strokeRect(x, 0, w, h);

            RichText text = cell.getAsText(Locale.getDefault());
            if (text != null) {
                double textX = x + 5;
                double textY = h;
                g.setFill(FxUtil.convert(cellStyle.getFont().getColor()));
                g.fillText(text.toString(), textX, textY);
            }
        }
    }

    @Override
    public void dispose() {
    }
}
