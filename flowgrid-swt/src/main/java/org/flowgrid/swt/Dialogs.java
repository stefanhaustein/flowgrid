package org.flowgrid.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;
import org.kobjects.swt.Validator;

public class Dialogs {

    public static void confirm(Shell context, String title, String message, final Runnable callback) {
        AlertDialog alert = new AlertDialog(context);
        alert.setTitle(title);
        new Label(alert.getContentContainer(), SWT.NONE).setText(message);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.run();
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }

    public static void info(Shell context, String title, String text) {
        AlertDialog alert = new AlertDialog(context);
        alert.setTitle(title);
        new Label(alert.getContentContainer(), SWT.NONE).setText(text);
        alert.setPositiveButton("Ok", null);
        alert.show();
    }

    public static void promptIdentifier(final Shell context, final String title, final String label, String value, final Callback<String> callback) {
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

    public static void prompt(Shell context, String title, String label, int inputType, String value, final Callback<String> callback) {
        AlertDialog alert = new AlertDialog(context);
        alert.setTitle(title);

        if (label != null) {
            new Label(alert.getContentContainer(), SWT.NONE).setText(label);
        }

        final Text input = new Text(alert.getContentContainer(), SWT.NONE);
        input.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        if (value != null) {
            input.setText(value);
        }

        Validator.add(input, inputType);

        System.out.println("FIXME: Dialogs.prompt(): constraints, multiline");
        /*
        boolean multiLine = false;
        if (constraints == -1) {
            input.addTextChangedListener(new IdentifierValidator(input));
        } else if (constraints != 0) {
            input.setInputType(constraints);
            multiLine = (constraints & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
        }

        if (multiLine) {
            input.setLines(5);
            input.setGravity(Gravity.START|Gravity.BOTTOM);
//      ((LinearLayout.LayoutParams) input.getLayoutParams()).gravity = Gravity.START | Gravity.BOTTOM;
        }
        */

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
        alert.show();
    }

    public static void promptMultiline(Shell context, String title, String label, String value, final Callback<String> callback) {
        AlertDialog alert = new AlertDialog(context);
        alert.setTitle(title);

        if (label != null) {
            new Label(alert.getContentContainer(), SWT.NONE).setText(label);
        }

        final Text input = new Text(alert.getContentContainer(), SWT.MULTI);
        input.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        if (value != null) {
            input.setText(value);
        }

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
        alert.show();
    }

}
