package org.flowgrid.swt.widget;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

public class Dialogs {

    public static void center(Shell dialog, Shell parent) {
        Rectangle parentBounds = parent.getBounds();
        Rectangle childBounds = dialog.getBounds();

        dialog.setLocation(parentBounds.x + (parentBounds.width - childBounds.width) / 2,
                parentBounds.y + (parentBounds.height - childBounds.height) / 2);
    }
}
