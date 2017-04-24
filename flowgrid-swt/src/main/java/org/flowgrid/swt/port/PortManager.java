package org.flowgrid.swt.port;

import org.flowgrid.model.Controller;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.swt.SwtFlowgrid;

public interface PortManager {
  //  SensorManager sensorManager();
    Controller controller();
    SwtFlowgrid flowgrid();
    void start();
    boolean isRunning();
    CustomOperation operation();
    void addInput(WidgetPort inputPort);
}
