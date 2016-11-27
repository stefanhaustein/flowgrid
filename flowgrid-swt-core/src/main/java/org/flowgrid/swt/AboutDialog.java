package org.flowgrid.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;

public class AboutDialog {
    public static void show(final SwtFlowgrid platform) {
        final AlertDialog alert = new AlertDialog(platform.shell);
        alert.setTitle("About FlowGrid");
        Composite main = alert.getContentContainer();
        Label content = new Label(main, SWT.WRAP);
        content.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, true));
        int padding = platform.dpToPx(24);
        final boolean[] restart = new boolean[1];
        //content.setPadding(padding, padding, padding, padding);
        content.setText(platform.documentation("Copyright"));
        content.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                if (--requiredClicks == 0 && !platform.settings().developerMode()) {
                    platform.settings().setDeveloperMode(true);
                    System.out.println("Show developer toast");
//                    Toast toast = Toast.makeText(platform, "Developer mode enabled", Toast.LENGTH_LONG);
                    restart[0] = true;
  //                  toast.show();
                }
            }

            int requiredClicks = 7;
            /*
            @Override
            public void onClick(View v) {
            }*/
        });
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public boolean onClick(DialogInterface dialog, int which) {
                if (restart[0]) {
                    platform.reboot(Settings.BootCommand.NONE, null);
                }
                return true;
            }
        });
        if (platform.settings().developerMode()) {
            alert.setNeutralButton("Developer off", new DialogInterface.OnClickListener() {
                @Override
                public boolean onClick(DialogInterface dialog, int which) {
                    platform.settings().setDeveloperMode(false);
                    platform.reboot(Settings.BootCommand.NONE, null);
                    return true;
                }
            });
        }
        alert.show();
    }
}
