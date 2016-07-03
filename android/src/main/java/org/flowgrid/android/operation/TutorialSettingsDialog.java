package org.flowgrid.android.operation;

import org.flowgrid.android.Dialogs;
import org.flowgrid.android.Views;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.TutorialData;
import org.flowgrid.android.widget.ColumnLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TabHost;


public class TutorialSettingsDialog {

  public static void show(final EditOperationFragment fragment) {
    final Context context = fragment.platform();
    final CustomOperation operation = fragment.operation;
    final TutorialData tutorialData = operation.tutorialData;
    AlertDialog.Builder alert = new AlertDialog.Builder(context);
    
    ColumnLayout layout = new ColumnLayout(context);
    layout.setColumnCount(2, 2);
    final EditText orderField = new EditText(context);
    orderField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    orderField.setText("" + tutorialData.order);
    layout.addView(Views.addLabel("Order", orderField), 0, 1);

    final EditText editableRowsStartField = new EditText(context);
    editableRowsStartField.setInputType(InputType.TYPE_CLASS_NUMBER);
    editableRowsStartField.setText("" + tutorialData.editableStartRow);
    layout.addView(Views.addLabel("Rows editable from", editableRowsStartField), 1, 1);
      
    final EditText editableRowsEndField = new EditText(context);
    editableRowsEndField.setInputType(InputType.TYPE_CLASS_NUMBER);
    editableRowsEndField.setText("" + tutorialData.editableEndRow);
    layout.addView(Views.addLabel("to", editableRowsEndField), 1, 1);
      
    final EditText optimalCellCountField = new EditText(context);
    optimalCellCountField.setInputType(InputType.TYPE_CLASS_NUMBER);
    optimalCellCountField.setText("" + tutorialData.optimalCellCount);
    layout.addView(Views.addLabel("Optimal cell count", optimalCellCountField), 1, 1);

    final EditText speedField = new EditText(context);
    speedField.setInputType(InputType.TYPE_CLASS_NUMBER);
    speedField.setText("" + tutorialData.speed);
    layout.addView(Views.addLabel("Speed", speedField), 1, 1);

    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
        context, android.R.layout.simple_list_item_multiple_choice,
        EditOperationFragment.TUTORIAL_MENU_OPTIONS);
    
    TabHost tabHost = Views.createTabHost(context);
    Views.addTab(tabHost, "Tutorial Settins", layout);

    alert.setView(tabHost);
    alert.setNegativeButton("Cancel", null);
    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        fragment.beforeChange();
        tutorialData.editableStartRow = Integer.parseInt(editableRowsStartField.getText().toString());
        tutorialData.editableEndRow = Integer.parseInt(editableRowsEndField.getText().toString());
        tutorialData.optimalCellCount = Integer.parseInt(optimalCellCountField.getText().toString());
        tutorialData.order = Double.parseDouble(orderField.getText().toString());
        tutorialData.speed = Integer.parseInt(speedField.getText().toString());
        fragment.afterChange();
      }
    });
    Dialogs.showWithoutKeyboard(alert);
  }
}
