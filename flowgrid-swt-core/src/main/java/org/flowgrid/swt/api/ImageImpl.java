package org.flowgrid.swt.api;

import org.eclipse.swt.graphics.Rectangle;

public class ImageImpl implements org.flowgrid.model.Image {

    private static int count;
    private final org.eclipse.swt.graphics.Image bitmap;
    private final int id = ++count;
    private final int width;
    private final int height;

    public ImageImpl(org.eclipse.swt.graphics.Image  bitmap) {
            this.bitmap = bitmap;
        Rectangle bounds = bitmap.getBounds();
        width = bounds.width;
        height = bounds.height;
    }

    public org.eclipse.swt.graphics.Image  bitmap() {
            return bitmap;
        }

    @Override
    public int width() {
            return width;
        }

    @Override
    public int height() {
            return height;
        }

    public String toString() {
            return "Image#" + id;
        }

}
