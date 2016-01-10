package org.flowgrid.android.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.TypedValue;
import android.view.View;

public class RunChart extends View {

  static final double[] DIV = {2, 2.5, 2};
  static final int[] COLORS = {
    Color.CYAN, Color.YELLOW, Color.MAGENTA,
    Color.RED, Color.GREEN, Color.BLUE, 
  };
  
  ArrayList<float[]> data;
  int startPos = 0;
  int widthPx;
  int widthDp;
  int heightPx;
  Paint[] paint = new Paint[COLORS.length];
  Paint originPaint = new Paint();
  Paint scalePaint = new Paint();
  Path path = new Path();
  double scale = 1;
  float[] buf = new float[0];
  int bufCount;
  float pixelPerDp;
  int divPos;
  long lastPush;
  int dataCount;
  int prevPos;
  int lastData;
  int lastDataPos;
  int pushCount;
  
  public RunChart(Context context) {
    super(context);
    originPaint.setColor(Color.DKGRAY);
    originPaint.setStyle(Style.STROKE);
    scalePaint.setColor(Color.LTGRAY);
    data = new ArrayList<float[]>();
    for (int i = 0; i < paint.length; i++) {
      paint[i] = new Paint();
      paint[i].setColor(COLORS[i % COLORS.length]);
      paint[i].setStyle(Style.STROKE);
    } 
    pixelPerDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
    scalePaint.setTextSize(10 * pixelPerDp);
  }

  public float[] getDatBuffer(int i) {
    bufSize(i + 1);
    while (buf.length > data.size()) {
      data.add(new float[widthDp]);
    }
    return data.get(i);
  }

  
  void bufSize(int i) {
    if (buf.length < i) {
      float[] newBuf = new float[i];
      System.arraycopy(buf, 0, newBuf, 0, buf.length);
      buf = newBuf;
    }
  }

  public void addData(double... value) {
    bufSize(value.length);
    for (int i = 0; i < value.length; i++) {
      buf[i] += (float) value[i];
    }
    dataCount++;
  }
  
  public void timerTick(int frames) {
    for (int i = 0; i < frames; i++) {
      push();
    }
  }

  boolean skip;
  
  synchronized void push() {
    skip = !skip;
    
    if (skip) {
      return;
    }
    while (buf.length > data.size()) {
      data.add(new float[widthDp]);
    }
    if (dataCount == 0) {
      boolean fill = (pushCount - lastData > 10);
      for (int i = 0; i < buf.length; i++) {
        data.get(i)[startPos] = fill ? data.get(i)[lastDataPos] : Float.NaN;
      }
      if (fill) {
        lastDataPos = startPos;
      }
    } else {
      for (int i = 0; i < buf.length; i++) {
        data.get(i)[startPos] = buf[i] / dataCount;
        buf[i] = 0;
      }
      lastData = pushCount;
      lastDataPos = startPos;
    }
    dataCount = 0;
    prevPos = startPos;
    startPos++;
    if (startPos >= widthDp) {
      startPos = 0;
    }
    pushCount++;
    postInvalidate();
  }

  @Override
  public synchronized void onDraw(Canvas canvas) {
    float h2 = heightPx / 2.0f;
    canvas.drawLine(0, h2, getWidth(), h2, originPaint);
    float min = 0;
    float max = 0;
    
    String text = "" + (1/scale)/2 + " _";
    canvas.drawText(text, widthPx - scalePaint.measureText(text), h2/2, scalePaint);
    
    for (int i = 0; i < data.size(); i++) {
      int p = startPos;
      path.reset();
      float[] di = data.get(i);
      boolean started = false;
      for (int x = 0; x < di.length; x++) {
        float f = di[p];
        if (!Float.isNaN(f)) {  
          if (f < min) {
            min = f;
          } 
          if (f > max) {
            max = f;
          }
          float sy = (float) (h2 - di[p] * scale * h2);
          if (!started) {
            path.moveTo(x * pixelPerDp, sy);
            started = true;
          } else {
            path.lineTo(x * pixelPerDp, sy);
          }
        }
        p++;
        if (p >= di.length) {
          p = 0;
        }
      }
      canvas.drawPath(path, paint[i % paint.length]);
    }
    
    float absMax = Math.max(-min, max);
    if (absMax * scale > 1) {
      scale /= DIV[divPos++];
      if (divPos >= DIV.length) {
        divPos = 0;
      }
    } else if (scale < 1 && absMax * scale < 0.4) {
      divPos--;
      if (divPos < 0) {
        divPos = DIV.length - 1;
      }
      scale *= DIV[divPos];
    }
    invalidate();
  }
  
  
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    //super.onMeasure(widthMeasureSpec, heightMeasureSpec); 
      
    int min = (int) (48 * pixelPerDp);
      
    widthPx = resolveSize(min, widthMeasureSpec);
    heightPx = resolveSize(min, heightMeasureSpec);
    
    setMeasuredDimension(widthPx, heightPx); 
    
    widthDp = (int) (widthPx / pixelPerDp);
    for (int i = 0; i < data.size(); i++) {
      if (data.get(i).length != widthDp) {
        data.set(i, new float[widthDp]);
      }
    }

  }
}
