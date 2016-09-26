package org.flowgrid.swt.api;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.flowgrid.swt.Colors;
import org.flowgrid.swt.graphics.EmojiTextHelper;

public class Graphics {

    private int canvasViewEpoch = -1;
    private CanvasControl canvasControl;
    private GC gc;
    private Color foreground;
    private Color background;

    private boolean stroke = true;
    private boolean fill = true;
 //   private TextHelper.VerticalAlign verticalAlign = TextHelper.VerticalAlign.CENTER;
    private Image bitmap;
    private double textSize = 100;
    private Rectangle rectF = new Rectangle(0, 0, 0, 0);
    private boolean eraseTextBackground = true;
    private double strokeWidth = 10;
    private int strokeWidthPx;
    private int horizontalAlign = SWT.LEFT;
    private int verticalAlign = SWT.TOP;

    public Graphics(CanvasControl canvasControl) {
        this.canvasControl = canvasControl;
        Colors colors = canvasControl.flowgrid.colors;
        background = colors.black;
        foreground = colors.blues[Colors.Brightness.REGULAR.ordinal()];
    }

    private GC gc() {
        if (canvasControl.getBitmap() != bitmap) {
            bitmap = canvasControl.getBitmap();
            gc = new GC(bitmap);
            gc.setBackground(background);
            gc.setForeground(foreground);
            setStrokeWidth(strokeWidth);
        }
        canvasControl.postInvalidate();
        return gc;
    }

    public void clearRect(double x0, double y0, double x1, double y1) {
        synchronized (canvasControl) {
            setRect(x0, y0, x1, y1);
            gc().fillRectangle(rectF);
        }
    }

    private void setRect(double x0, double y0, double x1, double y1) {
        int x0i = Math.round(canvasControl.pixelX(x0));
        int x1i = Math.round(canvasControl.pixelX(x1));
        int y0i = Math.round(canvasControl.pixelY(y0));
        int y1i = Math.round(canvasControl.pixelY(y1));

        if (x0i > x1i) {
            int tmp = x0i;
            x0i = x1i;
            x1i = tmp;
        }
        if (y0i > y1i) {
            int tmp = y0i;
            y0i = y1i;
            y1i = tmp;
        }
        rectF.x = x0i;
        rectF.y = y0i;
        rectF.width = x1i - x0i;
        rectF.height = y1i - y0i;
    }

    public void drawImage(double x, double y, org.flowgrid.model.Image image) {
        synchronized (canvasControl) {
            ImageImpl imageImpl = (ImageImpl) image;
            int w = imageImpl.width();
            int h = imageImpl.height();
            switch (horizontalAlign) {
                case SWT.CENTER:
                    x -= w / 2;
                    break;
                case SWT.RIGHT:
                    x -= w;
                    break;
            }
            switch (verticalAlign) {
                case SWT.CENTER:
                    y -= h / 2;
                    break;
                case SWT.TOP:
                    y -= h;
                    break;
            }

            setRect(x, y, x + w, y + h);
            gc().drawImage(imageImpl.bitmap(), 0, 0, w, h, rectF.x, rectF.y, rectF.width, rectF.height);
        }
    }

    public void drawRect(double x0, double y0, double x1, double y1) {
        synchronized (canvasControl) {
            setRect(x0, y0, x1, y1);
            if (fill) {
                gc().fillRectangle(rectF);
            }
            if (stroke) {
                gc().drawRectangle(rectF);
            }
        }
    }

    public void drawLine(double x0, double y0, double x1, double y1) {
        synchronized (canvasControl) {
            gc().drawLine(
                    Math.round(canvasControl.pixelX(x0)),
                    Math.round(canvasControl.pixelY(y0)),
                    Math.round(canvasControl.pixelX(x1)),
                    Math.round(canvasControl.pixelY(y1)));
        }
    }

    public void drawText(double x, double y, String text) {
        synchronized (canvasControl) {
            int pxSize = Math.round(canvasControl.pixelSize(textSize));

            gc().setFont(canvasControl.flowgrid.colors.getFont(pxSize, 0));

            int pX = Math.round(canvasControl.pixelX(x));
            int pY = Math.round(canvasControl.pixelY(y));

            EmojiTextHelper.drawText(gc(), text, pX, pY, horizontalAlign, verticalAlign, eraseTextBackground);
        }
    }

    public void setFillColor(org.flowgrid.model.api.Color color) {
        int argb = color.argb();
        fill = (argb & 0x0ff000000) != 0;
        background = canvasControl.flowgrid.colors.getColor(argb);
        if (gc != null) {
            gc.setBackground(background);
        }
    }

    public void setStrokeColor(org.flowgrid.model.api.Color color) {
        int argb = color.argb();
        stroke = strokeWidthPx > 0 && (argb & 0x0ff000000) != 0;
        foreground = canvasControl.flowgrid.colors.getColor(argb);
        if (gc != null) {
            gc.setForeground(background);
        }
    }

    public void setStrokeWidth(double sw) {
        strokeWidth = sw;
        strokeWidthPx = Math.round(canvasControl.pixelSize(sw));
        gc().setLineWidth(strokeWidthPx);
        stroke = strokeWidthPx > 0 && foreground.getAlpha() != 0;
    }

    public void setEraseTextBackground(boolean eraseTextBackground) {
        this.eraseTextBackground = eraseTextBackground;
    }

    public void setAlignRight() {
        horizontalAlign = SWT.RIGHT;
    }

    public void setAlignLeft() {
        horizontalAlign = SWT.LEFT;
    }

    public void setAlignHCenter() {
        horizontalAlign = SWT.CENTER;
    }

    public void setAlignTop() {
        verticalAlign = SWT.TOP;
    }

    public void setAlignBottom() {
        verticalAlign = SWT.BOTTOM;
    }

    public void setAlignVCenter() {
        // verticalAlign = TextHelper.VerticalAlign.CENTER;
    }

    public void setTextSize(double size) {
        textSize = size;
        //strokePaint.setTextSize(pxSize);
    }

    public String toString() {
        return "Graphics#" + (hashCode() % 1000);
    }
}
