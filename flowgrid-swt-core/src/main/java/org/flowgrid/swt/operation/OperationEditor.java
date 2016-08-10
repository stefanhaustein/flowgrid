package org.flowgrid.swt.operation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;
import org.flowgrid.model.Cell;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.Controller;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Instance;
import org.flowgrid.model.Port;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.port.PortManager;
import org.flowgrid.swt.port.WidgetPort;
import org.flowgrid.swt.widget.ControlManager;
import sun.rmi.runtime.Log;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class OperationEditor implements PortManager {

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


    SwtFlowgrid flowgrid;
    CustomOperation operation;
    Instance instance;
    Classifier classifier;
    Controller controller;
    Composite controlPanel;
    int inactiveTicks;
    OperationCanvas operationCanvas;
    boolean tutorialMode;
    public int counted;
    public int countedToRow;
    boolean running;
    ScrolledComposite scrolledComposite;
    Timer timer;

    public OperationEditor(SwtFlowgrid flowgrid, CustomOperation operation) {
        this.flowgrid = flowgrid;
        this.operation = operation;

        classifier = operation.classifier;
        instance = classifier != null ? new Instance(classifier) : null;
        controller = new Controller(operation);
        controller.setInstance(this.instance);

        /*
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
        */

        // operation.ensureLoaded(); // Fixme
        operation.validate();

        // UI Setup

        for(Control control: flowgrid.shell().getChildren()) {
            control.dispose();
        }

        final GridLayout shellLayout = new GridLayout(2, false);
        shellLayout.marginWidth = 0;
        shellLayout.marginHeight = 0;
        flowgrid.shell().setLayout(shellLayout);

        scrolledComposite = new ScrolledComposite(flowgrid.shell(), SWT.V_SCROLL);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        controlPanel = new Composite(scrolledComposite, SWT.NONE);

        final GridLayout controlLayout = new GridLayout(1, false);
        controlLayout.marginHeight = 0;
        controlLayout.marginWidth = 0;
        controlPanel.setLayout(controlLayout);
        Label label = new Label(controlPanel, SWT.NONE);
        label.setText(operation.name());
        scrolledComposite.setContent(controlPanel);

        controller.setVisual(true);
        operationCanvas = new OperationCanvas(this, flowgrid.shell());
        operationCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        attachAll();

        for (int i = 0; i < 10; i++) {
            System.out.println("");
        }

       // flowgrid.shell().layout(true, true);  // FIXME
    }


    @Override
    public void addInput(WidgetPort inputPort) {
       /* int pos = 0;
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
        controlLayout.addView(pos, view, colSpan, rowSpan); */

        inputPort.createControl(controlPanel);
    }


    Port attachPort(PortCommand portCommand) {
        Port result;

        String portType = portType(portCommand.peerJson());
        /*
        if (portType.equals("Sensor")) {
            result = new SensorPort(this, portCommand);
        } else if (portType.equals("Test")) {
            result = new TestPort(this, portCommand);
        } else if (portType.equals("Firmata")) {
            result = new FirmataPort(this, portCommand);
        } else { */
            result = new WidgetPort(this, portCommand);
        //}
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

 //       controlPanel.layout();

        Point minSize = controlPanel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        System.out.println("****attachAll: control panel min size: " + minSize);
        scrolledComposite.setMinSize(minSize);

        flowgrid.shell().layout(true, true);
    }

    @Override
    public Controller controller() {
        return controller;
    }

    public void detachAll() {
        for (PortCommand portCommand : operation.portCommands()) {
            portCommand.detach();
        }
    }


    @Override
    public SwtFlowgrid flowgrid() {
        return flowgrid;
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
                /*
                if (!checkOnly) {
                    widget.view().setBackgroundColor(0x088ff0000);         FIXME
                }
                */
            }
        }
        return missing.length() == 0;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public CustomOperation operation() {
        return operation;
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

    @Override
    public void removeWidget(ControlManager widget) {
        System.out.println("TBD: OperationEditor.removeWidget()");
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
                            flowgrid.error("TimerTick", e);
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

        flowgrid.display().asyncExec(new Runnable() {
            @Override
            public void run() {
                System.out.println("FIXME: updateMenu();");
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
        flowgrid.display().asyncExec(new Runnable() {
            @Override
            public void run() {
                System.out.println("FIXME: updateMenu();");
            }
        });
    }

    void timerTick() {
        if (running && controller.pendingCount() == 0 &&
                (operation.isTutorial() || !operation.asyncInput())) {
            inactiveTicks += 2;
            if (inactiveTicks == 10) {
                boolean allSent = true;
                for (Port port: ports()) {
                    /*
                    if (port instanceof  TestPort) {                           FIXME
                        TestPort test = (TestPort) port;
                        if (test.outputPending()) {
                            allSent = false;
                        }
                    }
                    */
                }
                if (!allSent) {
                    inactiveTicks = 0;
                } else if (operation.isTutorial()) {
                    /*                                                          FIXME
                    Log.d(TAG, "scheduling check tutorial success");
                    new Timer().schedule(new UiTimerTask(platform) {
                        @Override
                        public void runOnUiThread() {
                            checkTutorialSuccess();
                        }
                    }, 0);
                    */
                } else {
                    stop();
                }
            }
        } else {
            inactiveTicks = 0;
        }

        // This is at the end because it may cause pending activity, preventing tutorial end.
        for (Port port : ports()) {
            port.timerTick(2);
        }
        flowgrid.display().asyncExec(new Runnable() {
            @Override
            public void run() {
                operationCanvas.redraw();
            }
        });
    }

}
