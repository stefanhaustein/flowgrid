package org.flowgrid.swt.port;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.flowgrid.emoji.Emoji;
import org.flowgrid.model.Port;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.swt.ResourceManager;
import org.flowgrid.swt.graphics.Drawing;
import org.flowgrid.swt.graphics.EmojiTextHelper;
import org.flowgrid.swt.graphics.ImageCache;


public class TestPort implements Port {
    public enum Mode {
        SOURCE, CHECK
    }

    public static final String[] OUTPUT_ICONS = {"none", "pipe"};

    private PortCommand command;
    ArrayList<Object> values = new ArrayList<Object>();
    ArrayList<Object> received = new ArrayList<Object>();
   // Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    final Mode mode;
    int consumed;
    int ticks = 0;
    boolean error;
    static float HEIGHT_FACTOR = 0.5f; // see ColumLayout (16 vs. 64-16)
    private static HashMap<String,Image> bitmapCache = new HashMap<String,Image>();
    PortManager manager;
    ResourceManager resourceManager;

    public TestPort(PortManager manager, PortCommand command) {
        this.manager = manager;
        this.command = command;
        this.resourceManager = manager.flowgrid().resourceManager;
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

    public void draw(GC gc, int x0, int y0, float cellSize, boolean ready) {
        int h = (int) cellSize;

        int y = y0 + h/2;
        String icon = command.peerJson().getString("icon", "").toLowerCase(Locale.US);
        if (mode == Mode.SOURCE) {
            if ("pipe".equalsIgnoreCase(icon)) {
                String id = command.dataType() == PrimitiveType.TEXT ? "pipe_string" :
                        command.dataType() == PrimitiveType.BOOLEAN ? "pipe_boolean": "pipe_number";
                Image image = ImageCache.getImage(gc.getDevice(), "/sprites/" + id + ".png");
                Rectangle imageBounds = image.getBounds();
                gc.drawImage(image, 0, 0, imageBounds.width, imageBounds.height, x0, y0, Math.round(cellSize), Math.round(cellSize));
                y -= cellSize;
            }
        } else {
            Sprite.Mood mood;
            if (received.size() > 0) {
                mood = error ? Sprite.Mood.SAD : received.size() == values.size() ? Sprite.Mood.SUPER_HAPPY : Sprite.Mood.HAPPY;
            } else {
                mood = Sprite.Mood.NEUTRAL;
            }
            Sprite.draw(gc, icon, mood, x0, y0, cellSize);
            y += cellSize;
        }


        gc.setForeground(manager.flowgrid().resourceManager.white);
        gc.setLineWidth(h/10);
        int border = h/10;
        int x = x0 + border;
        int fontSize = Math.round(h * HEIGHT_FACTOR);
        gc.setFont(manager.flowgrid().resourceManager.getFont(fontSize, 0));

        for (int i = 0; i < Math.max(values.size(), received.size()); i++) {
            String s;
            Object value = i < values.size() ? values.get(i) : null;
            boolean matches = i < received.size() && received.get(i).equals(value);
            boolean sent = mode == Mode.SOURCE ? i < consumed : false;

            int w;
            if (value != null) {
                s = (value instanceof Boolean ? Boolean.TRUE.equals(value) ? "\uf889" : "\uf888" :
                        manager.flowgrid().model().toString(value));
                w = EmojiTextHelper.stringExtent(gc, s);

                if (value instanceof Boolean || Emoji.isEmoji(s)) {
                    gc.setAlpha(sent ? 0x044 : 0xff);
                   if (matches) {
                        Drawing.drawHalo(gc, x, y, w, resourceManager.greens[ResourceManager.Brightness.REGULAR.ordinal()]);
                   }
                } else {
                    gc.setForeground(matches ? manager.flowgrid().resourceManager.greens[ResourceManager.Brightness.REGULAR.ordinal()]
                            : sent ? resourceManager.grays[ResourceManager.Brightness.REGULAR.ordinal()] : resourceManager.white);
                    gc.setFont(manager.flowgrid().resourceManager.getFont(fontSize, matches ? SWT.BOLD : SWT.NORMAL));
                }
                EmojiTextHelper.drawText(gc, s, x, y, SWT.CENTER, SWT.CENTER);
            } else {
                w = Math.round(h * HEIGHT_FACTOR);
            }

            gc.setAlpha(0xff);

            if (i < received.size() && !matches) {
                gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_RED));
//        canvas.drawRect(x,  border, x + w - 2, h-border, paint);
                int dy = h/2 - 2 * border;
                gc.drawLine(x, y - dy, x + w, y + dy);
                gc.drawLine(x, y + dy, x + w, y - dy);
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