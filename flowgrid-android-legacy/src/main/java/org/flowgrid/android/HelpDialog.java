package org.flowgrid.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;

import org.flowgrid.model.Callback;

public class HelpDialog {

  static void edit(Context context, String title, String text, Callback<String> callback) {
    Dialogs.prompt(context, title, null, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, 
        text, callback);
  }
  
  /**
   * If no callback is provided, this will be read-only.
   */
  public static void show(final Context context, final String title, final String text, final Callback<String> callback) {
    if (text == null || text.trim().length() == 0) {
      edit(context, title, text, callback);
      return;
    }
    AlertDialog.Builder alert = new AlertDialog.Builder(context);
    alert.setTitle(title);
    alert.setMessage(text);
    // Views.setAlertText(alert, context, text);
    alert.setPositiveButton("Ok", null);
    if (callback != null) {
      alert.setNeutralButton("Edit", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          edit(context, title, text, callback);
        }
      });
    }
    alert.show();
  }
  
}
