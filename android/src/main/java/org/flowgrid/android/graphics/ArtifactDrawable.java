package org.flowgrid.android.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import org.kobjects.emoji.android.TextHelper;

public class ArtifactDrawable extends Drawable {
  public enum Kind {
    CONTINUOUS_OPERATION, OPERATION, PROPERTY, CLASSIFIER, MODULE, SOUND, TUTORIAL,
    BRANCH_LEFT, BRANCH_RIGHT, BRANCH_ALL, BRANCH_LEFT_AND_RIGTH, IMAGE
  }

  final Paint paint = new Paint();
  final Kind kind;
  final String text;
  Path path;
  RectF rect;
  final Context context;

  public void setEnabled(boolean enabled) {
    int fgColor = Colors.GRAY[enabled ? Colors.GRAY.length - 2 : 0];
    paint.setColor(fgColor);
  }

  public ArtifactDrawable(Context context, Kind kind, String text) {
    this.context = context;
    if (kind == Kind.IMAGE) {
      this.kind = Kind.OPERATION;
      this.text = "\u273f";
    } else {
      this.kind = kind;
      this.text = text;
    }
    if (kind == Kind.MODULE || kind == Kind.OPERATION || kind == Kind.SOUND
        || kind == Kind.CONTINUOUS_OPERATION || kind == Kind.IMAGE) {
      rect = new RectF();
    }
    if (kind == Kind.PROPERTY || kind == Kind.TUTORIAL || kind == Kind.SOUND) {
      path = new Path();
    }
    if (kind == Kind.CLASSIFIER) {
      paint.setTypeface(Typeface.DEFAULT_BOLD);
    }
    paint.setTextAlign(Paint.Align.CENTER);
    paint.setAntiAlias(true);
    setEnabled(true);
  }


