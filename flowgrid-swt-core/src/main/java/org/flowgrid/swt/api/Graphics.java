package org.flowgrid.swt.api;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.flowgrid.model.api.Color;

public class Graphics {

    private int canvasViewEpoch = -1;
    private CanvasControl canvasControl;
    private GC gc;
//    private Canvas canvas;
//    private Paint fillPaint = new android.graphics.Paint();
//    private Paint strokePaint = new android.graphics.Paint();
 //   private Paint clearPaint = new android.graphics.Paint();
    private boolean stroke = true;
    private boolean fill = true;
 //   private TextHelper.VerticalAlign verticalAlign = TextHelper.VerticalAlign.CENTER;
    private Image bitmap;
    private double textSize = 100;
    private Rectangle rectF = new Rectangle(0, 0, 0, 0);
    private boolean eraseTextBackground = true;
    private double strokeWidth = 10;

    public Graphics(CanvasControl canvasControl) {
        this.canvasControl = canvasControl;

        /*
        fillPaint.setStyle(android.graphics.Paint.Style.FILL);
        fillPaint.setColor(Colors.GRAY[Colors.Brightness.REGULAR.ordinal()]);
        fillPaint.setTextAlign(Paint.Align.CENTER);
        fillPaint.setAntiAlias(true);
        strokePaint.setStyle(android.graphics.Paint.Style.STROKE);
        strokePaint.setColor(Colors.BLUE[Colors.Brightness.REGULAR.ordinal()]);
        strokePaint.setAntiAlias(true);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        */
    }

    private GC gc() {
        if (canvasControl.getBitmap() != bitmap) {
            bitmap = canvasControl.getBitmap();
            gc = new GC(bitmap);
            gc.setBackground(canvasControl.getDisplay().getSystemColor(SWT.COLOR_BLACK));
            gc.setForeground(canvasControl.getDisplay().getSystemColor(SWT.COLOR_WHITE));
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

    public void drawImage(double x, double y, Image image) {
        synchronized (canvasControl) {
            /*
            ImageImpl imageImpl = (ImageImpl) image;
            double w = imageImpl.width();
            double h = imageImpl.height();
            switch (fillPaint.getTextAlign()) {
                case CENTER:
                    x -= w / 2;
                    break;
                case RIGHT:
                    x -= w;
                    break;
            }
            switch (verticalAlign) {
                case CENTER:
                    y -= h / 2;
                    break;
                case TOP:
                    y -= h;
                    break;
            }

            setRect(x, y, x + w, y + h);
            gc().drawBitmap(imageImpl.bitmap(), null, rectF, null);
            */
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
            float pxSize = canvasControl.pixelSize(textSize);

            System.out.println("FIXME: CanvasControl.drawText");   // FIXME

            /*
            fillPaint.setTextSize(pxSize);

            float pX = canvasControl.pixelX(x);
            float pY = canvasControl.pixelY(y);

            if (eraseTextBackground) {
                TextHelper.getTextBounds(fillPaint, text, verticalAlign, rectF);
                rectF.offset(pX, pY);
                gc().drawRect(rectF, clearPaint);
            }
            TextHelper.drawText(canvasControl.getContext(), canvas(),
                    text, pX, pY, fillPaint, verticalAlign);

                    */
        }
    }

    public void setFillColor(Color color) {
        // fillPaint.setColor(color.argb());
        fill = (color.argb() & 0x0ff000000) != 0;
    }

    public void setStrokeColor(Color color) {
        //        strokePaint.setColor(color.argb());
        stroke = Math.round(strokeWidth) > 0 && (color.argb() & 0x0ff000000) != 0;
    }

    public void setStrokeWidth(double sw) {
        strokeWidth = sw;
        int rounded = Math.round(canvasControl.pixelSize(sw));
        gc().setLineWidth(rounded);
        stroke = rounded > 0; // && strokePaint.getAlpha() != 0;
    }

    public void setEraseTextBackground(boolean eraseTextBackground) {
        this.eraseTextBackground = eraseTextBackground;
    }

    public void setAlignRight() {

        //fillPaint.setTextAlign(Paint.Align.RIGHT);
    }

    public void setAlignLeft() {

        //fillPaint.setTextAlign(Paint.Align.LEFT);
    }

    public void setAlignHCenter() {
        //fillPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setAlignTop() {
//        verticalAlign = TextHelper.VerticalAlign.TOP;
    }

    public void setAlignBottom() {
        //verticalAlign = TextHelper.VerticalAlign.BOTTOM;
    }

    public void setAlignBaseline() {
        //verticalAlign = TextHelper.VerticalAlign.BASELINE;
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
