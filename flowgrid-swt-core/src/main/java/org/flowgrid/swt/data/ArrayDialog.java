package org.flowgrid.swt.data;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.flowgrid.model.ArrayType;
import org.flowgrid.model.Callback;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayDialog {
    AlertDialog alert;
    ArrayType type;
    List<Object> newList;
    org.eclipse.swt.widgets.List swtList;

    public ArrayDialog(final SwtFlowgrid flowgrid, String name, final ArrayType type, List originalList, final Callback<List<?>> callback) {
        alert = new AlertDialog(flowgrid.shell());
        alert.setTitle(name == null || name.isEmpty() ? "Edit List" : name);
        this.type = type;

        Composite mainContainer = new Composite(alert.getContentContainer(), SWT.NONE);
        GridLayout mainLayout = new GridLayout(2, false);
        mainLayout.marginHeight = 0;
        mainLayout.marginWidth = 0;
        mainContainer.setLayout(mainLayout);
        mainContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        int originalSize = originalList == null ? 0 : originalList.size();
        newList = new ArrayList<Object>(originalSize);
        if (originalSize > 0) {
            newList.addAll(originalList);
        }
        swtList = new org.eclipse.swt.widgets.List(mainContainer, SWT.MULTI);
        swtList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        for (Object item: newList) {
            swtList.add(String.valueOf(item));
        }

        Composite rightBar = new Composite(mainContainer, SWT.NONE);
        GridLayout rightLayout = new GridLayout(1, false);
        rightLayout.marginWidth = 0;
        rightLayout.marginHeight = 0;
        rightBar.setLayout(rightLayout);

        Button addButton = new Button(rightBar, SWT.PUSH);
        addButton.setText("Add");
        addButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new DataDialog(flowgrid, "Add List Item", new Callback<Object>() {
                    @Override
                    public void run(Object value) {
                        newList.add(value);
                        swtList.add(String.valueOf(value));
                    }
                }).setType(type.elementType).show();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        Button deleteButton = new Button(rightBar, SWT.PUSH);
        deleteButton.setText("Delete selected");
        deleteButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] indices = swtList.getSelectionIndices();
                System.out.println("Selected indices: " + Arrays.toString(indices));
                for (int i = indices.length - 1; i >= 0; i--) {
                    newList.remove(indices[i]);
                    swtList.remove(indices[i]);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });



        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.cancel();
            }
        });
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.run(newList);
            }
        });

    }


    public void show() {
        alert.show();
    }
}
