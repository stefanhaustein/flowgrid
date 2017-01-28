package org.flowgrid.swt.operation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.model.Callback;
import org.flowgrid.model.PortFactory;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;

public class GenericPortDialog {

    AlertDialog alert;

    public GenericPortDialog(SwtFlowgrid flowgrid, PortFactory factory, final PortCommand portCommand, boolean creating, final Callback<Void> callback) {
        alert = new AlertDialog(flowgrid.shell());
        alert.setTitle((creating ? "Create " : "Edit ") + factory.getPortType());

        final PortFactory.Option[] options = factory.getOptions();
        final Control[] optionControl = new Control[options.length];

        alert.getContentContainer().setLayout(new GridLayout(2, false));

        if (options != null) {
            for (int i = 0; i < options.length; i++) {
                PortFactory.Option option = options[i];
                new Label(alert.getContentContainer(), SWT.NONE).setText(option.name);
                if (option.values != null && option.values.length > 0) {
                    Combo combo = new Combo(alert.getContentContainer(), SWT.DROP_DOWN | SWT.READ_ONLY);
                    for (Object value : option.values) {
                        combo.add(String.valueOf(value));
                    }
                    optionControl[i] = combo;
                } else {
                    Text text = new Text(alert.getContentContainer(), SWT.NONE);
                    optionControl[i] = text;
                }
                optionControl[i].setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            }
        }

        alert.setNegativeButton("Cancel", null);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < options.length; i++) {
                    Control ctrl = optionControl[i];
                    if (ctrl instanceof Combo) {
                        portCommand.peerJson().put(options[i].name, ((Combo) ctrl).getText());
                    } else {
                        portCommand.peerJson().put(options[i].name, ((Text) ctrl).getText());
                    }
                }
                callback.run(null);
            }
        });
    }

    public void show() {
        alert.show();
    }

}
