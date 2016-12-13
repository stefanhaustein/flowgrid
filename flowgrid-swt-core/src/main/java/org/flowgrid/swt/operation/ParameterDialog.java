package org.flowgrid.swt.operation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.model.Type;
import org.flowgrid.model.VirtualOperation;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;
import org.flowgrid.swt.type.TypeFilter;
import org.flowgrid.swt.type.TypeSpinner;

public class ParameterDialog {

    AlertDialog alert;
    Spinner positionField;
    Text nameField;
    TypeSpinner typeSpinner;
    Combo directionCombo;

    public ParameterDialog(SwtFlowgrid flowgrid, VirtualOperation operation, VirtualOperation.Parameter parameter) {
        alert = new AlertDialog(flowgrid.shell());
        alert.setTitle(parameter == null ? "Add Parameter" : "Edit Parameter");

        alert.getContentContainer().setLayout(new GridLayout(2, true));

        new Label(alert.getContentContainer(), SWT.NONE).setText("Position");
        new Label(alert.getContentContainer(), SWT.NONE).setText("Name");

        positionField = new Spinner(alert.getContentContainer(), SWT.NONE);
        positionField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        nameField = new Text(alert.getContentContainer(), SWT.NONE);
        nameField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        new Label(alert.getContentContainer(), SWT.NONE).setText("Direction");
        new Label(alert.getContentContainer(), SWT.NONE).setText("Type");

        directionCombo = new Combo(alert.getContentContainer(), SWT.SIMPLE | SWT.READ_ONLY);
        directionCombo.add("In");
        directionCombo.add("Out");

        typeSpinner = new TypeSpinner(alert.getContentContainer(), flowgrid, operation.module, Type.ANY, TypeFilter.ALL);

        alert.setNegativeButton("Cancel", null);
        alert.setPositiveButton(parameter == null ? "Add" : "Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

    }

    public void show() {
        alert.show();
    }
}
