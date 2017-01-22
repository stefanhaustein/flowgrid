package org.flowgrid.swt.operation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.model.VirtualOperation;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;
import org.flowgrid.swt.type.TypeFilter;
import org.flowgrid.swt.type.TypeComponent;

public class ParameterDialog {

    AlertDialog alert;
    Spinner positionField;
    Text nameField;
    TypeComponent typeSpinner;
    Combo directionCombo;
    int existingIndex;
    int existingDirection;
    VirtualOperation operation;

    public ParameterDialog(final VirtualOperationDialog operationDialog, final VirtualOperation.Parameter parameter) {
        operation = operationDialog.operation;
        alert = new AlertDialog(operationDialog.flowgrid.shell());
        alert.setTitle(parameter == null ? "Add Parameter" : "Edit Parameter");

        alert.getContentContainer().setLayout(new GridLayout(2, false));

        new Label(alert.getContentContainer(), SWT.NONE).setText("Position");
        new Label(alert.getContentContainer(), SWT.NONE).setText("Name");

        positionField = new Spinner(alert.getContentContainer(), SWT.NONE);
        positionField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        nameField = new Text(alert.getContentContainer(), SWT.NONE);
        nameField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        new Label(alert.getContentContainer(), SWT.NONE).setText("Direction");
        new Label(alert.getContentContainer(), SWT.NONE).setText("Type");

        directionCombo = new Combo(alert.getContentContainer(), SWT.SIMPLE | SWT.READ_ONLY);
        directionCombo.add("In");
        directionCombo.add("Out");

        TypeFilter typeFilter = new TypeFilter.Builder().setLocalModule(operationDialog.operation.module).build();
        typeSpinner = new TypeComponent(alert.getContentContainer(), operationDialog.flowgrid, typeFilter);

        if (parameter != null) {
            typeSpinner.setType(parameter.type);
            nameField.setText(parameter.name);
            existingIndex = -1;
            for (int i = 0; i < operation.inputParameterCount(); i++) {
                if (operation.inputParameter(i) == parameter) {
                    existingIndex = i;
                    existingDirection = 0;
                    break;
                }
            }
            if (existingIndex == -1) {
                for (int i = 0; i < operation.outputParameterCount(); i++) {
                    if (operation.outputParameter(i) == parameter) {
                        existingIndex = i;
                        existingDirection = 1;
                    }
                }
            }
            positionField.setSelection(existingIndex);
            directionCombo.select(existingDirection);
        } else {
            directionCombo.select(0);
            positionField.setSelection(operation.inputCount());
        }

        if (parameter != null) {
            alert.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    removeParameter();
                    operation.save();
                    operationDialog.updateParameterList();
                }
            });
        }

        alert.setNegativeButton("Cancel", null);
        alert.setPositiveButton(parameter == null ? "Add" : "Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (parameter != null) {
                    removeParameter();
                }
                if (directionCombo.getSelectionIndex() != 1) {
                    operation.addInputParameter(
                            Math.min(positionField.getSelection(), operation.inputParameterCount()),
                            nameField.getText(),
                            typeSpinner.type());
                } else {
                    operation.addOutputParameter(
                            Math.min(positionField.getSelection(), operation.outputParameterCount()),
                            nameField.getText(),
                            typeSpinner.type());

                }
                operation.save();
                operationDialog.updateParameterList();
            }
        });
    }

    private void removeParameter() {
        if (existingDirection == 0) {
            operation.removeInputParameter(existingIndex);
        } else {
            operation.removeOutputParameter(existingIndex);
        }
    }


    public void show() {
        alert.show();
    }
}
