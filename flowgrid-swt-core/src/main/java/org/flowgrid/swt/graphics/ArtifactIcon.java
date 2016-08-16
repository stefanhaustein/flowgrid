package org.flowgrid.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.flowgrid.swt.Colors;

public class ArtifactIcon extends Canvas {
    public enum Kind {
        CONTINUOUS_OPERATION, OPERATION, PROPERTY, CLASSIFIER, MODULE, SOUND, TUTORIAL,
        BRANCH_LEFT, BRANCH_RIGHT, BRANCH_ALL, BRANCH_LEFT_AND_RIGTH, PARENT, IMAGE
    }

    final Kind kind;
    final String text;
    final Colors colors;
    Color color;

    public void setEnabled(boolean enabled) {
        color = enabled ? colors.foreground : colors.grays[3];
    }

    public ArtifactIcon(Composite parent, Colors colors, Kind kind, String text) {
        super(parent, SWT.NONE);
        this.colors = colors;
        if (kind == Kind.IMAGE) {
           this.kind = Kind.OPERATION;
           this.text = "\u273f";
        } else {
           this.kind = kind;
           this.text = text;
        }
        /*    if (kind == Kind.MODULE || kind == Kind.OPERATION || kind == Kind.SOUND
                    || kind == Kind.CONTINUOUS_OPERATION || kind == Kind.IMAGE) {
                rect = new RectF();
            }
            if (kind == Kind.PROPERTY || kind == Kind.TUTORIAL || kind == Kind.SOUND) {
                path = new Path();
            }
            if (kind == Kind.CLASSIFIER) {
                paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setAntiAlias(true);  */
        setEnabled(true);
    }


    @Override
    public void drawBackground(GC gc, int clipX, int clipY, int clipW, int clipH) {
        Rectangle bounds = getBounds();
        int cellSize = Math.min(bounds.height, bounds.width);
        int border = Math.max(1, Math.round(cellSize / 8));
        cellSize -= 2 * border;
        int x0 = bounds.x + border;
        int y0 = bounds.y + border;
        int w = bounds.width - 2 * border - 1;
        int h = bounds.height - 2 * border - 1;

        int x1 = x0 + w;
        int y1 = y0 + h;
        int xM = (x0 + x1) / 2;
        int yM = (y0 + y1) / 2;
        gc.setLineWidth(Math.max(1, border / 2));
        gc.setForeground(color);

        switch (kind) {
            case BRANCH_ALL:
            case BRANCH_LEFT:
            case BRANCH_LEFT_AND_RIGTH:
            case BRANCH_RIGHT:
                if (kind == Kind.BRANCH_ALL || kind == Kind.BRANCH_LEFT || kind == Kind.BRANCH_LEFT_AND_RIGTH) {
                    gc.drawLine(xM, y0 + border, x0 + border, yM);
                }
                if (kind == Kind.BRANCH_ALL || kind == Kind.BRANCH_LEFT || kind == Kind.BRANCH_RIGHT) {
                    gc.drawLine(xM, y0 + border, xM, y1 - border);
                }
                if (kind == Kind.BRANCH_ALL || kind == Kind.BRANCH_RIGHT || kind == Kind.BRANCH_LEFT_AND_RIGTH) {
                    gc.drawLine(xM, y0 + border, x1 - border, yM);
                }
                break;

            case CLASSIFIER:
                gc.setBackground(colors.grays[1]);
                gc.fillOval(x0, y0, cellSize, cellSize);
                gc.drawOval(x0, y0, cellSize, cellSize);
                break;

            case TUTORIAL:
                gc.drawPolyline(new int[]{
                        x1 - 2 * border, y0 + 4 * border,
                        x1 - 2 * border, y1 - 2 * border,
                        xM, y1 - border,
                        x0 + 2 * border, y1 - 2 * border,
                        x0 + 2 * border, y0 + 4 * border});

                gc.drawPolygon(new int[]{
                        xM, y0 + border,
                        x1, y0 + 3 * border,
                        xM, y0 + 5 * border,
                        x0, y0 + 3 * border});

                gc.drawLine(x1, y0 + 3 * border, x1, yM + (text == null ? 2 : 1) * border);
                if (kind == Kind.TUTORIAL) {
                    break;
                }

            case MODULE:
                gc.setBackground(color);
                gc.fillRoundRectangle(x0, y0, xM - x0, border * 3 / 2, border / 2, border / 2);
                gc.fillRectangle(x0, y0 + border * 3 / 4, xM - x0, border * 3 / 4);
                gc.drawRoundRectangle(x0, y0 + border * 3 / 2, w, h - border * 5 / 2, border, border);
                break;
            case CONTINUOUS_OPERATION:
                // paint.setUnderlineText(true);   FIXME
                // Fallthrough intended
            case OPERATION:
                gc.drawRoundRectangle(x0, y0 + border, w, h - 2 * border, border, border);
                // canvas.drawRect(rect, paint);
                break;
            case PROPERTY:
                gc.drawPolygon(new int[]{
                        x0 + border, y0 + border,
                        x1, y0 + border,
                        x1 - border, y1 - border,
                        x0, y1 - border});
                break;
            case SOUND:
                gc.drawPolygon(new int[]{
                        x1 - border, y0 + border,
                        x1 - border, y1 - border,
                        xM, yM + border * 3 / 2,
                        xM, yM - border * 3 / 2});

                gc.drawRectangle(x0 + border, yM - border * 3 / 2, xM - x0 - border * 3 / 2, yM - y0 + border * 3 / 2);
                return;

        }

        if (text != null) {
            gc.setForeground(kind == Kind.CLASSIFIER ? colors.white : colors.foreground);
            if (kind == Kind.TUTORIAL) {
                gc.setFont(colors.getFont(Math.round(cellSize * 0.33f), 0));
                Point size = gc.stringExtent(text);
                gc.drawString(text, x0 + w - size.x, y0 + h - size.y, true);

//            TextHelper.drawText(context, canvas, text, bounds.right, bounds.bottom, paint, TextHelper.VerticalAlign.BOTTOM);
            } else {
                gc.setFont(colors.getFont(Math.round(cellSize * (kind == Kind.CLASSIFIER ? 0.66f : 0.5f)), 0));
                Point size = gc.stringExtent(text);
                gc.drawString(text, x0 + (w - size.x) / 2, y0 + (h - size.y) / 2, true);
            //                  paint.setTextSize();
//      canvas.drawText(text, bounds.centerX(), bounds.bottom - cellSize * (kind == Kind.CLASSIFIER ? 0.25f : 0.33f), paint);
//                    TextHelper.drawText(context, canvas, text, bounds.centerX(), bounds.centerY(), paint, TextHelper.VerticalAlign.CENTER);
            }
        }


         /*else {
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
         public Point computeSize(int wHint, int hHint, boolean changed) {
             return new Point(32, 32);
         }

}
