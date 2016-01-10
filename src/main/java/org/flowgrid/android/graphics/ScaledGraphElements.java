package org.flowgrid.android.graphics;

import org.flowgrid.android.graphics.Colors.Brightness;
import org.flowgrid.android.MainActivity;
import org.flowgrid.model.ArrayType;
import org.flowgrid.model.DisplayType;
import org.flowgrid.model.Model;
import org.flowgrid.model.Shape;
import org.flowgrid.model.Operation;
import org.flowgrid.model.Type;
import org.flowgrid.model.Command;
import org.flowgrid.model.api.LocalCallCommand;
import org.flowgrid.model.api.PropertyCommand;
import org.kobjects.emoji.android.TextHelper;
import org.kobjects.emoji.Emoji;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;

/**
 * Draws stuff, keeps track of the scale.
 */
public class ScaledGraphElements {
  private final Paint connectorPaint = new Paint();
  private final Paint errorPaint = new Paint();
  private final Paint operatorBoxPaint = new Paint();
  private final Paint valueBorderPaint = new Paint();
  private final Paint cellCountingPaint = new Paint();
  private final Paint operatorTextPaint = new Paint();
  
  private final Paint arrayConnectorPaint = new Paint();
  private final Paint operatorOutlinePaint = new Paint();
  private final Paint valueTextPaint = new Paint();
  private final Paint shapeTextPaint = new Paint();
  private final Paint readyBoxPaint = new Paint();
  private final Paint valueBoxPaint = new Paint();
  private final Paint labelPaint = new Paint();
  private final Paint highlightPaint = new Paint();
  
  private final RectF rect = new RectF();
  private final Path path = new Path();
  private final MainActivity platform;

  private float cellSize;

  public ScaledGraphElements(MainActivity platform, float cellSize) {
    this.platform = platform;

    connectorPaint.setStrokeCap(Cap.ROUND);
    connectorPaint.setStyle(Style.STROKE);
    connectorPaint.setAntiAlias(true);
    connectorPaint.setColor(Color.LTGRAY);
    connectorPaint.setStyle(Style.STROKE);

    cellCountingPaint.setStyle(Style.FILL);
    cellCountingPaint.setAntiAlias(true);

    Typeface condensed = Typeface.create("sans-serif-condensed", 0);

    operatorTextPaint.setTextAlign(Align.CENTER);
    operatorTextPaint.setColor(Color.LTGRAY);
    operatorTextPaint.setTypeface(condensed);
    operatorTextPaint.setAntiAlias(true);

    operatorBoxPaint.setStyle(Style.FILL);
    operatorBoxPaint.setColor(Color.BLACK);

    operatorOutlinePaint.setStyle(Style.STROKE);
    operatorOutlinePaint.setColor(Color.LTGRAY);
    operatorOutlinePaint.setAntiAlias(true);

    readyBoxPaint.setStyle(Style.FILL);
    readyBoxPaint.setColor(Color.GRAY);

    valueTextPaint.setColor(Color.WHITE);
    valueTextPaint.setTextAlign(Align.CENTER);
    valueTextPaint.setAntiAlias(true);
    valueTextPaint.setTypeface(condensed);

    shapeTextPaint.setTextAlign(Align.CENTER);
    shapeTextPaint.setTypeface(condensed);
//  valueTextPaint.setTextSkewX(-0.25f);

    valueBorderPaint.setColor(Color.BLACK);
    valueBorderPaint.setStyle(Style.STROKE);
    valueBorderPaint.setAntiAlias(true);
    valueBoxPaint.setStyle(Style.FILL);

    highlightPaint.setColor(Color.DKGRAY);
    highlightPaint.setStyle(Style.FILL);
    highlightPaint.setAlpha(127);

    labelPaint.setColor(Color.WHITE);
    labelPaint.setAntiAlias(true);
    labelPaint.setTypeface(condensed);

    errorPaint.setColor(Color.RED);
    errorPaint.setStyle(Style.STROKE);
    errorPaint.setAntiAlias(true);
    setCellSize(cellSize);
  }

  public void setCellSize(float cellSize) {
    this.cellSize = cellSize;
    arrayConnectorPaint.setStrokeWidth(2 * cellSize / 16);
    errorPaint.setStrokeWidth(cellSize / 8);
    operatorOutlinePaint.setStrokeWidth(cellSize / 16);
    shapeTextPaint.setTextSize(cellSize);
    valueBorderPaint.setStrokeWidth(cellSize / 32);
    connectorPaint.setStrokeWidth(cellSize / 16);
    valueTextPaint.setTextSize(cellSize / 3);
  }
  
