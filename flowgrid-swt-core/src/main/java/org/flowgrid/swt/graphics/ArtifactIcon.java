package org.flowgrid.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Module;
import org.flowgrid.model.Operation;
import org.flowgrid.model.Property;
import org.flowgrid.swt.ResourceManager;

public class ArtifactIcon {
    public enum Kind {
        CONTINUOUS_OPERATION, OPERATION, PROPERTY, CLASSIFIER, MODULE, SOUND, TUTORIAL,
        BRANCH_LEFT, BRANCH_RIGHT, BRANCH_ALL, BRANCH_LEFT_AND_RIGTH, PARENT, NO_ICON, IMAGE
    }

    public static ArtifactIcon create(ResourceManager resourceManager, Artifact artifact) {
        ArtifactIcon.Kind kind;
        String text = null;
        if (artifact instanceof Module) {
            kind = ArtifactIcon.Kind.MODULE;
        } else if (artifact instanceof Classifier) {
            kind = ArtifactIcon.Kind.CLASSIFIER;
            text = ((Classifier) artifact).isInterface() ? "I" : "C";
        } else if (artifact instanceof Property){
            kind = ArtifactIcon.Kind.PROPERTY;
            text = "p";
        } else if (artifact instanceof Operation) {
            kind = ArtifactIcon.Kind.OPERATION; // ((Operation) artifact).
            if (artifact instanceof CustomOperation) {
                text = ((CustomOperation) artifact).classifier != null ? "m" : "op";
                if (((CustomOperation) artifact).asyncInput()) {
                    kind = ArtifactIcon.Kind.CONTINUOUS_OPERATION;
                }
            }
        } else {
            kind = ArtifactIcon.Kind.NO_ICON;
            text = "???";
        }
        return new ArtifactIcon(resourceManager, kind, text);
    }

    final Kind kind;
    final String text;
    final ResourceManager resourceManager;
    Color color;

    public void setEnabled(boolean enabled) {
        color = enabled ? resourceManager.foreground : resourceManager.grays[3];
    }


    public ArtifactIcon(ResourceManager resourceManager, Kind kind, String text) {
        this.resourceManager = resourceManager;
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

    public Image createImage(int size) {
        Image result = new Image(resourceManager.display, size, size);
        GC gc = new GC(result);
        draw(gc, 0, 0, size);
        gc.dispose();
        return result;
    }

    public void draw(GC gc, int x, int y, int cellSize) {
        gc.setAntialias(SWT.ON);
        int border = Math.max(1, Math.round(cellSize / 8));
        //cellSize -= 2 * border;
        int x0 = x + border;
        int y0 = y + border;
        int w = cellSize - 2 * border; //- 1;
        int h = cellSize - 2 * border;// - 1;
      //  int size = cellSize - 2 * border;

        int x1 = x0 + w;
        int y1 = y0 + h;
        int xM = (x0 + x1) / 2;
        int yM = (y0 + y1) / 2;
        gc.setLineWidth(Math.max(1, border / 2));
        gc.setForeground(color);
/*
        size debug code
        gc.setBackground(resourceManager.reds[0]);
        gc.fillRectangle(x, y, cellSize, cellSize);
        gc.setBackground(resourceManager.greens[0]);
        gc.fillRectangle(x0, y0, w, h);
*/
        switch (kind) {
            case NO_ICON:
                break;
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
                gc.setBackground(resourceManager.grays[1]);
                gc.fillOval(x0, y0, w, h);
                gc.drawOval(x0, y0, w, h);
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
            gc.setForeground(kind == Kind.CLASSIFIER ? resourceManager.white : resourceManager.foreground);
            if (kind == Kind.TUTORIAL) {
                gc.setFont(resourceManager.getFont(Math.round(cellSize * 0.33f), 0));
                Point size = gc.stringExtent(text);
                gc.drawString(text, x0 + w - size.x, y0 + h - size.y, true);

//            TextHelper.drawText(context, canvas, text, bounds.right, bounds.bottom, paint, TextHelper.VerticalAlign.BOTTOM);
            } else {
                gc.setFont(resourceManager.getFont(Math.round(cellSize * (kind == Kind.CLASSIFIER ? 0.66f : 0.5f)), 0));
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
      paint.setColor(ResourceManager.GRAY[0]);
      canvas.drawRect(rect, paint);
      paint.setColor(ResourceManager.GRAY[3]);
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

}
