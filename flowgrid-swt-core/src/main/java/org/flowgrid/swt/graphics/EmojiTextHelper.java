package org.flowgrid.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;


/**
 * Provides a set of static drawText methods with support for vertical alignment
 * and emoji fallback images.
 */
public class EmojiTextHelper {

  public static void drawEmoji(GC gc, int codepoint, int x, int y, int height) {
    Image image = ImageCache.getImage(gc.getDevice(), "/emoji/64/" + Integer.toHexString(codepoint) + ".png");
    Rectangle bounds = image.getBounds();
    gc.drawImage(image, 0, 0, bounds.width, bounds.height, x, y, height, height);
  }


  /*
  public static void drawBoolean(GC gc, boolean value, float cx, float cy,
      float radius) {
    shapePaint.setColor(value ? GREEN : RED);
    if (paint != null) {
      shapePaint.setAlpha(paint.getAlpha());
    }
    canvas.drawCircle(cx, cy, radius, shapePaint);
    shapePaint.setColor(shapePaint.getColor() | 0x0ffffff);
    shapePaint.setTypeface(Typeface.DEFAULT_BOLD);
    shapePaint.setTextAlign(Align.CENTER);
    shapePaint.setTextSize(radius * 3 / 2);
    
    shapePaint.setStyle(Style.STROKE);
    shapePaint.setStrokeWidth(radius / 4);
    shapePaint.setStrokeCap(value ? Cap.ROUND : Cap.BUTT);
    float d = radius / 2;
    if (value) {
      canvas.drawLine(cx - d, cy, cx-d/4, cy + d, shapePaint);
      canvas.drawLine(cx-d/4, cy + d, cx + d, cy - d, shapePaint);
    } else {
      canvas.drawLine(cx - d, cy - d, cx + d, cy + d, shapePaint);
      canvas.drawLine(cx - d, cy + d, cx + d, cy - d, shapePaint);
    }
    //drawText(null, canvas, value ? "\u2713" : "\u2715", cx, cy, shapePaint, VerticalAlign.CENTER);

    shapePaint.setStyle(Style.FILL);
  }
*/

  public static void drawText(GC gc, String text, int x, int y) {
	  drawText(gc, text, 0, text.length(), x, y, SWT.LEFT | SWT.TOP);
  }

  /*
  public static void drawText(Context context, Canvas canvas, CharSequence text, 
		  float x, float y, Paint paint, VerticalAlign verticalAlign) {
	  drawText(context, canvas, text, 0, text.length(), x, y, paint, verticalAlign);
  }

  public static void drawText(Context context, Canvas canvas, CharSequence text, int start, int end, 
		  float x, float y, Paint paint) {
	  drawText(context, canvas, text, start, end, x, y, paint, VerticalAlign.BASELINE);
  }

  public static void getTextBounds(Paint paint, String text, VerticalAlign verticalAlign, RectF rectF) {
    getTextBounds(paint, text, 0, text.length(), verticalAlign, rectF);
  }

  public static void getTextBounds(Paint paint, String text, int start, int end, VerticalAlign verticalAlign, RectF rectF) {
    float ascent = Math.abs(paint.ascent());
    float descent = Math.abs(paint.descent());
    float size = ascent + descent;
    Align horizontalAlign = paint.getTextAlign();
    float width = measureText(paint, text, start, end);
    switch(horizontalAlign) {
      case LEFT: rectF.left = 0; rectF.right = width; break;
      case RIGHT: rectF.left = -width; rectF.right = 0; break;
      case CENTER: rectF.left = -width / 2; rectF.right = width / 2; break;
    }
    switch(verticalAlign) {
      case CENTER: rectF.top = -size / 2; rectF.bottom = size / 2; break;
      case BASELINE: rectF.top = -ascent; rectF.bottom = descent; break;
      case BOTTOM: rectF.top = -size; rectF.bottom = 0; break;
      case TOP: rectF.top = 0; rectF.bottom = size; break;
    }
  } */

  public static void drawText(GC gc, String text, int start, int end,
		  int x, int y, int align) {
    Font font = gc.getFont();
   // float ascent = Math.abs(gc.ascent());
   // float descent = Math.abs(paint.descent());

    // FIXME: Use fontmetrics
    int size = font.getFontData()[0].getHeight(); // ascent + descent;
 /*   Align horizontalAlign = paint.getTextAlign();
    switch(horizontalAlign) {
    case LEFT: break;
    case RIGHT: x -= measureText(paint, text, start, end); break;
    case CENTER: x -= measureText(paint, text, start, end) / 2; break;
    }
    switch(verticalAlign) {
    case CENTER: y -= size / 2f; break;
    case BASELINE: y -= ascent; break;
    case BOTTOM: y -= size; break;
    case TOP: y += 0; break;
    }
    paint.setTextAlign(Align.LEFT);*/
  //  int alpha = paint.getColor() & 0xff000000;
    int pos = start;
    while (pos < end) {
      int codepoint = Character.codePointAt(text, pos);
      System.out.println(Integer.toHexString(codepoint));
      if (//codepoint == 0xf888 || codepoint == 0xf889 ||
         codepoint > 0x1f300) {
      //    (fallback == Fallback.FULL && codepoint >= FALLBACK_START && codepoint < FALLBACK_END) ||
      //    fallback == Fallback.COLOR_HEARTS_ONLY && codepoint >= 0x1F499 && codepoint <= 0x1f49c) {
        String part = text.substring(start, pos);
        gc.drawString(part, x, y, true);
        x += gc.stringExtent(part).x;
      /*  switch (codepoint) {
        case 0xf888:
          drawBoolean(canvas, false, x + size / 2, y + size / 2, size / 2, paint);
          break;
        case 0xf889:
          drawBoolean(canvas, true, x + size / 2, y + size / 2, size / 2, paint);
          break;
        default:
          Bitmap bitmap = fallbackBitmap(context, codepoint);
          rect.left = (int) x;
          rect.top = (int) y;
          rect.right = (int) (x + size);
          rect.bottom = (int) (y + size);
          canvas.drawBitmap(bitmap, null, rect, paint);
        }*/
        drawEmoji(gc, codepoint, x, y, size);
        x += size;
        pos += codepoint > 0x10000 ? 2 : 1;
        start = pos; 
      } else {
        pos += codepoint > 0x10000 ? 2 : 1;
      }
    }

    gc.drawString(text.substring(start, end), x, y, true);
//    canvas.drawText(text, start, end, x, y + ascent, paint);
 //   paint.setTextAlign(horizontalAlign);
  }

  public static boolean isEmoji(String text) {
    return false;
  }

  /*
  public static float measureText(Paint paint, CharSequence text) {
    return measureText(paint, text, 0, text.length());
  }
  
  public static float measureText(Paint paint, CharSequence text, int start, int end) {
    int pos = start;
    float ascent = Math.abs(paint.ascent());
    float descent = Math.abs(paint.descent());
    float size = ascent + descent;
    float width = 0;
    while (pos < end) {
      int codepoint = Character.codePointAt(text, pos);
      if (codepoint == 0xf888 || codepoint == 0xf889 ||
          (fallback == Fallback.FULL && codepoint >= FALLBACK_START && codepoint < FALLBACK_END)) {
        width += paint.measureText(text, start, pos) + size;
        pos += codepoint >= 0x10000 ? 2 : 1;
        start = pos;
      } else {
        pos += codepoint > 0x10000 ? 2 : 1;
      }
    }
    return width + paint.measureText(text, start, end);
  }
  */
  
}
