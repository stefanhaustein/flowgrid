package org.flowgrid.swt.widget;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public class ColumnLayout extends Layout {

    int[] percentages;
    public ColumnLayout(int... percentages) {
        this.percentages = percentages;
    }

    @Override
    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        Control[] children = composite.getChildren();
        int height = hHint;
        int width = wHint;
        int consumedPercent = 0;
        for (int i = 0; i < children.length; i++) {
            int percent;
            if (i >= percentages.length) {
                percent = (100 - consumedPercent) / (children.length - percentages.length);
            } else {
                percent = percentages[i];
                consumedPercent += percent;
            }
            Point childSize = children[i].computeSize(wHint == -1 ? -1 : wHint * percent / 100, hHint);
            if (wHint == -1) {
                width = Math.max(width, childSize.x * 100 / percent);
            }
            if (hHint == -1) {
                height = Math.max(height, childSize.y);
            }
        }
        return new Point(width, Math.max(height, 0));
    }

    @Override
    protected void layout(Composite composite, boolean flushCache) {
        Control[] children = composite.getChildren();
        Rectangle available = composite.getClientArea();
        int x = available.x;
        int consumedPercent = 0;
        for (int i = 0; i < children.length - 1; i++) {
            int percent;
            if (i >= percentages.length) {
                percent = (100 - consumedPercent) / (children.length - percentages.length);
            } else {
                percent = percentages[i];
                consumedPercent += percent;
            }
            int w = available.width * percent / 100;
            children[i].setBounds(x, available.y, w, available.height);
            x += w;
        }
        if (children.length > 0) {
            children[children.length - 1].setBounds(x, available.y,
                    available.width - (x - available.x), available.height);
        }
    }
}
