package org.flowgrid.android.port;

import android.hardware.SensorManager;

import org.flowgrid.android.MainActivity;
import org.flowgrid.android.Widget;
import org.flowgrid.model.Controller;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Port;

public interface PortManager {
  SensorManager sensorManager();
  Controller controller();
  MainActivity platform();
  void start();
  boolean isRunning();
  CustomOperation operation();
  void addInput(WidgetPort inputPort);
  void removeWidget(Widget widget);
}
