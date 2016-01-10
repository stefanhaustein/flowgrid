package org.flowgrid.android.operation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.flowgrid.android.ArtifactFragment;
import org.flowgrid.android.Views;
import org.flowgrid.android.Widget;
import org.flowgrid.android.data.DataWidget;
import org.flowgrid.android.port.FirmataPort;
import org.flowgrid.android.port.PortManager;
import org.flowgrid.android.port.SensorPort;
import org.flowgrid.android.port.TestPort;
import org.flowgrid.android.port.WidgetPort;
import org.flowgrid.model.Cell;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.Instance;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Module;
import org.flowgrid.model.Port;
import org.flowgrid.model.Property;
import org.flowgrid.model.Controller;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.android.widget.ColumnLayoutInterface;
import org.flowgrid.model.hutn.HutnObject;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public abstract class AbstractOperationFragment<V extends View> extends ArtifactFragment implements PortManager {
  static final String TAG = "AbstractOperationFragment";
  CustomOperation operation;
  Instance instance;
  Classifier classifier;

  ColumnLayoutInterface controlLayout;
  SensorManager sensorManager;
  Controller controller;
  Timer timer;

  final ArrayList<DataWidget> propertyWidgets = new ArrayList<>();
  boolean running;
  int inactiveTicks;

  static String portType(HutnObject peerJson) {
    String portType = peerJson.getString("portType", "");
    if (!portType.isEmpty()) {
      return portType;
    }
    if (peerJson.containsKey("sensor")) {
      return "Sensor";
    }
    if (peerJson.containsKey("pin")) {
      return "Ioio";
    }
    if (peerJson.containsKey("testData")) {
      return "Test";
    }
    return "Ui";
  }

  protected AbstractOperationFragment(Class<V> rootViewClass) {
    super(rootViewClass);
  }

  public Module module() {
    return operation.module;
  }
 
  Port attachPort(PortCommand portCommand) {
    Port result;
    
    String portType = portType(portCommand.peerJson());
    if (portType.equals("Sensor")) {
      result = new SensorPort(this, portCommand);
    } else if (portType.equals("Test")) {
      result = new TestPort(this, portCommand);
    } else if (portType.equals("Firmata")) {
      result = new FirmataPort(this, portCommand);
    } else {
      result = new WidgetPort(this, portCommand);
    }
 //   ports.add(result);
    portCommand.setPort(result);
    return result;
  }


  void attachAll() {
    detachAll();
    for (Cell cell: operation) {
      if (cell.command() instanceof PortCommand) {
        attachPort((PortCommand) cell.command());
      }
    }
    operation.validate();
  }
  

  @Override
  public boolean isRunning() {
    return running;
  }

  /**
   * Checks whether all input is available. If the missing parameter is
   * not null, missing input names will be accumulated there, and
   * Missing fields will be highlighted.
   */
  boolean isInputComplete(StringBuilder missing) {
    boolean checkOnly = missing == null;
    if (checkOnly) {
      missing = new StringBuilder();
    }
    for (WidgetPort port: portWidgets()) {
      WidgetPort widget = (WidgetPort) port;
      if (widget.port.input && widget.value() == null) {
        if (missing.length() > 0) {
          missing.append(", ");
        }
        missing.append(widget.port.name());
        if (!checkOnly) {
          widget.view().setBackgroundColor(0x088ff0000);
        }
      }
    }
    return missing.length() == 0;
  }

  public void onConfigurationChanged(Configuration newConfig) {
    updateLayout();
  }

  @Override
  public void onPlatformAvailable(Bundle savedInstanceState) {
    sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
   // controlLayout = new ColumnLayout(getActivity());

    this.operation = (CustomOperation) platform.model().artifact(getArguments().getString("artifact"));
    setArtifact(operation);
    classifier = operation.classifier;
    instance = classifier != null ? new Instance(classifier) : null;
    controller = new Controller(operation);
    controller.setInstance(this.instance);

    if (classifier != null) {
      TextView separator = new TextView(platform);
      separator.setText(classifier.name() + " properties");
      Views.applyEditTextStyle(separator, false);
      separator.setPaintFlags(separator.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
      controlLayout.addView(separator);
      for (Property property: classifier.properties(null)) {
        DataWidget input = new DataWidget(platform, operation, "instance", property.name());
        View view = input.view();
        controlLayout.addView(view);
        propertyWidgets.add(input);
      }
    }
  }


/*  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    if (controlLayout == null || scrollView == null) {
     scrollView = new ScrollView(getActivity());
      scrollView.setScrollbarFadingEnabled(false);
      scrollView.addView(controlLayout);
    }
    return scrollView;
  } */

  @Override
  public void onPause() {
    super.onPause();
    stop();
  }

  @Override
  public SensorManager sensorManager() {
    return sensorManager;
  }

  protected void updateMenu() {
  }

  protected void updateLayout() {
  }

  public void detachAll() {
    for (PortCommand portCommand : operation.portCommands()) {
      portCommand.detach();
    }
  }
  
  @Override
  public void onDestroy() {
    super.onDestroy();
    detachAll();
  }

  @Override
  public void start() {
    if (!operation.asyncInput() && !isInputComplete(null)) {
      return;
    }
    
    if (running) {
      stop();
    }

    inactiveTicks = 0;
    if (controller != null) {
      controller.start();
    }
    running = true;
    timer = new Timer();
    timer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            try {
              timerTick();
            } catch (Exception e) {
              Log.e("FlowGrid", "TimerTick", e);
            }
          }
        }, 33, 33);

    for (Port port : ports()) {
      if (operation.asyncInput()) {
        port.start();
      } else {
        port.ping();
      }
    }

    platform.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        updateMenu();
      }
    });
  }
  
  void stop() {
    if (!running) {
      return;
    }
    if (timer != null) {
      timer.cancel();
      timer = null;
    }
    if (operation.asyncInput()) {
      for (Port port: ports()) {
        port.stop();
      }
    }
    if (controller != null) {
      controller.stop();
    }
    running = false;
    platform.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        updateMenu();
      }
    });
  }
  
  void timerTick() {
    for (Port port : ports()) {
      port.timerTick(2);
    }
  }

  public Iterable<Port> ports() {
    return new Iterable<Port>() {
      @Override
      public Iterator<Port> iterator() {
        final Iterator<PortCommand> base = operation.portCommands().iterator();
        return new Iterator<Port>() {
          private Port next;

          @Override
          public boolean hasNext() {
            while (next == null && base.hasNext()) {
              next = base.next().port();
            }
            return next != null;
          }

          @Override
          public Port next() {
            hasNext();
            Port result = next;
            next = null;
            return result;
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      };
    };
  }


  public Iterable<WidgetPort> portWidgets() {
    return new Iterable<WidgetPort>() {
      @Override
      public Iterator<WidgetPort> iterator() {
        return new Iterator<WidgetPort>() {
          private Iterator<Port> base = ports().iterator();
          private WidgetPort next;

          @Override
          public WidgetPort next() {
            hasNext();
            WidgetPort result = next;
            next = null;
            return result;
          }

          @Override
          public boolean hasNext() {
            if (next != null) {
              return true;
            }
            while (base.hasNext()) {
              Port n = base.next();
              if (n instanceof  WidgetPort) {
                next = (WidgetPort) n;
                return true;
              }
            }
            return false;
          }

          @Override
          public void remove() {
            base.remove();
          }
        };
      };
    };
  }

  public void onResume() {
    super.onResume();
    attachAll();
/*
    for (WidgetPort widgetPort: portWidgets()) {
      widgetPort.updateView();
    }
    for (DataWidget propertyWidget: propertyWidgets) {
      propertyWidget.updateView();
    } */
    updateLayout();
  }

  private WidgetPort findWidgetPort(View view) {
    for (WidgetPort port: portWidgets()) {
      if (port.view() == view) {
        return port;
      }
    }
    return null;
  }

  @Override
  public void addInput(WidgetPort input) {
    int pos = 0;
    for (;pos < controlLayout.getChildCount(); pos++) {
      View view = controlLayout.getChildAt(pos);
      WidgetPort other = findWidgetPort(view);
      if (other == null || other.port.cell().compareTo(input.port.cell()) > 0) {
        break;
      }
    }
    View view = input.view();
    int colSpan = input.port.peerJson().getInt("width", 1);
    int rowSpan = input.port.peerJson().getInt("height", 1);
    controlLayout.addView(pos, view, colSpan, rowSpan);
  }

  @Override
  public CustomOperation operation() {
    return operation;
  }

  @Override
  public void removeWidget(Widget input) {
    controlLayout.removeView(input.view());
    //ports.remove(input);
    propertyWidgets.remove(input);
  }

  public Controller controller() {
    return controller;
  }
}
