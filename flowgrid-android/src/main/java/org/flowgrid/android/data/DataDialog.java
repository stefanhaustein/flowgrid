package org.flowgrid.android.data;

import org.flowgrid.model.Callback;
import org.flowgrid.android.Dialogs;
import org.flowgrid.android.MainActivity;
import org.flowgrid.model.Member;
import org.flowgrid.model.Type;

import android.app.AlertDialog;
import android.content.DialogInterface;

public class DataDialog {
  
  public static void show(MainActivity platform, Member owner, Type forceType, final Callback<Object> callback, String... path) {
    AlertDialog.Builder alert = new AlertDialog.Builder(platform);
    final DataWidget dataWidget = new DataWidget(platform, owner, forceType, "", true, path);
    Dialogs.setViewWithPadding(alert, dataWidget.view());
    alert.setTitle(path[path.length - 2]);
    alert.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        callback.run(dataWidget.value());
      }
    });
    alert.setNegativeButton("Cancel", null);
    Dialogs.showWithoutKeyboard(alert);

  }
}
