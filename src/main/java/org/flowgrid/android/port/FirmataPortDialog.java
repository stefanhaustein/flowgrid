package org.flowgrid.android.port;

import org.flowgrid.model.Callback;
import org.flowgrid.android.Views;
import org.flowgrid.model.JSONEnums;
import org.flowgrid.model.Model;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.android.widget.ColumnLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class FirmataPortDialog {
  private static final String TAG = "FirmataPortDialog";

  
  public static void show(final Context context, final Model model, final PortCommand portCommand, boolean create, final Callback<Void> callback) {
    AlertDialog.Builder alert = new AlertDialog.Builder(context);
    final HutnObject peerJson = portCommand.peerJson();
    final FirmataPort.Mode mode = JSONEnums.optEnum(peerJson, "mode", FirmataPort.Mode.DIGITAL);
    alert.setTitle((create ? "Create" : "Edit") + " Firmata " + (portCommand.input ? "Input" : "Output") + 
        " Port (" + mode.toString() + ")");
    ColumnLayout layout = new ColumnLayout(context);

    final EditText editPin = new EditText(context);
    editPin.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
    editPin.setText("" + peerJson.getInt("pin", 0));
    layout.addView(Views.addLabel("Pin", editPin), 0, 1);
    
    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
      }
    });
    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        try {
          peerJson.put("pin", Integer.parseInt(editPin.getText().toString()));
        } catch (Exception e) {
          Log.e(TAG, "Numerical input error?", e);
        }
        callback.run(null);
      }
    });
    alert.setView(layout);
    alert.show();
  }
}
