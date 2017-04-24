package org.flowgrid.swt.type;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.model.ArrayType;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Container;
import org.flowgrid.model.Type;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;

public class ArrayTypeDialog {

    public static void show(SwtFlowgrid platform, Container localModule, Type assignableTo, TypeFilter.Category filter,
                            final Callback<Type> callback) {

        final int fixedLength = (assignableTo instanceof ArrayType) ?
                ((ArrayType) assignableTo).length : -1;
        final Type elementType = (assignableTo instanceof ArrayType) ? ((ArrayType) assignableTo).elementType :
                Type.ANY;
        AlertDialog alert = new AlertDialog(platform.shell());
        alert.setTitle("Array Type");

        Composite container = alert.getContentContainer();
        GridLayout containerLayout = new GridLayout(2, false);
        containerLayout.marginHeight = 0;
        containerLayout.marginWidth = 0;
        container.setLayout(containerLayout);

        new Label(container, SWT.NONE).setText("Element type");
        TypeFilter typeFilter = new TypeFilter.Builder().setLocalModule(localModule).setAssignableTo(elementType).setCategory(filter).build();
        final TypeComponent elementTypeSpinner = new TypeComponent(container, platform, typeFilter);

        new Label(container, SWT.NONE).setText("Fixed length");
        final Button fixedLengthCheckBox = new Button(container, SWT.CHECK);
        fixedLengthCheckBox.setSelection(fixedLength != -1);

        new Label(container, SWT.NONE).setText("Array Length");
        final Text lengthEditText = new Text(container, SWT.SIMPLE);
        lengthEditText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        lengthEditText.setEnabled(fixedLength != -1);
        if (fixedLength != -1) {
            lengthEditText.setText("" + fixedLength);
        }
        fixedLengthCheckBox.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                lengthEditText.setEnabled(fixedLengthCheckBox.getSelection());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });


        alert.setNegativeButton("Cancel", null);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int len = -1;
                if (lengthEditText.isEnabled()) {
                    String value = lengthEditText.getText().toString();
                    if (!value.isEmpty()) {
                        len = Integer.parseInt(value.trim());
                    }
                }
                ArrayType arrayType = new ArrayType(elementTypeSpinner.type(), len);
                callback.run(arrayType);
            }
        });

        alert.show();
    }

}
