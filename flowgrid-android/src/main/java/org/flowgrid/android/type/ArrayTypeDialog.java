package org.flowgrid.android.type;

import org.flowgrid.android.MainActivity;
import org.flowgrid.model.Callback;
import org.flowgrid.android.Dialogs;
import org.flowgrid.android.Views;
import org.flowgrid.model.ArrayType;
import org.flowgrid.model.Container;
import org.flowgrid.model.Type;
import org.flowgrid.android.widget.ColumnLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class ArrayTypeDialog {
  
  public static void show(MainActivity platform, Container localModule, Type assignableTo, TypeFilter filter,
      final Callback<Type> callback) {
    Context context = (Context) localModule.model().platform;
    final int fixedLength = (assignableTo instanceof ArrayType) ? 
        ((ArrayType) assignableTo).length : -1;
    final Type elementType = (assignableTo instanceof ArrayType) ? ((ArrayType) assignableTo).elementType : 
      Type.ANY;
    AlertDialog.Builder alert = new AlertDialog.Builder(context);
    alert.setTitle("Array Type");
    ColumnLayout layout = new ColumnLayout(context);
    final CheckBox fixedLengthCheckBox = new CheckBox(context);
    fixedLengthCheckBox.setText("Fixed length");
    layout.addView(fixedLengthCheckBox, 0, 1);
    final EditText lengthEditText = new EditText(context);
    lengthEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
    lengthEditText.setEnabled(false);
    if (fixedLength != -1) {
      fixedLengthCheckBox.setChecked(true);
      fixedLengthCheckBox.setEnabled(false);
      lengthEditText.setText("" + fixedLength);
    }
    fixedLengthCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        lengthEditText.setEnabled(isChecked);
      }
    });
    
    layout.addView(Views.addLabel("Array length", lengthEditText), 0, 1);
    final TypeSpinner elementTypeSpinner = new TypeSpinner(platform, localModule, elementType, filter);
    layout.addView(Views.addLabel("Element type", elementTypeSpinner), 0, 1);
    alert.setView(layout);
    alert.setNegativeButton("Cancel", null);
    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        int len = -1;
        if (lengthEditText.isEnabled()) {
          String value = lengthEditText.getText().toString();
          if (!value.isEmpty()) {
            len = Integer.parseInt(value.trim());
          }
        }
        ArrayType arrayType = new ArrayType(elementTypeSpinner.type(), len);
        callback.run(arrayType);
      }
    });
    Dialogs.showWithoutKeyboard(alert);
  }
}
