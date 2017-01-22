package org.flowgrid.swt.port;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Module;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;
import org.flowgrid.swt.type.TypeComponent;
import org.flowgrid.swt.type.TypeFilter;
import org.flowgrid.swt.type.TypeSpinner;
import org.flowgrid.swt.type.TypeWidget;

import java.util.Locale;

public class WidgetPortDialog {
    private static final String[] INPUT_OPTIONS = {"Input Field"};
    private static final String[] INPUT_OPTIONS_NUMBER = {"Input Field", "Slider (0..100)"};
    private static final String[] INPUT_OPTIONS_BOOLEAN = {"Switch", "Button"};

    private static final String[] WIDTH_OPTIONS = {"Full Width", "1", "2"};
    private static final String[] HEIGHT_OPTIONS = {"Full Height", "1", "2", "3", "4"};

    private static String[] getInputWidgetOptions(Type type) {
        return type == PrimitiveType.NUMBER ? INPUT_OPTIONS_NUMBER :
                type == PrimitiveType.BOOLEAN ? INPUT_OPTIONS_BOOLEAN : INPUT_OPTIONS;
    }

    private final AlertDialog alert;
    private Combo widgetSpinner;
    private TypeComponent typeSpinner;

    public WidgetPortDialog(SwtFlowgrid platform, final Module module, final PortCommand portCommand,
                            boolean create, final Callback<Void> callback) {
        alert = new AlertDialog(platform.shell());
        final boolean output = portCommand.inputCount() > 0;
        final boolean input = portCommand.outputCount() > 0;

        final HutnObject peerJson = portCommand.peerJson();
        String widget = portCommand.peerJson().getString("widget", null);

        String title;
        final Type fixedType;
        if ("canvas".equals(widget)) {
            title = "Canvas";
            fixedType = module.typeForName("/media/Canvas");
        } else if ("runchart".equals(widget)) {
            title = "Run chart";
            fixedType = null;
        } else if ("histogram".equals(widget)) {
            // TODO(haustein): Array types are OK, too...
            title = "Histogram";
            fixedType = PrimitiveType.NUMBER;
        } else if ("percent".equals(widget)) {
            title = "Percent";
            fixedType = PrimitiveType.NUMBER;
        } else if ("webview".equals(widget)) {
            title = "Web View";
            fixedType = PrimitiveType.TEXT;
        } else if (input) {
            title = output ? "Combined Field" : "Input field";
            fixedType = null;
        } else {
            title = "Output Field";
            fixedType = null;
        }

        alert.setTitle((create ? "Add " : "Edit ") + title);

        final Composite main = new Composite(alert.getContentContainer(), 0);
        GridLayout gridLayout = new GridLayout(6, false);
        main.setLayout(gridLayout);
        main.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        new Label(main, SWT.NONE).setText("Name:");
        final Text editText = new Text(main, 0);
        editText.setText(portCommand.name());
        editText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 5, 1));

        if (fixedType == null) {
            new Label(main, SWT.NONE).setText("Type:");
            TypeFilter typeFilter = new TypeFilter.Builder().setLocalModule(module).build();
            typeSpinner = new TypeComponent(platform, main, typeFilter);
            typeSpinner.setType(create ? PrimitiveType.NUMBER : portCommand.dataType());

            if (input) {
                new Label(main, SWT.NONE);
                new Label(main, SWT.NONE).setText("Control:");
                widgetSpinner = new Combo(main, SWT.POP_UP);
                widgetSpinner.setItems(getInputWidgetOptions(typeSpinner.type()));

                String currenWidget = portCommand.peerJson().getString("widget", "");
                if (currenWidget != null && !currenWidget.isEmpty()) {
                    String[] widgetOptions = getInputWidgetOptions(typeSpinner.type());
                    for (int i = 0; i < widgetOptions.length; i++) {
                        if (widgetOptions[i].toLowerCase(Locale.US).startsWith(currenWidget)) {
                            widgetSpinner.select(i);
                        }
                    }
                }
                typeSpinner.setOnTypeChangedListener(new TypeWidget.OnTypeChangedListener() {
                    @Override
                    public void onTypeChanged(Type type) {
                        widgetSpinner.setItems(getInputWidgetOptions(type));
                    }
                });
                new Label(main, SWT.NONE);
            } else {
                new Label(main, SWT.SINGLE).setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 5, 1));
            }
        } 

        new Label(main, SWT.NONE).setText("Width:");

        final Combo widthSpinner = new Combo(main, SWT.DROP_DOWN);
        widthSpinner.setItems(WIDTH_OPTIONS);

        new Label(main, SWT.NONE).setText("\u00a0\u00a0\u00a0\u00a0");
        new Label(main, SWT.NONE).setText("Height:");

        final Combo heightSpinner = new Combo(main, SWT.DROP_DOWN);
        heightSpinner.setItems(HEIGHT_OPTIONS);

        widthSpinner.select(peerJson.getInt("width", 1));
        heightSpinner.select(peerJson.getInt("height", 1));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, final int whichButton) {
                portCommand.setName(editText.getText().toString());
                portCommand.setDataType(fixedType == null ? typeSpinner.type() : fixedType);
                if (widgetSpinner != null) {
                    String selected = (String) widgetSpinner.getText();
                    if (selected.startsWith("Slider")) {
                        portCommand.peerJson().put("widget", "slider");
                    } else if (selected.equals("Button")) {
                        portCommand.peerJson().put("widget", "button");
                    } else {
                        portCommand.peerJson().remove("widget");
                    }
                }
                peerJson.put("width", widthSpinner.getSelectionIndex());
                peerJson.put("height", heightSpinner.getSelectionIndex());
                callback.run(null);
            }
        };

        new Label(main, SWT.NONE).setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));


        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                callback.cancel();
            }
        });
        alert.setPositiveButton("Ok", listener);
    }

    public void show() {
        alert.show();
    }

}