  float cellSize() {
    return cellSize;
  }
  
  public void drawConnector(Canvas canvas, float x0, float y0, float x1, float y1, Type type) {
    int color = Colors.typeColor(type, false);
    if (type instanceof ArrayType) {
      arrayConnectorPaint.setColor(color);
      canvas.drawLine(x0, y0, x1, y1, arrayConnectorPaint);
      connectorPaint.setColor(Color.BLACK);
    } else {
      connectorPaint.setColor(color);
    }
    canvas.drawLine(x0, y0, x1, y1, connectorPaint);
  }

  public void drawData(Canvas canvas, float x, float y, Object value, boolean constant, int mod) {
    String text = Model.toString(value, DisplayType.GRAPH);
      
    float h = cellSize / 2;
    float border = h / 8;
    float width = TextHelper.measureText(valueTextPaint, text) + h / 2;
//      rectF.left = x - width / 2 - border;
//      rectF.top = y - h / 2 - border;
//      rectF.right = x + width / 2 + border;
//      rectF.bottom = y + h / 2 + border;
      // canvas.drawRoundRect(rectF, border, border, constant ? constantValueBoxPaint : valueBoxPaint);
    float tY = y - h/2 + (h - valueTextPaint.descent() - valueTextPaint.ascent()) / 2; 
      
    float x0 = x - width / 2;
    float y0 = y - h / 2;
    float x1 = x0 + width;
    float y1 = y0 + h;
      
    Paint paint = valueTextPaint;
    if (constant) {
      valueBoxPaint.setColor(mod == 0 ? Color.BLACK : Color.GRAY);
      valueBorderPaint.setColor(Color.LTGRAY);
      valueTextPaint.setColor(Color.LTGRAY);
      path.rewind();
      path.moveTo(x0, y0);
      path.lineTo(x1 + border, y0);
      path.lineTo(x1, y1);
      path.lineTo(x0 - border, y1);
      path.close();
      canvas.drawPath(path, valueBoxPaint);
      canvas.drawPath(path, valueBorderPaint);
    } else if (value instanceof Boolean || (value instanceof String && Emoji.isEmoji(text))) {
      if (value instanceof Boolean) {
        text = value.equals(Boolean.TRUE) ? "\uf889" : "\uf888";
      }
      operatorTextPaint.setTextSize(cellSize / 2);
      operatorTextPaint.setTextAlign(Align.CENTER);
      if (mod > 0) {
        Drawing.drawHalo(canvas, x, y, cellSize * 2 / 3, Color.WHITE);
      }
      
      paint = operatorTextPaint;
    } else {
      valueTextPaint.setColor(Color.WHITE);
      valueBorderPaint.setColor(Color.BLACK);
      int rgb;
      Brightness brightness = mod > 0 ? Colors.Brightness.BRIGHTER : Colors.Brightness.DARKEST;
      rgb = Colors.typeColor(platform.model().type(value), brightness);
      if (mod > 0) {
        valueBoxPaint.setColor(rgb);
      } else {
        mod = -mod + 1;
        valueBoxPaint.setARGB(255/mod, ((rgb >> 16) & 255) ,
            ((rgb >> 8) & 255), (rgb & 255));
      }
      rect.top = y0;
      rect.bottom = y1;
      rect.left = x0;
      rect.right = x1;
      canvas.drawRoundRect(rect, 2 * border, 2 * border, valueBoxPaint);
      canvas.drawRoundRect(rect, 2 * border, 2 * border, valueBorderPaint);
    }
    TextHelper.drawText(platform, canvas, text, x, tY, paint);
  }


