package org.flowgrid.swt.data;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Module;
import org.flowgrid.model.Type;
import org.flowgrid.swt.Colors;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;

public class DataDialog {
    public static void show(SwtFlowgrid platform, String title, Type type, Module localModule, Object value, final Callback<Object> callback) {
        AlertDialog alert = new AlertDialog(platform.shell());
        alert.setTitle(title);
        final DataMetaControl dataWidget = new DataMetaControl.Builder(platform).setType(type)
                .setEditable(true)
                .setLocalModule(localModule)
                .build(alert.getContentContainer());
        if (value != null) {
            dataWidget.setValue(value);
        }
        dataWidget.getControl().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        alert.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
            @Override
            public boolean onClick(DialogInterface dialog, int which) {
                callback.run(dataWidget.value());
                return true;
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }
}
