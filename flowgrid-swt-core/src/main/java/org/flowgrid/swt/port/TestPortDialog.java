package org.flowgrid.swt.port;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.model.Callback;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;
import org.flowgrid.swt.type.TypeComponent;
import org.flowgrid.swt.type.TypeFilter;

public class TestPortDialog {

    AlertDialog alert;

    public TestPortDialog(final SwtFlowgrid flowgrid, final PortCommand portCommand, boolean create, final Callback<Void> callback) {
        System.out.println("FIXME: TestPortDialog.show()");   // FIXME
        alert = new AlertDialog(flowgrid.shell());
        final boolean output = portCommand.inputCount() > 0;

        final HutnObject peerJson = portCommand.peerJson();

        String title = output ? "Expectation" : "Test Input";

        //alert.setTitle((create ? "Add " : "Edit ") + title);

        final Composite main = alert.getContentContainer();
        //main.setColumnCount(2, 2);

        Label label = new Label(main, SWT.SINGLE);
        label.setText("Type");
        TypeFilter typeFilter = new TypeFilter.Builder().setCategory(TypeFilter.Category.PRIMITIVE).build();
        final TypeComponent inputTypeSpinner = new TypeComponent(main, flowgrid, typeFilter);
        inputTypeSpinner.setType(!(portCommand.dataType() instanceof PrimitiveType) ? PrimitiveType.NUMBER : (PrimitiveType) portCommand.dataType());

        label = new Label(main, SWT.SINGLE);
        label.setText("Text data");

        final Text testData = new Text(main, SWT.SINGLE);
//        testData.setInputType(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
  //      main.addView(Views.addLabel("Test data", testData), 0, 1);
        testData.setText(peerJson.getString("test data"));

        label = new Label(main, SWT.SINGLE);
        label.setText("Icon");
        final Combo iconSpinner = new Combo(main, SWT.READ_ONLY);
        iconSpinner.setItems(portCommand.output ? Sprite.NAMES : TestPort.OUTPUT_ICONS);

        String selected = portCommand.peerJson().getString("icon", "");
        for (int i = 0; i < Sprite.NAMES.length; i++) {
            if (Sprite.NAMES[i].equals(selected)) {
                iconSpinner.select(i);
                break;
            }
        }

        alert.setTitle(title);

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, final int whichButton) {
                portCommand.setDataType(inputTypeSpinner.type());
                peerJson.put("icon", iconSpinner.getText());
                peerJson.put("testData", testData.getText().toString());
                callback.run(null);
            }
        };

        alert.setPositiveButton("Ok", listener);
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                callback.cancel();
            }
        });


    }

    public void show() {
        alert.show();
    }
}
