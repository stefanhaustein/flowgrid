package org.flowgrid.swing;

import org.eclipse.swt.widgets.SwingDisplay;
import org.flowgrid.swt.SwtFlowgrid;

public class SwingFlowgrid {

    public static void main(String[] args) {
        SwingDisplay display = new SwingDisplay();
        new SwtFlowgrid(display);
    }

}