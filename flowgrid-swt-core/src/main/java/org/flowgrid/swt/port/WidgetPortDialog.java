package org.flowgrid.swt.port;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.model.Callback;
import org.flowgrid.model.FlowGridException;
import org.flowgrid.model.Module;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;
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

    public static void show(SwtFlowgrid platform, final Module module, final PortCommand portCommand,
                            boolean create, final Callback<Void> callback) {
        AlertDialog alert = new AlertDialog(platform.shell());
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

        //alert.setTitle((create ? "Add " : "Edit ") + title);

        final Composite main = new Composite(alert.getContentContainer(), 0);
        GridLayout gridLayout = new GridLayout(2, false);
        main.setLayout(gridLayout);

        Label label = new Label(main, 0);
        label.setText("Name");

        final Text editText = new Text(main, 0);
        editText.setText(portCommand.name());
//        main.addView(Views.addLabel("Name", editText), 0, 1);

        boolean needsTypeSpinner = fixedType == null;
        boolean needsWidgetSpinner = needsTypeSpinner && input;

        if (needsTypeSpinner) {
            label = new Label(main, SWT.SINGLE);
            label.setText("Type");
        }

        final TypeSpinner typeSpinner = needsTypeSpinner ? new TypeSpinner(main, platform, module, Type.ANY, TypeFilter.ALL) : null;

        if (needsWidgetSpinner) {
            label = new Label(main, SWT.SINGLE);
            label.setText("MetaControl");
        }

        final Combo widgetSpinner = needsWidgetSpinner ? new Combo(main, SWT.POP_UP) : null;

        if (needsTypeSpinner) {
            typeSpinner.setType(create ? PrimitiveType.NUMBER : portCommand.dataType());

            if (widgetSpinner != null) {
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
            }
        }


        label = new Label(main, SWT.SINGLE);
        label.setText("width");
        //   final JSONObject peerJson = port.peerJson();
        final Combo widthSpinner = new Combo(main, SWT.DROP_DOWN);
        widthSpinner.setItems(WIDTH_OPTIONS);

        label = new Label(main, SWT.SINGLE);
        label.setText("height");
        final Combo heightSpinner = new Combo(main, SWT.DROP_DOWN);
        heightSpinner.setItems(HEIGHT_OPTIONS);

        widthSpinner.select(peerJson.getInt("width", 1));
        heightSpinner.select(peerJson.getInt("height", 1));

        /*
        TabHost tabHost = Views.createTabHost(platform);
        Views.addTab(tabHost, title, main);
        Views.addTab(tabHost, "Layout", layout);
        alert.setView(tabHost);*/

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

        alert.setPositiveButton("Ok", listener);
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                callback.cancel();
            }
        });


        alert.show();
    }


}
