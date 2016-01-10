package org.flowgrid.android.port;

import java.util.HashMap;

import org.flowgrid.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;

public class Sprite {
  static String[] NAMES = {"None", "Fluffy", "Bluffy", "Robby"};
  
  enum Mood {NEUTRAL, HAPPY, SAD, SUPER_HAPPY};
  
  private static RectF rect = new RectF();
  private static RectF rect2 = new RectF();
  private static HashMap<Integer,Bitmap> bitmapCache = new HashMap<Integer,Bitmap>();
  
  static Bitmap getBitmap(Context context, int id) {
    Bitmap bitmap = bitmapCache.get(id);
    if (bitmap == null) {
      bitmap = BitmapFactory.decodeResource(context.getResources(), id);
      bitmapCache.put(id, bitmap);
    }
    return bitmap;
  }
  
  
  public static void draw(Context context, Canvas canvas, String icon, Mood mood, float x0, float y0, float cellSize) {
    rect.set(x0, y0, x0 + cellSize * 3/2, y0 + cellSize);
    int id = -1;
    if ("Fluffy".equals(icon)) {
      id = R.drawable.fluffy;
    } else if ("Bluffy".equals(icon)) {
      id = R.drawable.bluffy;
    } else if ("Robby".equals(icon)) {
      id = R.drawable.robby;
    }
    if (id != -1) {
      canvas.drawBitmap(getBitmap(context, id), null, rect, null);
      int mouthId;
      int eyesId;
      switch(mood) {
      case SAD:
        eyesId = R.drawable.eyes_normal;
        mouthId = R.drawable.mouth_unhappy;
        break;
      case HAPPY:
        eyesId = R.drawable.eyes_normal;
        mouthId = R.drawable.mouth_happy;
        break;
      case SUPER_HAPPY:
        eyesId = R.drawable.eyes_happy;
        mouthId = R.drawable.mouth_happy;
        break;
      default:
        eyesId = R.drawable.eyes_normal;
        mouthId = R.drawable.mouth_neutral;
      }

      rect2.set(rect);
      rect2.left += cellSize / 6;
      rect2.right -= cellSize / 6;
      rect2.top += cellSize / 2;
      rect2.bottom -= cellSize / 8;
      canvas.drawBitmap(getBitmap(context, mouthId), null, rect2, null);

      rect2.set(rect);
      rect2.left += cellSize / 2.5;
      rect2.right -= cellSize / 2.5;
      rect2.top += cellSize / 7;
      rect2.bottom -= cellSize / 2;
      canvas.drawBitmap(getBitmap(context, eyesId), null, rect2, null);
    }
  }
}
