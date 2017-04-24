package org.flowgrid.swt.data;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Module;
import org.flowgrid.model.Type;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;

public class DataDialog {

    final AlertDialog alert;
    final DataComponent.Builder controlBuilder;
    final Callback<Object> callback;
    Object value;

    public DataDialog(SwtFlowgrid platform, String title, final Callback<Object> callback) {
        alert = new AlertDialog(platform.shell());
        alert.setTitle(title);
        controlBuilder = new DataComponent.Builder(platform);
        this.callback = callback;
    }

    DataDialog setType(Type type) {
        controlBuilder.setType(type);
        return this;
    }

    DataDialog setValue(Object value) {
        this.value = value;
        return this;
    }

    public DataDialog setLocalModule(Module localModule) {
        controlBuilder.setLocalModule(localModule);
        return this;
    }

    public void show() {
        final DataComponent dataControl = controlBuilder.build(alert.getContentContainer());
        if (value != null) {
            dataControl.setValue(value);
        }
        dataControl.getControl().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        alert.setNegativeButton("Cancel", null);
        alert.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.run(dataControl.value());
            }
        });
        alert.show();
    }
}