  public void drawShape(Canvas canvas, Shape shape, String text, float x0, float y0, int width, boolean highlight, boolean passThrough) {
    float x1 = x0 + width * cellSize;
    float y1 = y0 + cellSize;
    float xM = (x0 + x1) / 2;
    float yM = (y0 + y1) / 2;
    float border = cellSize / 10;

    Paint boxPaint = highlight ? readyBoxPaint : operatorBoxPaint;
    //   float b2 = border * 4 / 3;// + border * 2 / 3;

    float tX = x0 + cellSize * width / 2;

    switch(shape) {
      case SQUARE:
      case CIRCLE: {
        //   tX = width > 1 ? x0 + cellSize * 3 / 4 : x0 + cellSize / 2;

        rect.top = y0 + border;
        rect.bottom = y1 - border;
        if (width == 2) {
          rect.left = x0 + cellSize / 2 -  border;
          rect.right = x1 - cellSize / 2 + border;
        } else {
          rect.left = x0 + border;
          rect.right = x1 - border;
        }
        if (shape == Shape.CIRCLE) {
          canvas.drawRoundRect(rect, (rect.right - rect.left) / 2, cellSize / 2 - border, operatorBoxPaint);
          canvas.drawRoundRect(rect, (rect.right - rect.left) / 2, cellSize / 2 - border, operatorOutlinePaint);
        } else {
          canvas.drawRect(rect, operatorBoxPaint);
          canvas.drawRect(rect, operatorOutlinePaint);
        }
        break;
      }
      case FOLDER: {
        rect.left = x0 + border;
        rect.top = y0 + border;
        rect.bottom = y0 + 5 * border;
        rect.right = xM;
        canvas.drawRoundRect(rect, border, border, operatorTextPaint);
        canvas.drawRoundRect(rect, border, border, operatorOutlinePaint);

        rect.left = x0 + border;
        rect.top = y0 + 2.5f * border;
        rect.bottom = y1 - border;
        rect.right = x1 - border;
        canvas.drawRoundRect(rect, border, border, boxPaint);
        canvas.drawRoundRect(rect, border, border, operatorOutlinePaint);
        break;
      }
      case HEXAGON: {
        path.rewind();
        path.moveTo(x0 + 2*border, y0 + border);
        path.lineTo(x1 - 2*border, y0 + border);
        path.lineTo(x1, yM);
        path.lineTo(x1 - 2*border, y1 - border);
        path.lineTo(x0 + 2*border, y1 - border);
        path.lineTo(x0, yM);
        path.close();
        canvas.drawPath(path, boxPaint);
        canvas.drawPath(path, operatorOutlinePaint);
        break;
      }
      case INPUT_OUTPUT: {
        path.rewind();
        path.moveTo(x0, y0 + border);
        path.lineTo(x1 - border, y0 + border);
        path.lineTo(x1, yM);
        path.lineTo(x1 - border, y1 - border);
        path.lineTo(x0, y1 - border);
        path.lineTo(x0 + border, yM);
        path.close();
        canvas.drawPath(path, boxPaint);
        canvas.drawPath(path, operatorOutlinePaint);
        break;
      }
      case INPUT: {
        path.rewind();
        path.moveTo(x0, y0 + border);
        path.lineTo(x1 - border, y0 + border);
        path.lineTo(x1 - border, y1 - border);
        path.lineTo(x0, y1 - border);
        path.lineTo(x0 + border, yM);
        path.close();
        canvas.drawPath(path, boxPaint);
        canvas.drawPath(path, operatorOutlinePaint);
        break;
      }
      case PACKAGE:
        path.rewind();
        path.moveTo(x0 + border, y0 + 2 * border);
        path.lineTo(xM, y0);
        path.lineTo(x1 - border, y0 + 2 * border);
        path.lineTo(x1 - border, y1 - 2 * border);
        path.lineTo(xM, y1);
        path.lineTo(x0 + border, y1 - 2 * border);
        path.close();
        canvas.drawPath(path, boxPaint);
        canvas.drawPath(path, operatorOutlinePaint);
        canvas.drawLine(x0 + border, y0 + 2 * border, xM, y0 + 4 * border, operatorOutlinePaint);
        canvas.drawLine(x1 - border, y0 + 2 * border, xM, y0 + 4 * border, operatorOutlinePaint);
        canvas.drawLine(xM, y1, xM, y0 + 4* border, operatorOutlinePaint);
        break;

      case PARALLELOGRAM: {
        path.rewind();
        path.moveTo(x0 + border, y0 + border);
        path.lineTo(x1, y0 + border);
        path.lineTo(x1 - border, y1 - border);
        path.lineTo(x0, y1 - border);
        path.close();
        canvas.drawPath(path, boxPaint);
        if (passThrough) {
          connectorPaint.setColor(0x8899cc00);
          canvas.drawLine(x0 + cellSize / 2, y0 + border,
              x0 + cellSize / 2, y1 - border, connectorPaint);
        }
        canvas.drawPath(path, operatorOutlinePaint);
        break;
      }
      case RHOMBUS: {
        path.rewind();
        path.moveTo(xM, y0);
        path.lineTo(x1, yM);
        path.lineTo(xM, y1);
        path.lineTo(x0, yM);
        path.close();
        canvas.drawPath(path, boxPaint);
        canvas.drawPath(path, operatorOutlinePaint);
        break;
      }
      case STAR: {
        path.rewind();
        path.moveTo(x0, y0);
        path.lineTo(xM, y0 + border);
        path.lineTo(x1, y0);
        path.lineTo(x1 - border, yM);
        path.lineTo(x1, y1);
        path.lineTo(xM, y1 - border);
        path.lineTo(x0, y1);
        path.lineTo(x0 + border, yM);
        path.close();
        canvas.drawPath(path, boxPaint);
        canvas.drawPath(path, operatorOutlinePaint);
        break;
      }
      case SOUND:
        path.rewind();
        path.moveTo(x1 - border, y0 + border);
        path.lineTo(x1 - border, y1 - border);
        path.lineTo(xM, yM + 1.5f * border);
        path.lineTo(xM, yM - 1.5f * border);
        path.close();
        canvas.drawPath(path, boxPaint);
        canvas.drawPath(path, operatorOutlinePaint);
        rect.left = x0 + border;
        rect.right = xM - 1.5f * border;
        rect.top = yM - 1.5f * border;
        rect.bottom = yM + 1.5f * border;
        canvas.drawRect(rect, boxPaint);
        canvas.drawRect(rect, operatorOutlinePaint);
        return;
      case STORAGE: {
        path.rewind();
        path.moveTo(x0 + 2 * border, y0 + border);
        path.lineTo(x1, y0 + border);
        path.cubicTo(x1 - 2 * border, y0 + border, x1 - 2 * border, y1 - border, x1, y1 - border);
        path.lineTo(x0 + 2 * border, y1 - border);
        path.cubicTo(x0, y1 - border, x0, y0 + border, x0 + 2 * border, y0 + border);
        path.close();
        canvas.drawPath(path, boxPaint);
        canvas.drawPath(path, operatorOutlinePaint);
        break;
      }
      case OUTPUT: {
        path.rewind();
        path.moveTo(x0 + border, y0 + border);
        path.lineTo(x1 - border, y0 + border);
        path.lineTo(x1, yM);
        path.lineTo(x1 - border, y1 - border);
        path.lineTo(x0 + border, y1 - border);
        path.close();
        canvas.drawPath(path, boxPaint);
        canvas.drawPath(path, operatorOutlinePaint);
        break;
      }
      case TRAPEZOID_DOWN: {
        path.rewind();
        path.moveTo(x0, y0 + border);
        path.lineTo(x1, y0 + border);
        path.lineTo(x1 - border, y1 - border);
        path.lineTo(x0 + border, y1 - border);
        path.close();
        canvas.drawPath(path, boxPaint);
        canvas.drawPath(path, operatorOutlinePaint);
        break;
      }
      case TRAPEZOID_UP: {
        path.rewind();
        path.moveTo(x0 + border, y0 + border);
        path.lineTo(x1 - border, y0 + border);
        path.lineTo(x1, y1 - border);
        path.lineTo(x0, y1 - border);
        path.close();
        canvas.drawPath(path, boxPaint);
        canvas.drawPath(path, operatorOutlinePaint);
        break;
      }

      case BAR:
        canvas.drawLine(x0 + border, yM - border, x1 - border, yM - border, operatorOutlinePaint);
        canvas.drawLine(x0 + border, yM + border, x1 - border, yM + border, operatorOutlinePaint);
        // No text, crosses
        return;

      default:
        rect.left = x0 + border;
        rect.top = y0 + border;
        rect.bottom = y1 - border;
        rect.right = x1 - border;
        if (shape == Shape.OVAL) {
          float r = (rect.bottom - rect.top) / 2;
          canvas.drawRoundRect(rect, r, r, boxPaint);
          canvas.drawRoundRect(rect, r, r, operatorOutlinePaint);
        } else if (shape == Shape.ROUNDED_RECTANGLE || shape == Shape.ASYNC) {
          canvas.drawRoundRect(rect, border, border, boxPaint);
          canvas.drawRoundRect(rect, border, border, operatorOutlinePaint);
        } else {
          canvas.drawRect(rect, boxPaint);
          canvas.drawRect(rect, operatorOutlinePaint);
        }

        if (passThrough) {
          connectorPaint.setColor(0x8899cc00);
          canvas.drawLine(x0 + cellSize / 2, y0 + border,
              x0 + cellSize / 2, y1 - border, connectorPaint);
        }
    }

    if (text == null || text.isEmpty()) {
      return;
    }

    int len = text.length();
    operatorTextPaint.setTextSize(cellSize / 2);
    operatorTextPaint.setTextAlign(Align.CENTER);

    float available = cellSize * width - cellSize / 3;


    operatorTextPaint.setUnderlineText(shape == Shape.ASYNC);
    if (operatorTextPaint.measureText(text, 0, len) > available) {
      operatorTextPaint.setTextSize(cellSize / 3);
      while (operatorTextPaint.measureText(text, 0, len) > available) {
        len--;
      }
      if (len < text.length()) {
        int start2 = len;
        int sp = text.indexOf(' ');
        if (sp != -1) {
          start2 = sp + 1;
        } else {
          for (int i = len - 1; i > 0; i--) {
            if (Character.isLowerCase(text.charAt(i)) && Character.isUpperCase(text.charAt(i + 1))) {
              start2 = i + 1;
              break;
            }
          }
        }
        if (start2 < len) {
          len = start2;
        }
        int len2 = text.length();
        while (operatorTextPaint.measureText(text, start2, len2) > available) {
          len2--;
        }


        float tY = y0 - cellSize / 6 + (cellSize - operatorTextPaint.descent() - operatorTextPaint.ascent()) / 2;
        canvas.drawText(text, 0, len, tX, tY, operatorTextPaint);
        tY += cellSize / 3;
        canvas.drawText(text, start2, len2, tX, tY, operatorTextPaint);
        return;
      }
    }

    float tY = y0 + (cellSize - operatorTextPaint.descent() - operatorTextPaint.ascent()) / 2;
    canvas.drawText(text, 0, len, tX, tY, operatorTextPaint);
  }

