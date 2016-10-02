package org.flowgrid.swt.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ProgressDialog {

    Shell shell;
    Label label;

    public ProgressDialog(Shell parent) {
        shell = new Shell(parent);
        shell.setLayout(new FillLayout());
        label = new Label(shell, SWT.SINGLE);
    }

    public void setMessage(String message) {
        label.setText(message);
    }

    public void setTitle(String s) {
        shell.setText(s);
    }

    public void show() {
        shell.pack();
        shell.open();
    }

    public void dispose() {
        shell.dispose();
    }
}
