package org.flowgrid.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.flowgrid.model.ArrayType;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ResourceManager {

    public final float pixelPerDp;

    public enum Icon {FAST_FORWARD, MORE_VERT, PAUSE, PLAY_ARROW, STOP, MENU, SLOW_MOTION_VIDEO};

    public enum Brightness {DARKEST, DARKER, REGULAR, BRIGHTER, BRIGHTEST};


    public static int brightenChannel(int value, boolean brighten) {
        return brighten ? Math.min(255, (255 - value) / 2 + value) : value;
    }

    private Map<Integer,Font> fontMap = new HashMap<>();

    public final Color black;
    public final Color white;

    public final Color background;
    public final Color foreground;
    public final Color grid;
    public final Color origin;
    public final Color selection;
    public final Color highlight;

    public final Color[] blues;
    public final Color[] violets;
    public final Color[] greens;
    public final Color[] oranges;
    public final Color[] reds;
    public final Color[] grays;
    public final Display display;
    public final boolean dark;
    private Map<Integer,Color> colors = new HashMap<>();
    private Map<Icon,Image> icons = new HashMap<>();

    private Color c(int argb) {
        return new Color(display, (argb >> 16) & 255, (argb >> 8) & 255, argb & 255);
    }

    ResourceManager(Display display, boolean dark, float pixelPerDp) {
        this.display = display;
        this.dark = dark;
        this.pixelPerDp = pixelPerDp;

        black = display.getSystemColor(SWT.COLOR_BLACK);
        white = display.getSystemColor(SWT.COLOR_WHITE);

        blues = new Color[]{c(0xff0099cc), c(0xff1da9da), c(0xff33b5e5), c(0xff8ad5f0), c(0xffe2f4fb)};
        violets = new Color[]{c(0xff9933cc), c(0xffb368d9), c(0xffc58be2), c(0xffd6adeb), c(0xfff5eafa)};
        greens = new Color[]{c(0xff669900), c(0xff83b600), c(0xff99cc00), c(0xffc5e26d), c(0xfff0f8db)};
        oranges = new Color[]{c(0xffff8a00), c(0xffffa00e), c(0xffffbd21), c(0xffffd980), c(0xfffff6df)};
        reds = new Color[]{c(0xffcc0000), c(0xffe21d1d), c(0xffff4444), c(0xffff9494), c(0xffffe4e4)};
        grays = new Color[]{c(0xff444444), c(0xff888888), c(0xffcccccc), c(0xffe8e8e8), c(0xffffffff)};

        background = dark ? black : white;
        foreground = dark ? grays[Brightness.BRIGHTER.ordinal()] : grays[Brightness.DARKEST.ordinal()];

        highlight = dark ? grays[Brightness.DARKEST.ordinal()] : grays[Brightness.BRIGHTER.ordinal()];

        grid = dark ? grays[0] : grays[Brightness.BRIGHTER.ordinal()];
        origin = dark ? white : black;
        selection = new Color(display, 0x33, 0xb5, 0xe5, 0x88);
    }


    public Color typeColor(Type type, boolean ready) {
        return typeColor(type, ready ? (dark ? ResourceManager.Brightness.BRIGHTEST : Brightness.DARKER) : ResourceManager.Brightness.REGULAR);
    }

    public Color typeColor(Type type, Brightness brightness) {
        if (type instanceof ArrayType) {
            type = ((ArrayType) type).elementType;
        }

        int index = brightness.ordinal();
        if (type == PrimitiveType.NUMBER) {
            return blues[index];
        }
        if (type == PrimitiveType.BOOLEAN) {
            return oranges[index];
        }
        if (type == PrimitiveType.TEXT) {
            return violets[index];
        }
        if (type instanceof Classifier) {
            return greens[index];
        }
        return grays[index];
    }

    public Font getFont(int size, int style) {
        int id = (size << 2) | (style & 3);
        Font font = fontMap.get(id);
        if (font == null) {
            font = new Font(display,  "Roboto", size, style);
            fontMap.put(id, font);
        }
        return font;
    }

    public Color getColor(int argb) {
        Color result = colors.get(argb);
        if (result == null) {
            result = new Color(display, (argb >> 16) & 255, (argb >> 8) & 255, argb & 255, (argb >> 24) & 255);
            colors.put(argb, result);
        }
        return result;
    }

    public void releaseColor(Color color) {

    }

    public int dpToPx(int dp) {
        return Math.round(pixelPerDp * dp);
    }


    public Image getIcon(Icon id) {
        Image image = icons.get(id);
        if (image == null) {
            String resourceName = "/icons/ic_" + id.name().toLowerCase() + (dark ? "_white_24dp.png" : "_black_24dp.png");
            InputStream is = getClass().getResourceAsStream(resourceName);
            if (is == null) {
                throw new RuntimeException("Resource not found: " + resourceName);
            }
            image = new Image(display, is);

            int expectedSize = Math.round(24 * pixelPerDp);
            Rectangle bounds = image.getBounds();

            if (bounds.width != expectedSize || bounds.height != expectedSize) {
                Image scaledImage = new Image(display, expectedSize, expectedSize);
                GC gc = new GC(scaledImage);
                gc.setAntialias(SWT.ON);
                gc.drawImage(image, 0, 0, bounds.width, bounds.height, 0, 0, expectedSize, expectedSize);
                image = scaledImage;
            }

            icons.put(id, image);
        }
        return image;
    }


    public void dispose() {
        for (Font font : fontMap.values()) {
            font.dispose();
        }
    }

}
