package org.flowgrid.android.graphics;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;

import org.flowgrid.model.Instance;

import java.text.DecimalFormat;
import java.util.List;

public class Drawing {
  public static final String SHORT_THIS_PREFIX = "\u22c5";
  public static final String THIS_PREFIX = SHORT_THIS_PREFIX + "\u202f";

  public static void drawHalo(Canvas canvas, float x, float y, float r, int color) {
    Paint paint = new Paint();
    RadialGradient radialGradientShader;
    radialGradientShader = new RadialGradient(
      x, y, r, color, color & 0x0ffffff, RadialGradient.TileMode.MIRROR);
    paint.setShader(radialGradientShader);
    canvas.drawCircle(x, y, r, paint);
  }
}
