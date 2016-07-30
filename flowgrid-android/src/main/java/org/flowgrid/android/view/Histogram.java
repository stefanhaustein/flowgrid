package org.flowgrid.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.View;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class Histogram extends View {
  Map<Object, Counter> data = new TreeMap<>();
  // float pixelPerDp;
  Paint barPaint = new Paint();
  int max = 10;

  public Histogram(Context context) {
    super(context);
    // pixelPerDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
    barPaint.setStyle(Paint.Style.FILL);
    barPaint.setColor(Color.LTGRAY);
  }

  public synchronized void add(Object value) {
    if (!(value instanceof Comparable) && data instanceof TreeMap) {
      LinkedHashMap<Object,Counter> hashMap = new LinkedHashMap<>();
      hashMap.putAll(data);
      data = hashMap;
    }
    Counter counter = data.get(value);
    if (counter == null) {
      counter = new Counter();
      data.put(value, counter);
    }
    counter.count++;
    if (counter.count > max) {
      max = counter.count;
    }
  }

  public synchronized void onDraw(Canvas canvas) {
    float height = (float) getHeight();
    float width = (float) getWidth();
    float itemWidth = width / data.size();
    float scale = height / max;
    float x = 0;

    for (Map.Entry<Object, Counter> entry: data.entrySet()) {
      canvas.drawRect(x + itemWidth / 6, height - entry.getValue().count * scale,
          x + itemWidth * 4 / 6, height, barPaint);
    }
  }

  private static class Counter {
    private int count;
  }
}
