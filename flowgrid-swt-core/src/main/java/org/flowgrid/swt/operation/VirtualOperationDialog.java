package org.flowgrid.swt.operation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.model.VirtualOperation;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;

public class VirtualOperationDialog {

    final SwtFlowgrid flowgrid;
    final VirtualOperation operation;
    final AlertDialog alert;

    Composite parameterComposite;

    void addParameter(String prefix, VirtualOperation.Parameter parameter) {
        new Label(parameterComposite, SWT.NONE).setText(prefix);
        new Label(parameterComposite, SWT.NONE).setText(parameter.name);
        new Button(parameterComposite, SWT.PUSH).setText("Edit");
        new Button(parameterComposite, SWT.PUSH).setText("+");
        new Button(parameterComposite, SWT.PUSH).setText("-");
    }

    public VirtualOperationDialog(SwtFlowgrid flowgrid, VirtualOperation operation) {
        this.flowgrid = flowgrid;
        this.operation = operation;
        alert = new AlertDialog(flowgrid.shell());
        alert.setTitle("Edit Virtual Method");
        new Label(alert.getContentContainer(), SWT.NONE).setText(operation.name());
        new Label(alert.getContentContainer(), SWT.NONE).setText("Parameter");

        ScrolledComposite scrolledComposite = new ScrolledComposite(alert.getContentContainer(), SWT.NONE);
        parameterComposite = new Composite(scrolledComposite, SWT.NONE);
        scrolledComposite.setContent(parameterComposite);
        parameterComposite.setLayout(new GridLayout(5, false));

        for (int i = 0; i < operation.inputParameterCount(); i++) {
            addParameter("in", operation.inputParameter(i));
        }

        for (int i = 0; i < operation.inputParameterCount(); i++) {
            addParameter("out", operation.inputParameter(i));
        }

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public boolean onClick(DialogInterface dialog, int which) {
                return true;
            }
        });
    }

    public void show() {
        alert.show();

/*        Text text = new Text(alert.getContentContainer(), SWT.NONE);
        text.setText(operation.name());
        text.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 */



    }
}
