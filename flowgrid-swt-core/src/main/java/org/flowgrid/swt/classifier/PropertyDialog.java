package org.flowgrid.swt.classifier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.flowgrid.model.Property;
import org.flowgrid.model.Type;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.data.DataWidget;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;
import org.flowgrid.swt.type.TypeFilter;
import org.flowgrid.swt.type.TypeSpinner;

import java.util.Objects;

public class PropertyDialog {

    public static void show(SwtFlowgrid flowgrid, final Property property) {
        AlertDialog alert = new AlertDialog(flowgrid.shell());

        Composite content = alert.getContentContainer();
        GridLayout contentLayout = new GridLayout(2, false);
        contentLayout.marginWidth = contentLayout.marginHeight = 0;
        content.setLayout(contentLayout);
        new Label(content, SWT.SINGLE).setText("Name");
        final Label text = new Label(content, SWT.SINGLE);
        text.setText(property.name());

        new Label(content, SWT.SINGLE).setText("Type");
        final TypeSpinner typeSpinner = new TypeSpinner(content, flowgrid, property.owner(), Type.ANY, property.classifier == null ? TypeFilter.INSTANTIABLE : TypeFilter.ALL);
        typeSpinner.setType(property.type());

        // FIXME: Adjust value widget type when type changes.

        new Label(content, SWT.SINGLE).setText(property.classifier != null ? "Initial value" : "Constant value");
        final DataWidget valueWidget = new DataWidget(property.type());
        valueWidget.createControl(content);

        alert.setNegativeButton("Cancel", null);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!Objects.equals(valueWidget.value(), property.value())) {
                    property.setValue(valueWidget.value());
                }
                if (!Objects.equals(typeSpinner.type(), property.type())) {
                    property.setType(typeSpinner.type());
                }

/*                if (!Objects.equals(text.getText(), property.name())) {
                    System.out.println("TBD: Rename");
                }*/
                property.save();
            }
        });


        alert.show();
    }


}
