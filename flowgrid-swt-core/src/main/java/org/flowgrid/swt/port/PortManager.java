package org.flowgrid.swt.port;

import org.flowgrid.model.Controller;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.widget.ControlManager;

public interface PortManager {
  //  SensorManager sensorManager();
    Controller controller();
    SwtFlowgrid flowgrid();
    void start();
    boolean isRunning();
    CustomOperation operation();
    void addInput(WidgetPort inputPort);
    void removeWidget(ControlManager widget);
}
