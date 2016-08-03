package org.flowgrid.swt.widget;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface ControlManager {

    Control createControl(Composite parent);

    void disposeControl();
}
