package org.flowgrid.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;

public class ResetDialog {

    private static final String[] OPTIONS = {
            "Restore deleted files only", "Overwrite all local changes", "Also delete added examples"};

    private static final Settings.BootCommand[] BOOT_COMMANDS = {
            Settings.BootCommand.RESTORE_MISSING_FILES,
            Settings.BootCommand.RESTORE_CHANGED_FILES,
            Settings.BootCommand.DELETE_AND_RESTORE,
    };

    final AlertDialog alert;
    public ResetDialog(final SwtFlowgrid flowgrid) {
        alert = new AlertDialog(flowgrid.shell());
        alert.setTitle("Restore");
        final int[] option = new int[1];

        Composite main = alert.getContentContainer();
        final Combo options = new Combo(main, SWT.DEFAULT);
        options.setItems(OPTIONS);
        options.select(0);
        alert.setNegativeButton("Cancel", null);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
           flowgrid.restart(BOOT_COMMANDS[options.getSelectionIndex()], null);
            }
        });
    }

    public void show() {
        alert.show();
    }
}
