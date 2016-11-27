package org.flowgrid.swt.widget;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public class WrappingLabelCage extends Layout {
    int MAX_WIDTH = 128;

    @Override
    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        Control[] children = composite.getChildren();
        if (children.length == 0) {
            return new Point(0, 0);
        }
        if (children.length > 1) {
            throw new RuntimeException("Exactly one child required");
        }
        Point result = children[0].computeSize(wHint, hHint);
        if (result.x > MAX_WIDTH) {
            result = children[0].computeSize(MAX_WIDTH, hHint);
        }
        return result;
    }

    @Override
    protected void layout(Composite composite, boolean flushCache) {
        Control[] children = composite.getChildren();
        if (children.length == 0) {
            return;
        }
        Rectangle ca = composite.getClientArea();
        children[0].setBounds(ca.x, ca.y, ca.width, ca.height);
    }
}
