package org.flowgrid.swt.type;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.model.ArrayType;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Container;
import org.flowgrid.model.Type;
import org.flowgrid.model.io.Stat;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;

public class ArrayTypeDialog {

    public static void show(SwtFlowgrid platform, Container localModule, Type assignableTo, TypeFilter filter,
                            final Callback<Type> callback) {

        final int fixedLength = (assignableTo instanceof ArrayType) ?
                ((ArrayType) assignableTo).length : -1;
        final Type elementType = (assignableTo instanceof ArrayType) ? ((ArrayType) assignableTo).elementType :
                Type.ANY;
        AlertDialog alert = new AlertDialog(platform.shell());
        alert.setTitle("Array Type");
        Composite layout = alert.getContentContainer();
        final Button fixedLengthCheckBox = new Button(layout, SWT.CHECK);
        fixedLengthCheckBox.setText("Fixed length");

        new Label(layout, SWT.SINGLE).setText("ArrayLength");
        final Text lengthEditText = new Text(layout, SWT.SIMPLE);
        //lengthEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        lengthEditText.setEnabled(false);
        if (fixedLength != -1) {
            fixedLengthCheckBox.setSelection(true);
            fixedLengthCheckBox.setEnabled(false);
            lengthEditText.setText("" + fixedLength);
        }
        fixedLengthCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                System.out.println("FIXME: lengthEditText.setEnabled(isChecked);");
            }

        });

        new Label(layout, SWT.SINGLE).setText("Element type");
        final TypeSpinner elementTypeSpinner = new TypeSpinner(layout, platform, localModule, elementType, filter);
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
