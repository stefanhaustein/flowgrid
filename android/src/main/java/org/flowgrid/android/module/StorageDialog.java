package org.flowgrid.android.module;

import org.flowgrid.android.Dialogs;
import org.flowgrid.android.MainActivity;
import org.flowgrid.android.Views;
import org.flowgrid.model.Module;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.android.widget.ColumnLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;

public class StorageDialog {
  
  public static void show(final MainActivity platform, final Module module) {
    AlertDialog.Builder alert = new AlertDialog.Builder(platform);
    alert.setTitle("Google Drive Connection");
    ColumnLayout layout = new ColumnLayout(platform);
    alert.setView(layout);
    final HutnObject storageConnections = platform.settings().storageConnections();
    HutnObject moduleJsonTmp = storageConnections.getJsonObject(module.qualifiedName());
    boolean adding = moduleJsonTmp == null;
    final HutnObject moduleJson = adding ? new HutnObject() : moduleJsonTmp;
    
    final EditText usernameEditText = new EditText(platform);
    usernameEditText.setText(moduleJson.getString("username", ""));
    layout.addView(Views.addLabel("Account email address", usernameEditText), 0, 1);

    final EditText remotePathEditText = new EditText(platform);
    remotePathEditText.setText(moduleJson.getString("remotePath", "/FlowGrid" + module.path()));
    layout.addView(Views.addLabel("Remote path", remotePathEditText), 0, 1);

    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
      }
    });
    
    if (!adding) {
      alert.setNeutralButton("Disconnect", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          storageConnections.remove(module.qualifiedName());
          platform.settings().setStorageConnections(storageConnections);
          platform.reboot(null, null);
        }
      });
    }
    
    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        storageConnections.put(module.qualifiedName(), moduleJson);
        moduleJson.put("username", usernameEditText.getText().toString());
        moduleJson.put("remotePath", remotePathEditText.getText().toString());
        platform.settings().setStorageConnections(storageConnections);
        platform.reboot(null, null);
      }
    });
    Dialogs.showWithoutKeyboard(alert);
  }

}
