package org.flowgrid.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;

public class Dialogs {
  
  public static void confirm(Context context, String title, String message, final Runnable callback) {
    AlertDialog.Builder alert = new AlertDialog.Builder(context);
    alert.setTitle(title);
    alert.setMessage(message);
    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        callback.run();
      }
    });
    alert.setNegativeButton("Cancel", null);
    showWithoutKeyboard(alert);
  }
  
  public static void info(Context context, String title, String text) {
    AlertDialog.Builder alert = new AlertDialog.Builder(context);
    alert.setTitle(title);
    alert.setMessage(text);
    alert.setPositiveButton("Ok", null);
    alert.show();
  }

  public static void promptIdentifier(final Context context, final String title, final String label, String value, final Callback<String> callback) {
    prompt(context, title, label, -1, value, new Callback<String>() {
      @Override
      public void run(final String newValue) {
        if (Artifact.isIdentifier(newValue)) {
          callback.run(newValue);
        } else {
          confirm(context, "Invalid identifier",
              Artifact.IDENTIFIER_CONSTRAINT_MESSAGE, new Runnable() {
                @Override
                public void run() {
                  promptIdentifier(context, title, label, newValue, callback);
                }
              });
        }
      }
    });
  }

  public static void prompt(Context context, String title, String label, int constraints, String value, final Callback<String> callback) {
    AlertDialog.Builder alert = new AlertDialog.Builder(context);
    alert.setTitle(title);
    
    final EditText input = new EditText(context);
    if (value != null) {
      input.setText(value);
    }
    
    boolean multiLine = false;
    if (constraints == -1) {
      input.addTextChangedListener(new IdentifierValidator(input));
    } else if (constraints != 0) {
      input.setInputType(constraints);
      multiLine = (constraints & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
    }
    
    if (label != null) {
      setViewWithPadding(alert, Views.addLabel(label, input));
    } else {
      setViewWithPadding(alert, input);
    }

    if (multiLine) {
      input.setLines(5);
      input.setGravity(Gravity.START|Gravity.BOTTOM);
//      ((LinearLayout.LayoutParams) input.getLayoutParams()).gravity = Gravity.START | Gravity.BOTTOM;
    }
    
  /*  if (multiLine) {
      ((ColumnLayout.LayoutParams) input.getLayoutParams()).
    }
    */
    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        callback.run(input.getText().toString());
      }
    });

    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        callback.cancel();
      }
    });
    showWithoutKeyboard(alert);
  }
  
  public static AlertDialog showWithoutKeyboard(AlertDialog.Builder builder) {
    AlertDialog dialog = builder.create();
    dialog.show();
    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    return dialog;
  }

  public static void setViewWithPadding(AlertDialog.Builder alert, View view) {
    LinearLayout layout;
    if (view instanceof  LinearLayout) {
      layout = (LinearLayout) view;
    } else {
      layout = new LinearLayout(alert.getContext());
      layout.setOrientation(LinearLayout.VERTICAL);
      layout.addView(view);
    }
    layout.setPadding(50, 50, 50, 50);
    alert.setView(layout);
  }
  
}
