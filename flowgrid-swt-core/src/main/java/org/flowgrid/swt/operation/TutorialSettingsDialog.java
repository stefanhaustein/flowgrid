package org.flowgrid.swt.operation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.TutorialData;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;

public class TutorialSettingsDialog {

    public static void show(final OperationEditor fragment) {
            final SwtFlowgrid context = fragment.flowgrid;
            final CustomOperation operation = fragment.operation;
            final TutorialData tutorialData = operation.tutorialData;
            AlertDialog alert = new AlertDialog(context.shell());

            Composite layout = alert.getContentContainer();
            new Label(layout, SWT.SINGLE).setText("Order");
//            layout.setColumnCount(2, 2);
            final Text orderField = new Text(layout, SWT.SINGLE);
            //orderField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            orderField.setText("" + tutorialData.order);
            //layout.addView(Views.addLabel("Order", orderField), 0, 1);

            new Label(layout, SWT.SINGLE).setText("Rows editable from");
            final Text editableRowsStartField = new Text(layout, SWT.SINGLE);
            //editableRowsStartField.setInputType(InputType.TYPE_CLASS_NUMBER);
            editableRowsStartField.setText("" + tutorialData.editableStartRow);

            new Label(layout, SWT.SINGLE).setText("to");
            final Text editableRowsEndField = new Text(layout, SWT.SINGLE);
            //editableRowsEndField.setInputType(InputType.TYPE_CLASS_NUMBER);
            editableRowsEndField.setText("" + tutorialData.editableEndRow);

            new Label(layout, SWT.SINGLE).setText("Optimal cell count");
            final Text optimalCellCountField = new Text(layout, SWT.SINGLE);
//            optimalCellCountField.setInputType(InputType.TYPE_CLASS_NUMBER);
            optimalCellCountField.setText("" + tutorialData.optimalCellCount);

            new Label(layout, SWT.SINGLE).setText("Speed");
            final Text speedField = new Text(layout, SWT.SINGLE);
 //           speedField.setInputType(InputType.TYPE_CLASS_NUMBER);
            speedField.setText("" + tutorialData.speed);
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
                public boolean onClick(DialogInterface dialog, int which) {
                    fragment.operationCanvas.beforeChange();
                    tutorialData.editableStartRow = Integer.parseInt(editableRowsStartField.getText().toString());
                    tutorialData.editableEndRow = Integer.parseInt(editableRowsEndField.getText().toString());
                    tutorialData.optimalCellCount = Integer.parseInt(optimalCellCountField.getText().toString());
                    tutorialData.order = Double.parseDouble(orderField.getText().toString());
                    tutorialData.speed = Integer.parseInt(speedField.getText().toString());
                    fragment.operationCanvas.afterChange();
                    return true;
                }
            });
            alert.show();
        }
}
