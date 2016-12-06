package org.flowgrid.swt.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog builder following the Android dialog builder pattern.
 */
public class AlertDialog implements DialogInterface {

    Shell parent;
    Shell shell;
    Composite buttonRow;
    Composite contentContainer;


    public static void center(Shell dialog) {
        Shell parent = (Shell) dialog.getParent();
        Rectangle parentBounds = parent.getBounds();
        Rectangle childBounds = dialog.getBounds();

        dialog.setLocation(parentBounds.x + (parentBounds.width - childBounds.width) / 2,
                parentBounds.y + (parentBounds.height - childBounds.height) / 2);
    }

    public AlertDialog(Shell parent) {
        shell = new Shell(parent);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        shell.setLayout(gridLayout);
        contentContainer = new Composite(shell, 0);
        contentContainer.setLayout(new GridLayout());
        contentContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        buttonRow = new Composite(shell, 0);
        buttonRow.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));
        buttonRow.setLayout(new RowLayout());
    }

    public void setOnCancelListener(OnCancelListener onCancelListener) {
        System.out.println("FIXME: AlertDialog.Builder.setOnCancelListener");                 // FIXME
    }

    void setButton(final int code, String label, final OnClickListener onClickListener) {
        Button button = new Button(buttonRow, SWT.PUSH);
        button.setText(label);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (onClickListener == null || onClickListener.onClick(AlertDialog.this, code)) {
                    dismiss();
                }
            }
        });
    }

    public void setNegativeButton(String label, OnClickListener onClickListener) {
        setButton(BITTON_NEGATIVE, label, onClickListener);
    }

    public void setNeutralButton(String label, OnClickListener onClickListener) {
        setButton(BUTTON_NEUTRAL, label, onClickListener);
    }

    public void setPositiveButton(String label, OnClickListener onClickListener) {
        setButton(BUTTON_POSITIVE, label, onClickListener);
    }


    public void setTitle(String title) {
        shell.setText(title);
    }

    public void setMessage(String s) {
        Label label = new Label(contentContainer, 0);
        label.setText(s);
    }

    public Composite getContentContainer() {
        return contentContainer;
    }

    public void show() {
        shell.pack();
        center(shell);
        shell.open();
    }

    public void dismiss() {
        shell.dispose();
    }
}
