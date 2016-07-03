package org.flowgrid.android.classifier;

import org.flowgrid.android.MainActivity;
import org.flowgrid.model.Callback;
import org.flowgrid.android.Dialogs;
import org.flowgrid.android.Views;
import org.flowgrid.android.type.TypeFilter;
import org.flowgrid.android.type.TypeSpinner;
import org.flowgrid.model.VirtualOperation;
import org.flowgrid.model.VirtualOperation.Parameter;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;
import org.flowgrid.android.widget.ColumnLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.InputType;
import android.widget.EditText;

public class ParameterDialog {

  
  public static void show(MainActivity platform, final VirtualOperation operation, final boolean output, final int pos,
      final Callback<Void> callback) {
    AlertDialog.Builder alert = new AlertDialog.Builder(platform);
    boolean add = pos == -1;
    alert.setTitle((add ? "Add" : "Modify") + (output ? " output" : " input") + " parameter");
    ColumnLayout layout = new ColumnLayout(platform);
    
    Parameter param = add ? new Parameter("", PrimitiveType.NUMBER) : output ? operation.outputParameter(pos) : operation.inputParameter(pos);
    final EditText nameField = new EditText(platform);
    nameField.setText(param.name);
    layout.addView(Views.addLabel("Name", nameField), 0, 1);
    
    final TypeSpinner typeSpinner = new TypeSpinner(platform, operation.classifier.module(), Type.ANY, TypeFilter.ALL);
    typeSpinner.setType(param.type);
    layout.addView(Views.addLabel("Type", typeSpinner), 0, 1);
    
    final EditText positionField = new EditText(platform);
    positionField.setInputType(InputType.TYPE_CLASS_NUMBER);
    final int targetPosition = pos == -1 ? output ? operation.outputCount() : operation.inputCount() : pos;
    positionField.setText("" + (targetPosition + 1));
    layout.addView(Views.addLabel("Position", positionField), 0, 1);
    
    alert.setView(layout);
    
    alert.setPositiveButton("Ok", new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (pos != -1) {
          if (output) {
            operation.removeOutputParameter(pos);
          } else {
            operation.removeInputParameter(pos);
          }
        }
        String newName = nameField.getText().toString();
        Type newType = typeSpinner.type();
        int newPos = Integer.parseInt(positionField.getText().toString()) - 1;
        int max = output ? operation.outputCount() : operation.inputCount();
        if (newPos < 0) {
          newPos = 0;
        } else if (newPos > max) {
          newPos = max;
        }
        if (output) {
          operation.addOutputParameter(newPos, newName, newType);
        } else {
          operation.addInputParameter(newPos, newName, newType);
        }
        callback.run(null);
      }
    });
    
    alert.setNegativeButton("Cancel", null);
    Dialogs.showWithoutKeyboard(alert);
  }
  
}
