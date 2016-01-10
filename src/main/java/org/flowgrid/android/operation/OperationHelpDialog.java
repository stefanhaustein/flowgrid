package org.flowgrid.android.operation;

import org.flowgrid.android.Dialogs;
import org.flowgrid.android.Views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;

public class OperationHelpDialog {
  public static void show(final EditOperationFragment fragment) {
    Context context = fragment.platform();
    AlertDialog.Builder alert = new AlertDialog.Builder(context);
    TextView editorHelp = new TextView(context);
    editorHelp.setText(fragment.platform().documentation("Operation Editor"));

    String documentationText = fragment.operation.documentation();
    final EditText documentation = new EditText(context);
    documentation.setText(documentationText);
    documentation.setInputType(
        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
    documentation.setGravity(Gravity.TOP);
    documentation.setMinLines(5);
    
    if (fragment.tutorialMode && 
        (fragment.landscapeMode || !fragment.operation.hasDocumentation())) {
      int px = Views.px(context, 4);
      editorHelp.setPadding(px, 4*px, px, px);
      alert.setView(editorHelp);
      alert.setTitle("Editor Help");
    } else {
      TabHost tabHost = Views.createTabHost(context);
    
      if (fragment.operation.isTutorial()) {
        int index = 1;
        for(String paragraph : documentationText.split("\n\n")) {
          TextView textView = new TextView(context);
          textView.setText(paragraph);
        //  textView.setTextAppearance(context, android.R.style.TextAppearance_Medium);
          TextView parView = textView;
          Views.addTab(tabHost, "Hint " + (index++), parView);
        }
      }
    
      Views.addTab(tabHost, "Editor Help", editorHelp);
    
      if (!fragment.tutorialMode) {
        Views.addTab(tabHost, "Operation Documentation", documentation);
        if (documentationText != null && !documentationText.trim().isEmpty()) {
          tabHost.setCurrentTab(1);
        }
        alert.setNegativeButton("Cancel", null);
      }
      alert.setView(tabHost);
    }
    
    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        fragment.beforeChange();
        fragment.operation.setDocumentation(documentation.getText().toString());
        fragment.tutorialHelpView.setText(fragment.operation.documentation());
        fragment.afterChange();
      }
    });
    
    Dialogs.showWithoutKeyboard(alert);
  }
}
