package org.flowgrid.swt.widget;


import org.eclipse.swt.widgets.Control;

public interface Component {

    Control getControl();
    void dispose();
}
