/*
 *
 */
package com.dua3.meja.ui.javafx;

import com.dua3.meja.model.Color;
import com.dua3.meja.ui.GraphicsContext;
import com.dua3.meja.ui.Rectangle;
import javafx.geometry.Bounds;

/**
 *
 * @author Axel Howind
 */
class JfxGraphicsContext implements GraphicsContext {

    private final javafx.scene.canvas.GraphicsContext gc;

    public JfxGraphicsContext(javafx.scene.canvas.GraphicsContext gc) {
          this.gc = gc;
    }

    @Override
    public void setColor(Color color) {
        gc.setFill(MejaJfxHelper.toJfxColor(color));
    }

    @Override
    public void drawLine(double x1, double y1, double x2, double y2) {
        gc.beginPath();
        gc.moveTo(x1, y1);
        gc.lineTo(x2, y2);
        gc.stroke();
    }

    @Override
    public void drawRect(double x, double y, double w, double h) {
        gc.beginPath();
        gc.rect(x, y, w, h);
        gc.stroke();
    }

    @Override
    public void fillRect(double x, double y, double w, double h) {
        gc.fillRect(x, y, w, h);
    }

    @Override
    public void setStroke(Color color, double width) {
        setColor(color);
        gc.setLineWidth(width);
    }

    @Override
    public Rectangle getClipBounds() {
        Bounds b = gc.getCanvas().getBoundsInLocal();
        return new Rectangle(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
    }

    public javafx.scene.canvas.GraphicsContext graphics() {
        return gc;
    }

}
