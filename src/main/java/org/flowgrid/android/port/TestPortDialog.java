package org.flowgrid.android.port;

import org.flowgrid.model.Callback;
import org.flowgrid.android.Dialogs;
import org.flowgrid.android.Views;
import org.flowgrid.android.type.PrimitiveTypeSpinner;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.android.widget.ColumnLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Spinner;

public class TestPortDialog {
  public static void show(final Context context, final PortCommand portCommand, boolean create, final Callback<Void> callback) {
	AlertDialog.Builder alert = new AlertDialog.Builder(context);
    final boolean output = portCommand.inputCount() > 0;
  
    final HutnObject peerJson = portCommand.peerJson();
      
    String title = output ? "Expectation" : "Test Input";
      
    //alert.setTitle((create ? "Add " : "Edit ") + title);
      
    final ColumnLayout main = new ColumnLayout(context);
    main.setColumnCount(2, 2);
   
    final PrimitiveTypeSpinner inputTypeSpinner = new PrimitiveTypeSpinner(context);
    inputTypeSpinner.setType(!(portCommand.dataType() instanceof PrimitiveType) ? PrimitiveType.NUMBER : (PrimitiveType) portCommand.dataType());
    main.addView(Views.addLabel("Type", inputTypeSpinner), 0, 1);
  
    final EditText testData = new EditText(context);
    testData.setInputType(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
    main.addView(Views.addLabel("Test data", testData), 0, 1);
    testData.setText(peerJson.getString("testData", ""));

    final Spinner iconSpinner = Views.createSpinner(context,
        portCommand.output ? Sprite.NAMES : TestPort.OUTPUT_ICONS,
        portCommand.peerJson().getString("icon", ""));
    main.addView(Views.addLabel("Icon", iconSpinner), 0, 1);
    
    alert.setTitle(title);  
    alert.setView(main);
    
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, final int whichButton) {
        portCommand.setDataType(inputTypeSpinner.type());
        peerJson.put("icon", (String) iconSpinner.getSelectedItem());
        peerJson.put("testData", testData.getText().toString());
        callback.run(null);
      }
    };
      
    alert.setPositiveButton("Ok", listener);
    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        callback.cancel();
      }
    });

    Dialogs.showWithoutKeyboard(alert);
  }
}
