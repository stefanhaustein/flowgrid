package org.flowgrid.awt;

import org.eclipse.swt.widgets.AwtDisplay;
import org.flowgrid.swt.SwtFlowgrid;

public class AwtFlowgrid {

    public static void main(String[] args) {
        AwtDisplay display = new AwtDisplay();
        new SwtFlowgrid(display);
    }

}