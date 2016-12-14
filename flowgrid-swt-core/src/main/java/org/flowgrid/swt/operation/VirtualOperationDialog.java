package org.flowgrid.swt.operation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.flowgrid.model.VirtualOperation;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.dialog.AlertDialog;

public class VirtualOperationDialog {

    final SwtFlowgrid flowgrid;
    final VirtualOperation operation;
    final AlertDialog alert;

    Composite parameterComposite;

    void addParameter(String prefix, int index, final VirtualOperation.Parameter parameter) {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                new ParameterDialog(VirtualOperationDialog.this, parameter).show();
            }
        };
        for (String text: new String[] {
                prefix, String.valueOf(index), parameter.name, parameter.type.name()}) {
            Label label = new Label(parameterComposite, SWT.NONE);
            label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
            label.setText(text);
            label.addMouseListener(mouseAdapter);
        }
    }

    void updateParameterList() {
        for (Control child : parameterComposite.getChildren()) {
            child.dispose();
        }

        for (int i = 0; i < operation.inputParameterCount(); i++) {
            addParameter("in", i, operation.inputParameter(i));
        }

        for (int i = 0; i < operation.outputParameterCount(); i++) {
            addParameter("out", i, operation.outputParameter(i));
        }

    }

    public VirtualOperationDialog(final SwtFlowgrid flowgrid, final VirtualOperation operation) {
        this.flowgrid = flowgrid;
        this.operation = operation;
        alert = new AlertDialog(flowgrid.shell());
        alert.setTitle("Edit Virtual Method");
        new Label(alert.getContentContainer(), SWT.NONE).setText("Operation name");
        new Label(alert.getContentContainer(), SWT.NONE).setText(operation.name());

        Button addParameterButton = new Button(alert.getContentContainer(), SWT.NONE);
        addParameterButton.setText("Add Parameter");
        addParameterButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new ParameterDialog(VirtualOperationDialog.this, null).show();
            }
        });

        ScrolledComposite scrolledComposite = new ScrolledComposite(alert.getContentContainer(), SWT.NONE);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        parameterComposite = new Composite(scrolledComposite, SWT.NONE);
        scrolledComposite.setContent(parameterComposite);
        parameterComposite.setLayout(new GridLayout(4, false));

        updateParameterList();


        alert.setPositiveButton("Ok", null);
    }

    public void show() {
        alert.show();

/*        Text text = new Text(alert.getContentContainer(), SWT.NONE);
        text.setText(operation.name());
        text.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 */



    }
}
