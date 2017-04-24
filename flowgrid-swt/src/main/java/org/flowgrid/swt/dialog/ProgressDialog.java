package org.flowgrid.swt.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class ProgressDialog {

    private String message;
    private AlertDialog alert;
    private Label label;
    private boolean indeterminate;
    private boolean cancelable;
    private ProgressBar progressBar;

    public ProgressDialog(Shell parent) {
        alert = new AlertDialog(parent);
        label = new Label(alert.getContentContainer(), SWT.NONE);
    }

    public void setMessage(String message) {
        label.setText(message);
    }

    public void setTitle(String s) {
        alert.setTitle(s);
    }

    

    public void show() {
        progressBar = new ProgressBar(alert.getContentContainer(), indeterminate ? SWT.INDETERMINATE : SWT.NONE);
        alert.show();
    }

    public void dismiss() {
        alert.dismiss();
    }

    public void setIndeterminate(boolean b) {
        this.indeterminate = b;
    }
}