  @Override
  public void draw(Canvas canvas) {
    Rect bounds = getBounds();
    int cellSize = Math.min(bounds.height(), bounds.width());
    float x0 = bounds.left;
    float y0 = bounds.top;
    float x1 = bounds.right;
    float y1 = bounds.bottom;
    float xM = bounds.centerX();
    float yM = bounds.centerY();
    float border = cellSize / 8;
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(border / 2);

    switch(kind) {
      case BRANCH_ALL:
      case BRANCH_LEFT:
      case BRANCH_LEFT_AND_RIGTH:
      case BRANCH_RIGHT:
        if (kind == Kind.BRANCH_ALL || kind == Kind.BRANCH_LEFT || kind == Kind.BRANCH_LEFT_AND_RIGTH) {
          canvas.drawLine(xM, y0 + border, x0 + border, yM, paint);
        }
        if (kind == Kind.BRANCH_ALL || kind == Kind.BRANCH_LEFT || kind == Kind.BRANCH_RIGHT) {
          canvas.drawLine(xM, y0 + border, xM, y1 - border, paint);
        }
        if (kind == Kind.BRANCH_ALL || kind == Kind.BRANCH_RIGHT|| kind == Kind.BRANCH_LEFT_AND_RIGTH) {
          canvas.drawLine(xM, y0 + border, x1 - border, yM, paint);
        }
        break;

      case CLASSIFIER: {
        int color = paint.getColor();
        paint.setColor(Colors.GRAY[1]);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), cellSize / 2, paint);

        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), cellSize / 2, paint);
        break;
      }
      case MODULE:
        rect.set(bounds);
        rect.bottom = x0 + 1.5f * border;
        rect.right = bounds.centerX();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRoundRect(rect, border / 2, border / 2, paint);
        rect.top = rect.centerY();
        canvas.drawRect(rect, paint);
        rect.set(bounds);
        rect.top += 1.5*border;
        rect.bottom -= 1* border;
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(rect, border, border, paint);
        break;
      case CONTINUOUS_OPERATION:
        paint.setUnderlineText(true);
        // Fallthrough intended
      case OPERATION:
        rect.set(bounds);
        rect.top += border;
        rect.bottom -= border;
        canvas.drawRoundRect(rect, border, border, paint);
       // canvas.drawRect(rect, paint);
        break;
      case PROPERTY:
        path.rewind();
        path.moveTo(x0 + border, y0 + border);
        path.lineTo(x1, y0 + border);
        path.lineTo(x1 - border, y1 - border);
        path.lineTo(x0, y1 - border);
        path.close();
        canvas.drawPath(path, paint);
        break;
      case SOUND:
        path.rewind();
        path.moveTo(x1 - border, y0 + border);
        path.lineTo(x1 - border, y1 - border);
        path.lineTo(xM, yM + 1.5f * border);
        path.lineTo(xM, yM - 1.5f * border);
        path.close();
        canvas.drawPath(path, paint);
        rect.left = x0 + border;
        rect.right = xM - 1.5f * border;
        rect.top = yM - 1.5f * border;
        rect.bottom = yM + 1.5f * border;
        canvas.drawRect(rect, paint);
        return;

      case TUTORIAL:
        path.rewind();
        path.moveTo(x1 - 2 * border, y0 + 4 * border);
        path.lineTo(x1 - 2 * border, y1 - 2 * border);
        path.lineTo(xM, y1 - border);
        path.lineTo(x0 + 2 * border, y1 - 2 * border);
        path.lineTo(x0 + 2 * border, y0 + 4 * border);
        canvas.drawPath(path, paint);

        path.rewind();
        path.moveTo(xM, y0 + border);
        path.lineTo(x1, y0 + 3 * border);
        path.lineTo(xM, y0 + 5 * border);
        path.lineTo(x0, y0 + 3 * border);
        path.close();
        canvas.drawPath(path, paint);
        canvas.drawLine(x1, y0 + 3 * border, x1, yM + (text == null ? 2 : 1) * border, paint);
        break;
    }


    if (text != null) {
      paint.setStyle(Paint.Style.FILL);
      if (kind == Kind.TUTORIAL) {
        paint.setTextSize(cellSize * 0.33f);
        paint.setTextAlign(Paint.Align.RIGHT);
        TextHelper.drawText(context, canvas, text, bounds.right, bounds.bottom, paint, TextHelper.VerticalAlign.BOTTOM);
      } else {
        paint.setTextSize(cellSize * (kind == Kind.CLASSIFIER ? 0.66f : 0.5f));
//      canvas.drawText(text, bounds.centerX(), bounds.bottom - cellSize * (kind == Kind.CLASSIFIER ? 0.25f : 0.33f), paint);
        TextHelper.drawText(context, canvas, text, bounds.centerX(), bounds.centerY(), paint, TextHelper.VerticalAlign.CENTER);
      }
    } /*else {
      paint.setStyle(Paint.Style.FILL);
      rect.set(bounds);
      rect.inset(border, border);
      rect.top += border;
      paint.setColor(Colors.GRAY[0]);
      canvas.drawRect(rect, paint);
      paint.setColor(Colors.GRAY[3]);
      paint.setStrokeWidth(cellSize / 16);
      paint.setStyle(Paint.Style.STROKE);
      canvas.drawRect(rect, paint);

      path.rewind();
      path.moveTo(x0 + border, y0 + 2 * border);
      path.lineTo(x0 + 2 * border, y0 + border);
      path.lineTo(x1 - 2 * border, y0 + border);
      path.lineTo(x1 - border, y0 + 2 * border);

//      path.lineTo(x1 - border, y1 - 2 * border);
  //    path.lineTo(xM, y1);
    //  path.lineTo(x0 + border, y1 - 2 * border);
//      path.close();
      //paint.setColor(Color.BLACK);
      canvas.drawPath(path, paint);
//      canvas.drawPath(path, paint);
//      canvas.drawLine(x0 + border, y0 + 2 * border, xM, y0 + 4 * border, paint);
  //    canvas.drawLine(x1 - border, y0 + 2 * border, xM, y0 + 4 * border, paint);
    //  canvas.drawLine(xM, y1, xM, y0 + 4* border, paint);
    }
*/
  }

  @Override
  public void setAlpha(int alpha) {

  }

  @Override
  public void setColorFilter(ColorFilter cf) {

  }

  @Override
  public int getOpacity() {
    return 0;
  }
}
