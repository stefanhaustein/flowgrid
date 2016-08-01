package org.flowgrid.swt.operation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.Controller;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Instance;
import org.flowgrid.swt.SwtFlowgrid;

public class OperationEditor {

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

        flowgrid.shell().layout();  // FIXME
    }


}
