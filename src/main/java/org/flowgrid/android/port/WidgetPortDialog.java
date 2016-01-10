package org.flowgrid.android.port;

import java.util.Locale;

import org.flowgrid.android.MainActivity;
import org.flowgrid.model.Callback;
import org.flowgrid.android.Dialogs;
import org.flowgrid.android.type.TypeFilter;
import org.flowgrid.android.type.TypeSpinner;
import org.flowgrid.android.type.TypeWidget;
import org.flowgrid.android.Views;
import org.flowgrid.model.Module;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.android.widget.ColumnLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;

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
  
  public static void show(MainActivity platform, final Module module, final PortCommand portCommand,
                          boolean create, final Callback<Void> callback) {
	AlertDialog.Builder alert = new AlertDialog.Builder(platform);
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
      
    final ColumnLayout main = new ColumnLayout(platform);
    main.setColumnCount(1, 2);
      
    final EditText editText = new EditText(platform);
    editText.setText(portCommand.name());
    main.addView(Views.addLabel("Name", editText), 0, 1);
   
    final TypeSpinner typeSpinner = fixedType != null ? null : new TypeSpinner(platform, module, Type.ANY, TypeFilter.ALL);
    final Spinner widgetSpinner = typeSpinner != null && input ? new Spinner(main.getContext()) : null;
      
    if (typeSpinner != null) {
      typeSpinner.setType(create ? PrimitiveType.NUMBER : portCommand.dataType());
      main.addView(Views.addLabel("Type", typeSpinner), widgetSpinner == null ? 0 : 1, 1);
  
      if (widgetSpinner != null) {
        Views.setSpinnerOptions(widgetSpinner, getInputWidgetOptions(typeSpinner.type()));
        main.addView(Views.addLabel("Widget", widgetSpinner), 1, 1);
        String currenWidget = portCommand.peerJson().getString("widget", "");
        if (currenWidget != null && !currenWidget.isEmpty()) {
          String[] widgetOptions = getInputWidgetOptions(typeSpinner.type());
          for (int i = 0; i < widgetOptions.length; i++) {
            if (widgetOptions[i].toLowerCase(Locale.US).startsWith(currenWidget)) {
              widgetSpinner.setSelection(i);
            }
          }
        }
        
        typeSpinner.setOnTypeChangedListener(new TypeWidget.OnTypeChangedListener() {
          @Override
          public void onTypeChanged(Type type) {
            Views.setSpinnerOptions(widgetSpinner, getInputWidgetOptions(type));
          }
        });
      }
    }
    
    final ColumnLayout layout = new ColumnLayout(platform);
    layout.setColumnCount(1, 2);

 //   final JSONObject peerJson = port.peerJson();
    final Spinner widthSpinner = new Spinner(platform);
    Views.setSpinnerOptions(widthSpinner, WIDTH_OPTIONS);
  
    final Spinner heightSpinner = new Spinner(platform);
    Views.setSpinnerOptions(heightSpinner, HEIGHT_OPTIONS);
      
    widthSpinner.setSelection(peerJson.getInt("width", 1));
    heightSpinner.setSelection(peerJson.getInt("height", 1));

    layout.addView(Views.addLabel("Width", widthSpinner), 1, 1);
    layout.addView(Views.addLabel("Height", heightSpinner), 1, 1);

    TabHost tabHost = Views.createTabHost(platform);
    Views.addTab(tabHost, title, main);
    Views.addTab(tabHost, "Layout", layout);
    alert.setView(tabHost);

    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, final int whichButton) {
        portCommand.setName(editText.getText().toString());
        portCommand.setDataType(fixedType == null ? typeSpinner.type() : fixedType);
        if (widgetSpinner != null) {
          String selected = (String) widgetSpinner.getSelectedItem();
          if (selected.startsWith("Slider")) {
            portCommand.peerJson().put("widget", "slider");
          } else if (selected.equals("Button")) {
            portCommand.peerJson().put("widget", "button");
          } else {
            portCommand.peerJson().remove("widget");
          }
        }
        peerJson.put("width", widthSpinner.getSelectedItemPosition());
        peerJson.put("height", heightSpinner.getSelectedItemPosition());
        callback.run(null);
      }
    };
      
    alert.setPositiveButton("Ok", listener);
    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        callback.cancel();
      }
    });

    Dialogs.showWithoutKeyboard(alert);
  }
}
