package org.flowgrid.swt.port;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.flowgrid.model.Port;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.swt.api.CanvasControl;
import org.flowgrid.swt.data.DataComponent;
import org.flowgrid.swt.widget.Component;

public class WidgetPort implements Component, Port {

    public final PortCommand port;
    private final PortManager manager;
    DataComponent dataWidget;
    Type type;
    /*Histogram histogram;
    RunChart runChart;
    WebView webView;
    View view; 
    String inputUrl;
    Button button;
*/
    boolean buttonPressed;
    CanvasControl canvasControl;
    Control control;

    public WidgetPort(final PortManager manager, PortCommand port) {
        type = port.dataType();

        this.manager = manager;
        this.port = port;
        manager.addInput(this);
    }


    public PortCommand port() {
        return port;
    }

    public void start() {
        if (canvasControl != null) {
            canvasControl.removeAll();
//            canvasControl.setOnClickListener((View.OnClickListener) null);    // FIXME
        }
        sendValue();
    }


    public void sendValue() {
        if (port.outputCount() != 0) {
            if (dataWidget != null) {
                sendValue(dataWidget.value());
            } else if (canvasControl != null) {
                sendValue(canvasControl);
            } /*else if (button != null) {
                sendValue(button.isPressed());
            }*/
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
        } /*else if (histogram != null) {
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
        } */
    }

    public Object value() {
        return dataWidget != null ? dataWidget.value() : canvasControl != null ? canvasControl : null;
    }

    @Override
    public void stop() {
    }

    @Override
    public void ping() {
        sendValue();
    }


    public void detach() {
        dispose();
    }

    @Override
    public void timerTick(int frames) {
        /*
        if (runChart != null) {
            runChart.timerTick(frames);
        } else */
        if (canvasControl != null) {
            canvasControl.timerTick(frames);
        }
    }

    @Override
    public Control getControl() {
        return control;
    }


    public Control createControl(Composite parent) {
        String widget = port.peerJson().getString("widget", "").toLowerCase();

        if (widget.equals("button")) {
            Button button = new Button(parent, SWT.PUSH);
            button.setText(port.name());
            button.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseDown(MouseEvent e) {
                    trigger(true);
                }

                @Override
                public void mouseUp(MouseEvent e) {
                    trigger(false);
                }

                void trigger(boolean sendValue) {
                    buttonPressed = sendValue;
                    if (!manager.operation().asyncInput() || !manager.isRunning()) {
                        manager.start();  // This will send all values
                    } else {
                        sendValue(sendValue);
                    }
                }
            });
            control = button;
        } else if (widget.equals("canvas")) {
            control = canvasControl = new CanvasControl(parent, manager.flowgrid(), manager.controller());
        /*} else if (widget.equals("runchart")) {
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
*/
        } else {
            dataWidget = new DataComponent.Builder(manager.flowgrid())
                    .setType(port.dataType())
                    .setReadonly(port.outputCount() == 0)
                    .setWidget(widget)
                    .setName(port.name()).build(parent);
//            view = dataWidget.view();
            if (port.input) {
                if (type == PrimitiveType.BOOLEAN) {
                    dataWidget.setValue(false);
                 } else if (type == PrimitiveType.TEXT) {
                    dataWidget.setValue("");
                } else if (type == PrimitiveType.NUMBER) {
                    dataWidget.setValue(0.0);
                }
            }

            dataWidget.setOnValueChangedListener(new DataComponent.OnValueChangedListener() {
                @Override
                public void onValueChanged(final Object newValue) {
                    //view.setBackgroundColor(0);
                    if (!manager.operation().asyncInput() || !manager.isRunning()) {
                        manager.start();  // This will send all values
                    } else {
                        sendValue();
                    }
                }
            });
            control = dataWidget.getControl();
        }

        /*
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
        */
        return control;
    }

    @Override
    public void dispose() {
        if (dataWidget != null) {
            dataWidget.dispose();
        } else {
            control.dispose();
        }
    }

/*
    public void updateView() {
        if (dataWidget != null) {
            dataWidget.updateView();
        }
    }


*/

}
