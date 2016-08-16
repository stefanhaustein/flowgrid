package org.flowgrid.swt.graphics;


import org.eclipse.swt.graphics.GC;

public interface Drawable {
    void draw(GC gc, int x, int y, int w, int h);
}
