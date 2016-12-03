package org.flowgrid.swt;

import org.eclipse.swt.widgets.Shell;
import org.flowgrid.model.Callback;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;

public class HelpDialog {


    static void edit(Shell shell, String title, String text, Callback<String> callback) {
        Dialogs.prompt(shell, title, null, 0, // InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE,
                text, callback);
    }

    /**
     * If no callback is provided, this will be read-only.
     */
    public static void show(final Shell shell, final String title, final String text,
                            final Callback<String> callback) {
        if (text == null || text.trim().length() == 0) {
            edit(shell, title, text, callback);
            return;
        }
        AlertDialog alert = new AlertDialog(shell);
        alert.setTitle(title);
        alert.setMessage(text);
        // Views.setAlertText(alert, context, text);
        alert.setPositiveButton("Ok", null);
        if (callback != null) {
            alert.setNeutralButton("Edit", new DialogInterface.OnClickListener() {
                @Override
                public boolean onClick(DialogInterface dialog, int which) {
                    edit(shell, title, text, callback);
                    return true;
                }
            });
        }
        alert.show();
    }
}
