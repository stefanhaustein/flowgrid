package org.flowgrid.android.api;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import org.flowgrid.android.graphics.Colors;
import org.flowgrid.model.Image;
import org.flowgrid.model.api.Color;
import org.kobjects.emoji.android.TextHelper;

public class Graphics {
  private int canvasViewEpoch = -1;
  private CanvasView canvasView;
  private Canvas canvas;
  private Paint fillPaint = new android.graphics.Paint();
  private Paint strokePaint = new android.graphics.Paint();
  private Paint clearPaint = new android.graphics.Paint();
  private boolean stroke = true;
  private boolean fill = true;
  private TextHelper.VerticalAlign verticalAlign = TextHelper.VerticalAlign.CENTER;
  private Bitmap bitmap;
  private double textSize = 100;
  private RectF rectF = new RectF();
  private boolean eraseTextBackground = true;
  private double strokeWidth = 10;

  public Graphics(CanvasView canvasView) {
    this.canvasView = canvasView;

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
  }

  private Canvas canvas() {
    if (canvasView.getBitmap() != bitmap) {
      bitmap = canvasView.getBitmap();
      canvas = new Canvas(bitmap);
      setStrokeWidth(strokeWidth);
    }
    canvasView.postInvalidate();
    return canvas;
  }

  public void clearRect(double x0, double y0, double x1, double y1) {
    synchronized (canvasView) {
      setRect(x0, y0, x1, y1);
      canvas().drawRect(rectF, clearPaint);
    }
  }

  private void setRect(double x0, double y0, double x1, double y1) {
    rectF.set(canvasView.pixelX(x0), canvasView.pixelY(y0), canvasView.pixelX(x1), canvasView.pixelY(y1));
    rectF.sort();
  }

  public void drawImage(double x, double y, Image image) {
    synchronized (canvasView) {
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
      canvas().drawBitmap(imageImpl.bitmap(), null, rectF, null);
    }
  }

  public void drawRect(double x0, double y0, double x1, double y1) {
    synchronized (canvasView) {
      setRect(x0, y0, x1, y1);
      if (fill) {
        canvas().drawRect(rectF, fillPaint);
      }
      if (stroke) {
        canvas().drawRect(rectF, strokePaint);
      }
    }
  }

  public void drawLine(double x0, double y0, double x1, double y1) {
    synchronized (canvasView) {
      canvas().drawLine(
          canvasView.pixelX(x0), canvasView.pixelY(y0),
          canvasView.pixelX(x1), canvasView.pixelY(y1), strokePaint);
    }
  }

  public void drawText(double x, double y, String text) {
    synchronized (canvasView) {
      float pxSize = canvasView.pixelSize(textSize);
      fillPaint.setTextSize(pxSize);

      float pX = canvasView.pixelX(x);
      float pY = canvasView.pixelY(y);

      if (eraseTextBackground) {
        TextHelper.getTextBounds(fillPaint, text, verticalAlign, rectF);
        rectF.offset(pX, pY);
        canvas().drawRect(rectF, clearPaint);
      }
      TextHelper.drawText(canvasView.getContext(), canvas(),
          text, pX, pY, fillPaint, verticalAlign);
    }
  }

  public void setFillColor(Color color) {
    fillPaint.setColor(color.argb());
    fill = fillPaint.getAlpha() != 0;
  }

  public void setStrokeColor(Color color) {
    strokePaint.setColor(color.argb());
    stroke = strokePaint.getStrokeWidth() > 0 && strokePaint.getAlpha() != 0;
  }

  public void setStrokeWidth(double sw) {
    strokeWidth = sw;
    strokePaint.setStrokeWidth(canvasView.pixelSize(sw));
    stroke = sw > 0 && strokePaint.getAlpha() != 0;
  }

  public void setEraseTextBackground(boolean eraseTextBackground) {
    this.eraseTextBackground = eraseTextBackground;
  }

  public void setAlignRight() {
    fillPaint.setTextAlign(Paint.Align.RIGHT);
  }

  public void setAlignLeft() {
    fillPaint.setTextAlign(Paint.Align.LEFT);
  }

  public void setAlignHCenter() {
    fillPaint.setTextAlign(Paint.Align.CENTER);
  }

  public void setAlignTop() {
    verticalAlign = TextHelper.VerticalAlign.TOP;
  }

  public void setAlignBottom() {
    verticalAlign = TextHelper.VerticalAlign.BOTTOM;
  }

  public void setAlignBaseline() {
    verticalAlign = TextHelper.VerticalAlign.BASELINE;
  }

  public void setAlignVCenter() {
    verticalAlign = TextHelper.VerticalAlign.CENTER;
  }

  public void setTextSize(double size) {
    textSize = size;
    //strokePaint.setTextSize(pxSize);
  }

  public String toString() {
    return "Graphics#" + (hashCode() % 1000);
  }
}