  public void drawOperator(Canvas canvas, Command cmd, float x0, float y0, int width, boolean ready) {
    boolean passSelf = false;
    String text = cmd.name();
    if (cmd instanceof Operation) {
      Operation op = (Operation) cmd;
      passSelf = op.classifier != null;
    } else if (cmd instanceof PropertyCommand) {
      PropertyCommand pc = (PropertyCommand) cmd;
      if (pc.implicitInstance()) {
        text = Drawing.THIS_PREFIX + text;
      } else {
        passSelf = pc.property().classifier != null;
      }
    } else if (cmd instanceof LocalCallCommand) {
      text = Drawing.THIS_PREFIX + text;
    }

    if ("and".equals(text)) {
      text = "&";//\u2227";
    } else if ("or".equals(text)) {
      text = "\u2228";
    } else if("xor".equals(text)) {
      text = "\u2295";
    } else if ("not".equals(text)) {
      text = "\u00ac";
    } 
    drawShape(canvas, cmd.shape(), text, x0, y0, width, ready, passSelf);
  }

  public void drawOpenConnection(Canvas canvas, float toX, float toY, Type type) {
    connectorPaint.setColor(Colors.typeColor(type, false));
    connectorPaint.setStyle(Style.FILL);
    canvas.drawCircle(toX, toY, cellSize / 16f, connectorPaint);
    connectorPaint.setStyle(Style.STROKE);
  }

  public void drawErrorMarker(Canvas canvas, float x, float y0) {
    float radius = cellSize / 4;
    canvas.drawLine(x - radius, y0 - radius, x + radius, y0 + radius, errorPaint);
    canvas.drawLine(x - radius, y0 + radius, x + radius, y0 - radius, errorPaint);
  }

  public void drawBuffer(Canvas canvas, float x, float y) {
    valueBorderPaint.setColor(Color.LTGRAY);
    float radius = cellSize / 6;
    canvas.drawRect(x - radius, y - radius, x + radius, y + radius, operatorBoxPaint);
    canvas.drawRect(x - radius, y - radius, x + radius, y + radius, valueBorderPaint);
  }

  public void highlightCell(Canvas canvas, float x0, float y0, int color) {
    cellCountingPaint.setColor(color);
    canvas.drawRect(x0, y0, x0 + cellSize, y0 + cellSize, cellCountingPaint);
  }
}
