package org.flowgrid.swt.operation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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

    OperationCanvas operationCanvas;
    boolean tutorialMode;
    public int counted;
    public int countedToRow;
    boolean running;

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

        ScrolledComposite scrolledComposite = new ScrolledComposite(flowgrid.shell(), SWT.V_SCROLL);
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

        flowgrid.shell().layout();  // FIXME
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
    }

    public void detachAll() {
        for (PortCommand portCommand : operation.portCommands()) {
            portCommand.detach();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public Controller controller() {
        return controller;
    }

    @Override
    public SwtFlowgrid flowgrid() {
        return flowgrid;
    }

    @Override
    public void start() {
        System.out.println("TBD: OperationEditor.start()");
    }

    @Override
    public CustomOperation operation() {
        return operation;
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

    @Override
    public void removeWidget(ControlManager widget) {
        System.out.println("TBD: OperationEditor.removeWidget()");
    }
}
