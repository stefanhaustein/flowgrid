package org.flowgrid.android.port;

import java.util.List;

import org.flowgrid.android.api.CanvasView;
import org.flowgrid.android.api.ImageImpl;
import org.flowgrid.android.data.DataWidget;
import org.flowgrid.android.Widget;
import org.flowgrid.android.data.DataWidget.OnValueChangedListener;
import org.flowgrid.model.Instance;
import org.flowgrid.model.Port;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.android.view.Histogram;
import org.flowgrid.android.view.RunChart;

import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Single class to simplify view handling elsewhere.
 * Might not be an issue via checking for Port implements Widget though
 */
public class WidgetPort implements Widget, Port {
  public final PortCommand port;
  private final PortManager manager;
  DataWidget dataWidget;
  Type type;
  Histogram histogram;
  RunChart runChart;
  WebView webView;
  CanvasView canvasView;
  View view;
  String inputUrl;
  Button button;

  public WidgetPort(final PortManager manager, PortCommand port) {
    type = port.dataType();
    String widget = port.peerJson().getString("widget", "").toLowerCase();

    if (widget.equals("button")) {
      Button button = new Button(manager.platform());
      button.setText(port.name());
      button.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          boolean sendValue;
          if (event.getAction() == MotionEvent.ACTION_DOWN) {
            sendValue = true;
          } else if (event.getAction() == MotionEvent.ACTION_UP) {
            sendValue = false;
          } else {
            return false;
          }
          if (!manager.operation().asyncInput() || !manager.isRunning()) {
            manager.start();  // This will send all values
          } else {
            sendValue(sendValue);
          }
          return true;
        }
      });

      view = button;
    } else if (widget.equals("canvas")) {
      view = canvasView = new CanvasView(manager.platform(), manager.controller());
    } else if (widget.equals("runchart")) {
      view = runChart = new RunChart(manager.platform());
    } else if (widget.equals("histogram")) {
      view = histogram = new Histogram(manager.platform());
    } else if (widget.equals("webview")) {
      view = webView = new WebView(manager.platform());
      webView.setWebViewClient(new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
         if (!url.equals(inputUrl)) {
            sendValue(url);
          }
        }
      });

    } else {
      dataWidget = new DataWidget(manager.platform(), manager.operation(), null,
          widget, port.outputCount() != 0, "port", port.name());
      view = dataWidget.view();
      if (port.input) {
        if (type == PrimitiveType.BOOLEAN) {
          dataWidget.setValue(false);
        } else if (type == PrimitiveType.TEXT) {
          dataWidget.setValue("");
        } else if (type == PrimitiveType.NUMBER) {
          dataWidget.setValue(0.0);
        }
      }
      dataWidget.setOnValueChangedListener(new OnValueChangedListener() {
        @Override
        public void onValueChanged(final Object newValue) {
          view.setBackgroundColor(0);
          if (!manager.operation().asyncInput() || !manager.isRunning()) {
            manager.start();  // This will send all values
          } else {
            sendValue();
          }
        }
      });
    }

    if (dataWidget == null && port.peerJson().getInt("height", 1) > 0) {
      LinearLayout linearLayout = new LinearLayout(manager.platform());
      linearLayout.setOrientation(LinearLayout.VERTICAL);
      TextView label = new TextView(manager.platform());
      label.setText(port.name());
      linearLayout.addView(label);
      linearLayout.addView(view);
      ((LinearLayout.LayoutParams) view.getLayoutParams()).weight = 1;
      view = linearLayout;
    }

    this.manager = manager;
    this.port = port;
    manager.addInput(this);
  }
 
  
  public PortCommand port() {
    return port;
  }
  
  public void start() {
    if (canvasView != null) {
      canvasView.removeAll();
      canvasView.setOnClickListener((View.OnClickListener) null);
    }
    sendValue();
  }

  
  public void sendValue() {
    if (port.outputCount() != 0) {
      if (dataWidget != null) {
        sendValue(dataWidget.value());
      } else if (canvasView != null) {
        sendValue(canvasView);
      } else if (button != null) {
        sendValue(button.isPressed());
      }
    }
  }

  private void sendValue(Object value) {
    if (value != null) {
      WidgetPort.this.port.sendData(manager.controller().rootEnvironment, value, 0);
    }
  }

  @Override
  public synchronized void setValue(final Object newValue) {
    if (dataWidget != null) {
      dataWidget.setValue(newValue);
    } else if (histogram != null) {
      histogram.add(newValue);
    } else if (runChart != null) {
      if (newValue instanceof Number) {
        double d = ((Number) newValue).doubleValue();
        runChart.addData(d);
      } else if (newValue instanceof List<?>) {
        List<?> list = (List<?>) newValue;
        double[] d = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
          Object o = list.get(i);
          if (o instanceof Double) {
            d[i] = ((Double) o).doubleValue();
          }
        }
        runChart.addData(d);
      }
    } else if (canvasView != null) {
      // TODO(haustein) Remove
      if (newValue instanceof Instance) {
        canvasView.add((Instance) newValue);
      } else if (newValue instanceof ImageImpl) {

      //  canvasView.setBackground(((ImageImpl) newValue).bitmap());
      }
    } else if (webView != null) {
      manager.platform().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          webView.loadUrl(newValue.toString());
          inputUrl = webView.getUrl();
        }
      });
    } else if (histogram != null) {
      histogram.add(newValue);
    }
  }

  public Object value() {
    return dataWidget != null ? dataWidget.value() : canvasView != null ? canvasView : null;
  }

  @Override
  public void stop() {
  }

  @Override
  public void ping() {
    sendValue();
  }
  
  
  public void detach() {
    manager.removeWidget(this);
  }

  @Override
  public void timerTick(int frames) {
    if (runChart != null) {
      runChart.timerTick(frames);
    } else if (canvasView != null) {
      canvasView.timerTick(frames);
    } 
  }


  public void updateView() {
    if (dataWidget != null) {
      dataWidget.updateView();
    }
  }

  @Override
  public View view() {
    return view;
  }


}
