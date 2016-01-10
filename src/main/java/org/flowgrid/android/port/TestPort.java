package org.flowgrid.android.port;

import java.util.ArrayList;

import org.flowgrid.R;
import org.flowgrid.android.graphics.Colors;
import org.flowgrid.android.graphics.Drawing;
import org.flowgrid.model.Port;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.model.container.IntMap;
import org.kobjects.emoji.android.TextHelper;
import org.kobjects.emoji.Emoji;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

public class TestPort implements Port {
  public enum Mode {
    SOURCE, CHECK
  }

  public static final String[] OUTPUT_ICONS = {"None", "Pipe"};
  
  private PortCommand command;
  ArrayList<Object> values = new ArrayList<Object>();
  ArrayList<Object> received = new ArrayList<Object>();
  Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
  final Mode mode;
  int consumed;
  int ticks = 0;
  boolean error;
  static float HEIGHT_FACTOR = 0.5f; // see ColumLayout (16 vs. 64-16)
  private static IntMap<Bitmap> bitmapCache = new IntMap<Bitmap>();
   PortManager manager;
  
  public TestPort(PortManager manager, PortCommand command) {
    this.manager = manager;
    this.command = command;
    Type dataType = command.dataType();
    mode = command.input ? Mode.SOURCE : Mode.CHECK;

    String data = command.peerJson().getString("testData", "");
    for (String s: data.split(" ")) {
      if (dataType == PrimitiveType.NUMBER) {
        values.add(Double.parseDouble(s));
      } else if (dataType == PrimitiveType.BOOLEAN) {
        values.add(Boolean.parseBoolean(s));
      } else {
        values.add(s);
      }
    }
  }

  @Override
  public void detach() {
  }

  @Override
  public void setValue(Object data) {
    int index = received.size();
    received.add(data);
    if (index >= values.size() || !data.equals(values.get(index))) {
      error = true;
    }
  }

  @Override
  public void start() {
    consumed = 0;
    ticks = 0;
    error = false;
    received.clear();
  }

  @Override
  public void stop() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void timerTick(int count) {
    ticks += count;
    if (mode == Mode.SOURCE && ticks / 60 > consumed &&
        consumed < values.size()) {
      command.sendData(manager.controller().rootEnvironment, values.get(consumed), 0);
      consumed++;
    }
  }

  @Override
  public void ping() {
    // Unsupported
  }
  
  static Bitmap getBitmap(Context context, int id) {
    Bitmap bitmap = bitmapCache.get(id);
    if (bitmap == null) {
      bitmap = BitmapFactory.decodeResource(context.getResources(), id);
      bitmapCache.put(id, bitmap);
    }
    return bitmap;
  }
  
  public void draw(Context context, Canvas canvas, float x0, float y0, float cellSize, boolean ready) {
    int h = (int) cellSize;
    
    RectF rect = new RectF();
    float y = y0 + h/2;
    String icon = command.peerJson().getString("icon", "");
    if (mode == Mode.SOURCE) {
      rect.set(x0, y0, x0 + cellSize, y0 + cellSize);
      if ("Pipe".equals(icon)) {
        canvas.drawBitmap(getBitmap(context, 
            command.dataType() == PrimitiveType.TEXT ? R.drawable.pipe_string :
              command.dataType() == PrimitiveType.BOOLEAN ? R.drawable.pipe_boolean : 
                R.drawable.pipe_number), null, rect, paint);
        y -= cellSize;
      }
    } else {
      Sprite.Mood mood;
      if (received.size() > 0) {
        mood = error ? Sprite.Mood.SAD : received.size() == values.size() ? Sprite.Mood.SUPER_HAPPY : Sprite.Mood.HAPPY;
      } else {
        mood = Sprite.Mood.NEUTRAL;
      }
      Sprite.draw(context, canvas, icon, mood, x0, y0, cellSize);
      y += cellSize;
    }
    
    
    paint.setColor(Color.WHITE);
    paint.setStrokeWidth(h/10);
    float border = h/10;
    float x = x0 + border;
    paint.setTextSize(h * HEIGHT_FACTOR);
    
    for (int i = 0; i < Math.max(values.size(), received.size()); i++) {
      String s;
      Object value = i < values.size() ? values.get(i) : null;
      boolean matches = i < received.size() && received.get(i).equals(value);
      boolean sent = mode == Mode.SOURCE ? i < consumed : false;

      float w;
      if (value != null) {
        s = (value instanceof Boolean ? Boolean.TRUE.equals(value) ? "\uf889" : "\uf888" : manager.platform().model().toString(value));
        w = TextHelper.measureText(paint, s);

        if (value instanceof Boolean || Emoji.isEmoji(s)) {
          paint.setAlpha(sent ? 0x044 : 0xff);
          if (matches) {
            Drawing.drawHalo(canvas, x + w / 2, y, w, 0xff00ff00);
          }
        } else {
          paint.setColor(matches ? Colors.GREEN[Colors.Brightness.REGULAR.ordinal()] : sent ? Color.GRAY : Color.WHITE);
          paint.setTypeface(matches ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        }
        TextHelper.drawText(context, canvas, s, x, y, paint, TextHelper.VerticalAlign.CENTER);
      } else {
        w = h * HEIGHT_FACTOR;
      }

      paint.setAlpha(0xff);
      
      if (i < received.size() && !matches) {
        paint.setColor(Colors.RED[Colors.Brightness.REGULAR.ordinal()]);
//        canvas.drawRect(x,  border, x + w - 2, h-border, paint);
        float dy = h/2 - 2 * border;
        canvas.drawLine(x, y - dy, x + w, y + dy, paint);
        canvas.drawLine(x, y + dy, x + w, y - dy, paint);
      }
      
      x += w + h / 4;
    }
  }

  public boolean passes() {
    if (mode == Mode.SOURCE) {
      return true;
    }
    if (values.size() != received.size()) {
      return false;
    }
    return !error;
  }

  public boolean outputPending() {
    return mode == Mode.SOURCE && consumed < values.size();
  }
}
