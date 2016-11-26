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

    public static void show(final SwtFlowgrid platform, final String path) {
        AlertDialog alert = new AlertDialog(platform.shell());
        alert.setTitle("Restore " + path);
        final int[] option = new int[1];

        Composite main = alert.getContentContainer();
        final Combo options = new Combo(main, SWT.DEFAULT);
        options.setItems(OPTIONS);
        alert.setNegativeButton("Cancel", null);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public boolean onClick(DialogInterface dialog, int which) {
                platform.reboot(BOOT_COMMANDS[options.getSelectionIndex()], path);
                return true;
            }
        });
        alert.show();
    }
}
