package org.flowgrid.swt.graphics;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

public class Drawing {
    public static final String SHORT_THIS_PREFIX = "\u22c5";
    public static final String THIS_PREFIX = SHORT_THIS_PREFIX + "\u202f";

    public static void drawHalo(GC gc, int x, int y, int r, Color color) {
        Color background = gc.getBackground();
        int alpha = gc.getAlpha();
        gc.setAlpha(127);
        gc.setBackground(color);
        gc.fillOval(x - r, y - r, 2 * r, 2 * r);
        gc.setAlpha(alpha);
        gc.setBackground(background);
    }
}
