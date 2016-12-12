package org.flowgrid.swt.operation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.TutorialData;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;
import org.kobjects.swt.Validator;

public class TutorialSettingsDialog {

    public static void show(final OperationEditor fragment) {
            final SwtFlowgrid context = fragment.flowgrid;
            final CustomOperation operation = fragment.operation;
            final TutorialData tutorialData = operation.tutorialData;
            AlertDialog alert = new AlertDialog(context.shell());
            alert.setTitle("Tutorial Settings");

            Composite layout = alert.getContentContainer();
            layout.setLayout(new GridLayout(2, true));

            Label orderLabel = new Label(layout, SWT.NONE);
            orderLabel.setText("Order");
            orderLabel.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));

            new Label(layout, SWT.NONE);

            final Text orderField = new Text(layout, SWT.SINGLE);
            Validator.add(orderField, Validator.TYPE_CLASS_NUMBER | Validator.TYPE_NUMBER_FLAG_DECIMAL);
            orderField.setText("" + tutorialData.order);
            orderField.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));

            new Label(layout, SWT.NONE);

            new Label(layout, SWT.SINGLE).setText("Rows editable from");
            new Label(layout, SWT.SINGLE).setText("to");

            final Text editableRowsStartField = new Text(layout, SWT.SINGLE);
            Validator.add(editableRowsStartField, Validator.TYPE_CLASS_NUMBER);
            editableRowsStartField.setText("" + tutorialData.editableStartRow);
            editableRowsStartField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

            final Text editableRowsEndField = new Text(layout, SWT.SINGLE);
            Validator.add(editableRowsEndField, Validator.TYPE_CLASS_NUMBER);
            editableRowsEndField.setText("" + tutorialData.editableEndRow);
            editableRowsEndField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

            new Label(layout, SWT.NONE).setText("Optimal cell count");
            new Label(layout, SWT.NONE).setText("Speed");

            final Text optimalCellCountField = new Text(layout, SWT.SINGLE);
            Validator.add(optimalCellCountField, Validator.TYPE_CLASS_NUMBER);
            optimalCellCountField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            optimalCellCountField.setText("" + tutorialData.optimalCellCount);

            final Text speedField = new Text(layout, SWT.SINGLE);
            Validator.add(speedField, Validator.TYPE_CLASS_NUMBER);
            speedField.setText("" + tutorialData.speed);
            speedField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
   /*
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    context, android.R.layout.simple_list_item_multiple_choice,
                    EditOperationFragment.TUTORIAL_MENU_OPTIONS);

            TabHost tabHost = Views.createTabHost(context);
            Views.addTab(tabHost, "Tutorial Settins", layout);

            alert.setView(tabHost);*/
            alert.setNegativeButton("Cancel", null);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    fragment.operationCanvas.beforeChange();
                    tutorialData.editableStartRow = Integer.parseInt(editableRowsStartField.getText().toString());
                    tutorialData.editableEndRow = Integer.parseInt(editableRowsEndField.getText().toString());
                    tutorialData.optimalCellCount = Integer.parseInt(optimalCellCountField.getText().toString());
                    tutorialData.order = Double.parseDouble(orderField.getText().toString());
                    tutorialData.speed = Integer.parseInt(speedField.getText().toString());
                    fragment.operationCanvas.afterChange();
                }
            });
            alert.show();
        }
}
