package org.flowgrid.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.flowgrid.swt.Colors;


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


  public static void drawBoolean(GC gc, Colors colors, boolean value, int x, int y, int size) {
    gc.setBackground(value ? colors.greens[Colors.Brightness.REGULAR.ordinal()]
            : colors.reds[Colors.Brightness.REGULAR.ordinal()]);
    gc.fillOval(x, x, size, size);
    gc.setForeground(colors.white);
    gc.setLineWidth(Math.max(1, size / 8));
    gc.setLineCap(value ? SWT.CAP_ROUND : SWT.CAP_FLAT);
    int d = size / 4;
    int cx = x + size / 2;
    int cy = y + size / 2;
    if (value) {
      gc.drawLine(cx - d, cy, cx-d/4, cy + d);
      gc.drawLine(cx-d/4, cy + d, cx + d, cy - d);
    } else {
      gc.drawLine(cx - d, cy - d, cx + d, cy + d);
      gc.drawLine(cx - d, cy + d, cx + d, cy - d);
    }
  }


  public static void drawText(GC gc, String text, int x, int y) {
	  drawText(gc, text, x, y, SWT.LEFT, SWT.TOP);
  }


  public static void drawText(GC gc, String text, int x, int y, int horizontalAlign, int verticalAlign) {
  //  Font font = gc.getFont();
    FontMetrics metrics = gc.getFontMetrics();

    // FIXME: Use fontmetrics
  //  int size = font.getFontData()[0].getHeight(); // ascent + descent;
    int size = metrics.getHeight();
    switch(horizontalAlign) {
    //case SWT.LEFT: break;
      case SWT.RIGHT: x -= gc.stringExtent(text).x; break;
      case SWT.CENTER: x -= gc.stringExtent(text).x / 2; break;
    }
    switch(verticalAlign) {
      case SWT.CENTER: y -= size / 2f; break;
      //case SWT.BASELINE: y -= ascent; break;
      case SWT.BOTTOM: y -= size; break;
      case SWT.TOP: y += 0; break;
    }
  //  int alpha = paint.getColor() & 0xff000000;
    int start = 0;
    int pos = 0;
    int end = text.length();
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
