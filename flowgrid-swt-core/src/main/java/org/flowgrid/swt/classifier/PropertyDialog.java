package org.flowgrid.swt.classifier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.flowgrid.model.Container;
import org.flowgrid.model.Property;
import org.flowgrid.model.Type;
import org.flowgrid.swt.Dialogs;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.data.DataComponent;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;
import org.flowgrid.swt.type.TypeFilter;
import org.flowgrid.swt.type.TypeComponent;

import java.util.Objects;

public class PropertyDialog {

    final SwtFlowgrid flowgrid;
    final Property property;
    final AlertDialog alert;
    final boolean virtual;
    final TypeComponent typeSpinner;
    DataComponent valueWidget;
    Combo visibilityCombo;

    public PropertyDialog(final SwtFlowgrid flowgrid, final Property property) {
        alert = new AlertDialog(flowgrid.shell());
        alert.setTitle("Edit Property");
        this.flowgrid = flowgrid;
        this.property = property;
        virtual = property.classifier != null && property.classifier.isInterface();

        Composite content = alert.getContentContainer();
        GridLayout contentLayout = new GridLayout(2, false);
        contentLayout.marginWidth = contentLayout.marginHeight = 0;
        content.setLayout(contentLayout);
        new Label(content, SWT.SINGLE).setText("Name");
        new Label(content, SWT.SINGLE).setText(property.name());


        if (!virtual) {
            new Label(content, SWT.NONE).setText("Visibility");
            visibilityCombo = new Combo(content, SWT.READ_ONLY | SWT.POP_UP);
            visibilityCombo.add("Private");
            visibilityCombo.add("Public");
            visibilityCombo.select(property.isPublic() ? 1 : 0);
        }

        new Label(content, SWT.SINGLE).setText("Type");
        TypeFilter typeFilter = new TypeFilter.Builder().setLocalModule(property.owner()).setCategory(property.classifier == null ? TypeFilter.Category.INSTANTIABLE : TypeFilter.Category.ALL).build();
        typeSpinner = new TypeComponent(content, flowgrid, typeFilter);
        typeSpinner.setType(property.type());
        typeSpinner.setOnTypeChangedListener(new TypeComponent.OnTypeChangedListener() {
            @Override
            public void onTypeChanged(Type type) {
                setType();
            }
        });

        new Label(content, SWT.SINGLE).setText(property.classifier != null ? "Initial value" : "Constant value");
        setType();

        alert.setNegativeButton("Cancel", null);
        alert.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Dialogs.confirm(flowgrid.shell(), "Delete Property?",
                        "Delete property '" + property.name() + "'?", new Runnable() {
                            @Override
                            public void run() {
                                Container owner = property.owner();
                                property.delete();
                                owner.save();
                            }
                        });
            }
        });
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!Objects.equals(typeSpinner.type(), property.type())) {
                    property.setType(typeSpinner.type());
                }
                if (!Objects.equals(valueWidget.value(), property.value())) {
                    property.setValue(valueWidget.value());
                }
                property.setPublic(visibilityCombo.getSelectionIndex() == 1);

/*                if (!Objects.equals(text.getText(), property.name())) {
                    System.out.println("TBD: Rename");
                }*/
                property.save();

            }
        });
    }

    void setType() {
        if (!virtual) {
            if (valueWidget != null) {
                valueWidget.dispose();
            }
            valueWidget = new DataComponent.Builder(flowgrid).setType(typeSpinner.type()).setEditable(true).build(alert.getContentContainer());
            valueWidget.setValue(property.value());
        }
    }


    public void show() {
        alert.show();
    }


}
