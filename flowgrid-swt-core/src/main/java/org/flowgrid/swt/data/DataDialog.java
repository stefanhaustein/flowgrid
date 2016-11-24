package org.flowgrid.swt.data;

import org.flowgrid.model.Callback;
import org.flowgrid.model.Module;
import org.flowgrid.model.Type;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;

public class DataDialog {
    public static void show(SwtFlowgrid platform, String title, Type type, Module localModule, Object value, final Callback<Object> callback) {
        AlertDialog alert = new AlertDialog(platform.shell());
        alert.setTitle(title);
        final DataWidget dataWidget = new DataWidget(platform, type);
        dataWidget.setLocalModule(localModule);
        dataWidget.setEditable(true);
        dataWidget.createControl(alert.getContentContainer());
        if (value != null) {
            dataWidget.setValue(value);
        }
        alert.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.run(dataWidget.value());
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }
}
