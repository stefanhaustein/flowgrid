package org.flowgrid.android.operation;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TreeMap;

import org.flowgrid.R;
import org.flowgrid.android.Dialogs;
import org.flowgrid.android.port.FirmataPort;
import org.flowgrid.android.port.FirmataPortDialog;
import org.flowgrid.android.port.TestPort;
import org.flowgrid.android.port.TestPortDialog;
import org.flowgrid.android.port.WidgetPort;
import org.flowgrid.android.port.WidgetPortDialog;
import org.flowgrid.model.Callback;
import org.flowgrid.android.widget.MetaLayout;
import org.flowgrid.android.api.ImageImpl;
import org.flowgrid.android.MainActivity;
import org.flowgrid.android.UiTimerTask;
import org.flowgrid.android.Views;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Cell;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.Edge;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Module;
import org.flowgrid.model.Port;
import org.flowgrid.model.Position;
import org.flowgrid.model.Property;
import org.flowgrid.model.ResourceFile;
import org.flowgrid.model.TutorialData;
import org.flowgrid.model.Type;
import org.flowgrid.model.TypeAndValue;
import org.flowgrid.model.Command;
import org.flowgrid.model.api.ConstructorCommand;
import org.flowgrid.model.api.LiteralCommand;
import org.flowgrid.model.api.LocalCallCommand;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.model.api.PropertyCommand;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.model.hutn.HutnWriter;
import org.flowgrid.model.hutn.Hutn;
import org.flowgrid.android.widget.ColumnLayout;
import org.flowgrid.android.widget.ContextMenu;
import org.flowgrid.android.widget.ContextMenu.Item;
import org.flowgrid.android.widget.ContextMenu.ItemClickListener;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ActionBar.LayoutParams;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.hardware.Sensor;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VerticalSeekBar;

import com.mobidevelop.widget.SplitPaneLayout;

public class EditOperationFragment extends AbstractOperationFragment<SplitPaneLayout> implements ItemClickListener {

  private static final String MENU_ITEM_DATA_IO = "Data / IO\u2026";

  private static final String TAG = "EditOperationFragment";

  static final String MENU_ITEM_CANVAS = "Canvas";
  static final String MENU_ITEM_COMBINED_FIELD = "Combined field";
  static final String MENU_ITEM_CONTINUOUS_INPUT = "Continuous input";
  static final String MENU_ITEM_CLEAR_CELL = "Clear cell";
  static final String MENU_ITEM_CREATE_SHORTCUT = "Create shortcut";
  static final String MENU_ITEM_DELETE_COLUMN = "Delete column";
  static final String MENU_ITEM_DELETE_PATH = "Delete path";
  static final String MENU_ITEM_DELETE_ROW = "Delete row";
  static final String MENU_ITEM_EDIT = "Edit\u2026";
  static final String MENU_ITEM_EXPECTATION = "Expectation";
  static final String MENU_ITEM_FIRMATA_ANALOG_INPUT = "Firmata Analog input";
  static final String MENU_ITEM_FIRMATA_ANALOG_OUTPUT = "Firmata Analog output";
  static final String MENU_ITEM_FIRMATA_DIGITAL_OUTPUT = "Firmata Digital output";
  static final String MENU_ITEM_FIRMATA_DIGITAL_INPUT = "Firmata Digital input";
  static final String MENU_ITEM_FIRMATA_SERVO_OUTPUT = "Firmata Servo output";
  static final String MENU_ITEM_HISTOGRAM = "Histogram";
  static final String MENU_ITEM_INPUT_FIELD = "Input field";
  static final String MENU_ITEM_CONTROL = "Control\u2026";
  static final String MENU_ITEM_PASTE = "Paste";
  static final String MENU_ITEM_PERCENT_BAR = "Percent bar";
  static final String MENU_ITEM_TEST_INPUT = "Test input";
  static final String MENU_ITEM_TUTORIAL_SETTINGS = "Tutorial settings";
  static final String MENU_ITEM_OUTPUT_FIELD = "Output field";
  static final String MENU_ITEM_EDIT_CELL = "Edit cell";
  static final String MENU_ITEM_RUN_CHART = "Run chart";
  static final String MENU_ITEM_RUN_MODE = "Run mode";
  static final String MENU_ITEM_RESET = "Reset";
  static final String MENU_ITEM_UNDO = "Undo";
  static final String MENU_ITEM_WEB_VIEW = "Web view";
  static final String MENU_ITEM_INSERT_COLUMN = "Insert column";
  static final String MENU_ITEM_INSERT_ROW = "Insert row";
  static final String MENU_ITEM_ADD_BUFFER = "Add buffer";
  static final String MENU_ITEM_REMOVE_BUFFER = "Remove buffer";
  static final String MENU_ITEM_OPERATIONS_CLASSES = "Operations / classes\u2026";
  static final String MENU_ITEM_THIS_MODULE = "This module\u2026";
  static final String MENU_ITEM_THIS_CLASS = "This class\u2026";
  static final String MENU_ITEM_TUTORIAL_MODE = "Tutorial mode";

  static final String[] OPERATION_MENU_FILTER = {"control", "examples", "missions", "system"};
  static final String[] TUTORIAL_EDITOR_OPERATION_MENU_FILTER = {"control", "missions", "system"};

  static final String[] TUTORIAL_MENU_OPTIONS = {
    MENU_ITEM_DATA_IO,
      MENU_ITEM_CONTROL,
    MENU_ITEM_THIS_MODULE,
    MENU_ITEM_OPERATIONS_CLASSES,
    MENU_ITEM_EDIT
  };
  public static final String MENU_ITEM_CONSTANT_VALUE = "Constant value\u2026";


  @SuppressWarnings("serial")
  static TreeMap<String, Integer> SENSOR_MAP = new TreeMap<String, Integer>() {
    {
      put("Accelerometer", Sensor.TYPE_ACCELEROMETER);
      put("Ambient temperature", Sensor.TYPE_AMBIENT_TEMPERATURE);
      put("Gravity", Sensor.TYPE_GRAVITY);
      put("Gyroscope", Sensor.TYPE_GYROSCOPE);
      put("Light", Sensor.TYPE_LIGHT);
      put("Linear acceleration", Sensor.TYPE_LINEAR_ACCELERATION);
      put("Magnetic field", Sensor.TYPE_MAGNETIC_FIELD);
   // SENSOR_MAP.writeLong("Orientation", Sensor.TYPE_ORIENTATION);
      put("Pressure", Sensor.TYPE_PRESSURE);
      put("Proximity", Sensor.TYPE_PROXIMITY);
      put("Relative humidity", Sensor.TYPE_RELATIVE_HUMIDITY);
      put("Rotation vector", Sensor.TYPE_ROTATION_VECTOR);
    }
  };

  TextView tutorialHelpView;
  EditOperationView operationView;
  SelectionView selection;
  FrameLayout frameLayout;
 // ImageButton overflowButton;
  LinearLayout topRightButtons;
 // LinearLayout selectionButtons;
 // Menu loadMenu;
  ArrayList<Type> currentTypeFilter = new ArrayList<Type>();
  ArrayList<Integer> currentAutoConnectStartRow = new ArrayList<Integer>();
  float pixelPerDp;
  ColumnLayout menu;
  private boolean changing = false;
  int countedToRow = -1;
  int counted = 0;
  VerticalSeekBar speedBar;
  ArrayList<String> undoHistory;  // Created in onCreateView
  boolean tutorialMode;
  LinearLayout outerControlLayout;
  TextView fakeActionBar;
  boolean landscapeMode;
  private boolean selectionMode;
  private ColumnLayout columnLayout;

  public EditOperationFragment() {
    super(SplitPaneLayout.class);
  }

  private void addMemberCommand(Command command) {
    beforeChange();
    operation.setCommand(selection.row(), selection.col(), command);
    afterChange();
  }

  
  private void addWidgetPort(final boolean input, final boolean output, String widget) {
    // TODO(haustein) Move name disambiguation into addPortCommand and remove this?
    HashSet<String> usedNames = new HashSet<>();
    for (WidgetPort i: portWidgets()) {
      usedNames.add(i.port().name());
    }
    String namePrefix = widget != null ? widget : 
        input && output ? "io" : output ? "out" : "in";
    int index = 1;
    String suffix = "";
    while (usedNames.contains(namePrefix + suffix)) {
      index++;
      suffix = String.valueOf(index);
    }
  
    String name = namePrefix + suffix;
    if (widget != null) {
      addPortCommand("Widget", name, input, output, "widget", widget);
    } else {
      addPortCommand("Widget", name, input, output);
    }
  }

  void addPortCommand(String type, String name, boolean input, boolean output, Object... peerJson) {
    PortCommand portCommand = new PortCommand(name, input, output);
    portCommand.peerJson().put("portType", type);
    for (int i = 0; i < peerJson.length; i += 2) {
      portCommand.peerJson().put((String) peerJson[i], peerJson[i + 1]);
    }
    editPort(portCommand, true);
  }
  
  private void editPort(final PortCommand portCommand, final boolean creating) {
    Callback<Void> callback = new Callback<Void>() {
      @Override
      public void run(Void value) {
        beforeChange();
        if (creating) {
          controller.operation.setCommand(selection.row(), selection.col(), portCommand);
        } else {
          portCommand.detach();
        }
        attachPort(portCommand);
        afterChange();
      }

      @Override
      public void cancel() {
        resetTutorial();
      }
    };
    
    String portType = portType(portCommand.peerJson());
    if (portType.equals("Sensor")) {
      callback.run(null);
    } else if (portType.equals("Firmata")) {
      FirmataPortDialog.show(platform, platform.model(), portCommand, creating, callback);
    } else if (portType.equals("Test")) {
      TestPortDialog.show(platform, portCommand, creating, callback);
    } else {
      WidgetPortDialog.show(platform, operation.module, portCommand, creating, callback);
    }
  }

  private void addFirmataPort(boolean input, boolean output, FirmataPort.Mode mode) {
    addPortCommand("Firmata", "Firmata " + mode.toString().substring(0, 1) + 0, input, output,
        "pin", 0, "mode", mode);
  }

  void afterBulkChange() {
    attachAll();
    afterChange();
  }
  
  void beforeBulkChange() {
    beforeChange();
    detachAll();
  }
  
  void beforeChange() {
    if (changing) {
      Log.d("FlowGrid", "beforeChange called with changing = true");
    }
    changing = true;
    stop();
  }
  
  void afterChange() {
    if (!changing) {
      Log.d("FlowGrid", "afterChange called with changing = false");
    }
    selection.setVisibility(View.INVISIBLE);

    Cell cell = operation.cell(selection.row, selection.col);
    if (cell != null && cell.command() != null) {
      Command cmd = cell.command();
      for (int i = 0; i < Math.min(cmd.inputCount(), currentTypeFilter.size()); i++) {     
        if (!currentTypeFilter.get(i).isAssignableFrom(cmd.inputType(i))) {
          break;
        }
        for (int row = currentAutoConnectStartRow.get(i); row < selection.row; row++) {
          operation.connect(row, selection.col + i, Edge.TOP, Edge.BOTTOM);
        }
      }
    }
    currentTypeFilter.clear();
    currentAutoConnectStartRow.clear();

    StringWriter sw = new StringWriter();
    HutnWriter writer = new HutnWriter(sw);
    writer.startObject();
    operation.toJson(writer, Artifact.SerializationType.FULL);
    writer.endObject();
    writer.close();
    String currentJson = sw.toString();
    String lastJson = undoHistory.get(undoHistory.size() - 1);

    if (!lastJson.equals(currentJson)) {
      platform.log("push json: " + currentJson);
      undoHistory.add(currentJson);
      operationView.invalidate();
      operation.save();
      operation.validate();
      updateLayout();
      if (undoHistory.size() == 2) {
        updateMenu();
      }
    }
  }

  void resetTutorial() {
    countedToRow = -1;
    start();
    stop();
    EditOperationFragment.this.operationView.postInvalidate();
  }

  void checkTutorialSuccess() {
    final TutorialData tutorialData = operation.tutorialData;
    final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
    alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        resetTutorial();
      }
    });
    alert.setNegativeButton("Back", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        resetTutorial();
      }
    });
    boolean success = true;
    for (Port port : ports()) {
      if (port instanceof TestPort) {
        TestPort test = (TestPort) port;
        if (!test.passes()) {
          alert.setTitle("Bummer!");
          success = false;
          alert.setMessage("The program did not generate the expected output. Consider the help text and try again!");
          break;
        }
      }
    }
    if (success) {
      alert.setPositiveButton("Next", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          countedToRow = -1;
          Iterator<Artifact> it = operation.module().iterator();
          CustomOperation next = null;
          while (it.hasNext()) {
            if (it.next() == operation && it.hasNext()) {
              next = (CustomOperation) it.next();
              break;
            }
          }
          if (next != null) {
            platform.openOperation(next, true);
          } else {
            navigateUp();
          }
        }
      });
      this.countedToRow = tutorialData.editableStartRow;
      operationView.postInvalidate();
      new UiTimerTask(platform) {
        @Override
        public void runOnUiThread() {
          if (countedToRow < tutorialData.editableEndRow) {
            countedToRow++;
            operationView.invalidate();
          } else {
            cancel();
            
            beforeChange();
            tutorialData.passedWithStars = counted <= tutorialData.optimalCellCount ? 3 
                : counted <= tutorialData.optimalCellCount * 4 / 3 ? 2 : 1;
            afterChange();
            alert.setTitle((tutorialData.passedWithStars == 3 ? "Perfect " : "Success ") + "\u2b50\u2b50\u2b50".substring(0, tutorialData.passedWithStars));
            alert.setMessage("Used cell units: " + counted + "\n" +
                "Optimal cell units: " + tutorialData.optimalCellCount);
            alert.show();
          }
        }
      }.schedule(100, 100);
    } else {
      if (operation.hasDocumentation() && !landscapeMode) {
        alert.setNeutralButton("Help", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            OperationHelpDialog.show(EditOperationFragment.this);
          }
        });
      }
      alert.show();
    }
  }

  void addLiteral() {
    platform.editStructuredDataValue(operation(), new String[]{"literal", Position.toString(selection.row, selection.col)},
        selection, new Callback<TypeAndValue>() {
          @Override
          public void run(TypeAndValue variant) {
            beforeChange();
            LiteralCommand literal = new LiteralCommand(platform.model(), variant.type, variant.value);
            operation.setCommand(selection.row(), selection.col(), literal);
            selection.setVisibility(View.INVISIBLE);
            afterChange();
          }

          @Override
          public void cancel() {
            selection.setVisibility(View.INVISIBLE);
          }
        });
  }
  
  @Override
  public boolean onContextMenuItemClick(Item item) {
    final String label = item.getTitle().toString();

    if (MENU_ITEM_STOP.equals(label)) {
      stop();
      updateMenu();
      return true;
    }
    if (MENU_ITEM_PLAY.equals(label)) {
      StringBuilder missing = new StringBuilder();
      if (!operation.asyncInput() && !isInputComplete(missing)) {
        Toast.makeText(platform, "Missing input: " + missing, Toast.LENGTH_LONG).show();
      } else {
        start();
        updateMenu();
      }
      return true;
    }
    if (MENU_ITEM_DOCUMENTATION.equals(label)) {
      OperationHelpDialog.show(EditOperationFragment.this);
      return true;
    }
    if (MENU_ITEM_COPY.equals(label)) {
      platform.setEditBuffer(operation.copy(selection.row, selection.col, selection.height, selection.width));
      setSelectionMode(false);
      return true;
    }
    if (MENU_ITEM_CUT.equals(label)) {
      platform.setEditBuffer(operation.copy(selection.row, selection.col, selection.height, selection.width));
      beforeBulkChange();
      operation.clear(selection.row, selection.col, selection.height, selection.width);
      afterBulkChange();
      setSelectionMode(false);
      return true;
    }
    if (MENU_ITEM_CANCEL.equals(label)) {
      setSelectionMode(false);
      return true;
    }
    if (label.equals(MENU_ITEM_CONTINUOUS_INPUT)) {
      beforeChange();
      operation.setAsyncInput(!operation.asyncInput());
      afterChange();
      updateMenu();
      return true;
    }
    if (MENU_ITEM_PUBLIC.equals(label)) {
      beforeChange();
      artifact.setPublic(!artifact.isPublic());
      afterChange();
      updateMenu();
      return true;
    }

    if (label.equals(MENU_ITEM_RESET)) {
      if (operation.isTutorial()) {
        TutorialData tutorialData = operation.tutorialData;
        beforeChange();
        operation.clear(tutorialData.editableStartRow, 0, tutorialData.editableEndRow - tutorialData.editableStartRow, Integer.MAX_VALUE / 2);
        if (platform.settings().developerMode()) {
          tutorialData.passedWithStars = 0;
        }
        afterChange();
      }
      return true;
    }

    if (label.equals(MENU_ITEM_TUTORIAL_MODE)) {
      tutorialMode = !item.isChecked();
      updateMenu();
      return true;
    }

    if (label.equals(MENU_ITEM_UNDO)) {
      HutnObject json = (HutnObject) Hutn.parse(undoHistory.get(undoHistory.size() - 2));
      platform.log("undo to: " + json.toString());
      beforeBulkChange();
      operation.clear();
      operation.setPublic(false);
      operation.setAsyncInput(false);
      operation.fromJson(json, Artifact.SerializationType.FULL, null);
      afterBulkChange();
      undoHistory.remove(undoHistory.size() - 1);
      undoHistory.remove(undoHistory.size() - 1);
      if (undoHistory.size() == 1) {
        updateMenu();
      }
    }

    if (label.equals(MENU_ITEM_TUTORIAL_SETTINGS)) {
      TutorialSettingsDialog.show(this);
      return true;
    }

    if (currentTypeFilter.size() > 0 && currentTypeFilter.get(0) instanceof Classifier &&
        label.equals(currentTypeFilter.get(0).name() + "\u2026")) {
      new CommandMenu(selection, item, operation.module(), currentTypeFilter, new Callback<Command>() {
        @Override
        public void run(Command value) {
          addMemberCommand(value);
        }
      }).showType((Classifier) currentTypeFilter.get(0), false);
      return true;
    }

    if (label.equals(MENU_ITEM_THIS_CLASS)) {
      System.out.println("calling new artifactment.show");
      new CommandMenu(selection, item, operation.module(), currentTypeFilter, new Callback<Command>() {
        @Override
        public void run(Command value) {
          addMemberCommand(value);
        }
      }).showType(operation.classifier, true);
      return true;
    }

    // Needs to be after MENU_ITEM_PUBLIC because we implement slightly different
    // behavior here.
    if (super.onContextMenuItemClick(item)) {
      return true;
    }



    Module module = null;
    String[] filter = {};
    if (label.equals(MENU_ITEM_OPERATIONS_CLASSES)) {
      module = platform.model().rootModule;
      filter = operation().isTutorial() && !tutorialMode
          ? TUTORIAL_EDITOR_OPERATION_MENU_FILTER
          : OPERATION_MENU_FILTER;
    } else if (label.equals(MENU_ITEM_THIS_MODULE)) {
      module = operation.module();
    } else if (label.equals(MENU_ITEM_CONTROL)) {
      module = platform.model().rootModule.module("control");
    }
    if (module != null) {
      new CommandMenu(selection, item, operation.module(), currentTypeFilter, new Callback<Command>() {
        @Override
        public void run(Command result) {
          addMemberCommand(result);
        }
      }).showModule(module, false, filter);


//      currentModule = platform.model().rootModule();
//      updatePackageMenu(item);
      return true;
    }

    if (SENSOR_MAP.containsKey(label)) {
      beforeChange();
      addPortCommand("Sensor", label, true, false, "sensor", SENSOR_MAP.get(label));
      afterChange();
      suggestContinuous("Most sensors provide a continous stream of input and may not work for " +
          "regular operations.");
      return true;
    }


    Command command = null;
    int row = selection.row();
    int col = selection.col();

    if (label.equals(MENU_ITEM_ADD_BUFFER) || label.equals(MENU_ITEM_REMOVE_BUFFER)) {
      Cell below = operation.cell(row + 1, col);
      int index = col - below.col();
      beforeChange();
      below.setBuffered(index, label.equals(MENU_ITEM_ADD_BUFFER));
      afterChange();
    } else if (label.equals(MENU_ITEM_RUN_MODE)) {
      platform.openOperation(operation, false);
    } else if (label.equals(MENU_ITEM_CREATE_SHORTCUT)) {
      createShortcut();
    } else if (label.equals(MENU_ITEM_DELETE_PATH)) {
      beforeChange();
      operation.removePath(row, col, tutorialMode);
      afterChange();
    } else if (label.equals(MENU_ITEM_CLEAR_CELL)) {
      beforeChange();
      operation.removeCell(row, col);
      afterChange();
    } else if (label.equals(MENU_ITEM_EDIT_CELL)) {
      Command cmd = operation.cell(row, col).command();
      if (cmd instanceof Artifact) {
        platform.openArtifact((Artifact) cmd);
      } else if (cmd instanceof PropertyCommand) {
        Property p = ((PropertyCommand) cmd).property();
        platform.openProperty(p);
      } else if (cmd instanceof LocalCallCommand) {
        CustomOperation op = ((LocalCallCommand) cmd).operation();
        platform.openOperation(op, true);
      } else if (cmd instanceof PortCommand) {
        editPort((PortCommand) cmd, false);
      } else if (cmd instanceof ConstructorCommand) {
        platform.openClassifier(((ConstructorCommand) cmd).classifier());
      }
    } else if (label.equals(MENU_ITEM_INPUT_FIELD)) {
      addWidgetPort(true, false, null);
    } else if (label.equals(MENU_ITEM_OUTPUT_FIELD)) {
      addWidgetPort(false, true, null);
    } else if (label.equals(MENU_ITEM_TEST_INPUT)) {
      addPortCommand("Test", "TestInput", true, false, "testData", "");
    } else if (label.equals(MENU_ITEM_EXPECTATION)) {
      addPortCommand("Test", "Expectation", false, true, "testData", "");
    } else if (label.equals(MENU_ITEM_CANVAS)) {
      addWidgetPort(true, false, "canvas");
    } else if (label.equals(MENU_ITEM_HISTOGRAM)) {
      addWidgetPort(false, true, "histogram");
    } else if (label.equals(MENU_ITEM_RUN_CHART)) {
      addWidgetPort(false, true, "runchart");
    } else if (label.equals(MENU_ITEM_WEB_VIEW)) {
      addWidgetPort(true, true, "webview");
    } else if (label.equals(MENU_ITEM_PERCENT_BAR)) {
      addWidgetPort(false, true, "percent");
    } else if (label.equals(MENU_ITEM_COMBINED_FIELD)) {
      addWidgetPort(true, true, null);
    } else if (label.equals(MENU_ITEM_CONSTANT_VALUE)) {
      addLiteral();
    } else if (label.equals(MENU_ITEM_INSERT_ROW)) {
      beforeChange();
      operation.insertRow(row);
      afterChange();
    } else if (label.equals(MENU_ITEM_INSERT_COLUMN)) {
      beforeChange();
      operation.insertCol(col);
      afterChange();
    } else if (label.equals(MENU_ITEM_DELETE_ROW)) {
      beforeChange();
      operation.deleteRow(row);
      afterChange();
    } else if (label.equals(MENU_ITEM_DELETE_COLUMN)) {
      beforeChange();
      operation.deleteCol(col);
      afterChange();
    } else if (label.equals(MENU_ITEM_PASTE)) {
      beforeBulkChange();
      operation.cellsFromJson(platform.editBuffer(), selection.row, selection.col);
      afterBulkChange();
    } else if (label.equals(MENU_ITEM_FIRMATA_ANALOG_INPUT)) {
      addFirmataPort(true, false, FirmataPort.Mode.ANALOG);
    } else if (label.equals(MENU_ITEM_FIRMATA_ANALOG_OUTPUT)) {
      addFirmataPort(false, true, FirmataPort.Mode.ANALOG);
    } else if (label.equals(MENU_ITEM_FIRMATA_DIGITAL_INPUT)) {
      addFirmataPort(true, false, FirmataPort.Mode.DIGITAL);
    } else if (label.equals(MENU_ITEM_FIRMATA_DIGITAL_OUTPUT)) {
      addFirmataPort(false, true, FirmataPort.Mode.DIGITAL);
    } else if (label.equals(MENU_ITEM_FIRMATA_SERVO_OUTPUT)) {
      addFirmataPort(false, true, FirmataPort.Mode.SERVO);
    }

    if (command != null) {
      beforeChange();
      operation.setCommand(row, col, command);
      afterChange();
    }
    return true;
  }

  private void suggestContinuous(String text) {
    if (operation().asyncInput()) {
      return;
    }
    Dialogs.confirm(platform, "Switch to continuous mode?", text, new Runnable() {
      @Override
      public void run() {
        beforeChange();
        operation.setAsyncInput(true);
        afterChange();
        updateMenu();
      }
    });
  }

  private void createShortcut() {
    Intent shortcutIntent = new Intent(Intent.ACTION_RUN);
    shortcutIntent.setClass(platform, MainActivity.class);
    shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    shortcutIntent.putExtra("run", operation.qualifiedName());

    Intent addIntent = new Intent();
    addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
    addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, operation.name().equals("main") ? operation.module.name() : operation().name());
    
    boolean iconAdded = false;
    if (operation.name().equals("main") && operation.module.hasArtifact("main.png")) {
      Artifact mainPngArtifact = operation.module.artifact("main.png");
      if (mainPngArtifact instanceof ResourceFile) {
        ResourceFile mainPngRes = (ResourceFile) mainPngArtifact;
        if (mainPngRes.kind == ResourceFile.Kind.IMAGE) {
          try {
            ImageImpl image = (ImageImpl) mainPngRes.resource();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, image.bitmap());
            iconAdded = true;
          } catch (IOException e) {
            platform.error("Error loading resource", e);
          }
        }
      }
    }
    
    if (!iconAdded) {
      addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
          Intent.ShortcutIconResource.fromContext(platform, R.drawable.ic_launch_operation_default));
    }
    addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
    platform.sendBroadcast(addIntent);

    Dialogs.info(platform, "Create shortcut", "Home screen shortcut created successfully.");
  }

  private void navigateUp() {
    if (operation.classifier != null) {
      platform.openClassifier(operation.classifier);
    } else {
      platform.openModule(operation.module());
    }
  }

  @Override
  protected void updateMenu() {
    clearMenu();

    SpannableString title = new SpannableString("\u2039 " + operation().name());
    if (operation().asyncInput()) {
      title.setSpan(new UnderlineSpan(), 2, title.length(), 0);
    }
    if (artifact.isPublic()) {
      title.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 2, title.length(), 0);
    }

    fakeActionBar.setText(title);
    setArtifact(operation());  // Updates the action bar.

    if (selectionMode) {
      addMenuItem(MENU_ITEM_COPY);
      addMenuItem(MENU_ITEM_CUT);
      addMenuItem(MENU_ITEM_CANCEL);
    } else {
      addMenuItem(running ? MENU_ITEM_STOP : MENU_ITEM_PLAY);
      addMenuItem(operation().hasDocumentation() ? MENU_ITEM_DOCUMENTATION : MENU_ITEM_ADD_DOCUMENTATION);

      addMenuItem(MENU_ITEM_UNDO).setEnabled(undoHistory.size() > 1);

      if (!operation.isTutorial()) {
        addMenuItem(MENU_ITEM_RUN_MODE);
        if (operation.name().equals("main")) {
          addMenuItem(MENU_ITEM_CREATE_SHORTCUT);
        }
        addMenuItem(MENU_ITEM_PUBLIC);
        addMenuItem(MENU_ITEM_CONTINUOUS_INPUT).setCheckable(true).setChecked(operation.asyncInput());
      } else if (!tutorialMode) {
        addMenuItem(MENU_ITEM_TUTORIAL_SETTINGS);
      }

      if (operation.isTutorial() && platform.settings().developerMode()) {
        Item tmt = addMenuItem(MENU_ITEM_TUTORIAL_MODE);
        tmt.setCheckable(true);
        tmt.setChecked(tutorialMode);
      }

      if (operation.isTutorial()) {
        addMenuItem(MENU_ITEM_RESET);
      }

      if (!tutorialMode) {
        if (operation().classifier == null) {
          addMenuItem(MENU_ITEM_RENAME_MOVE);
        } else {
          addMenuItem(MENU_ITEM_RENAME);
        }
        addMenuItem(MENU_ITEM_DELETE);
      }
    }
    topRightButtons.removeAllViews();
    int padding = Views.px(platform, 12);
    for (Object action: actions.items()) {
      final ContextMenu.Item item = (ContextMenu.Item) action;
      ImageButton button = new ImageButton(platform);
      button.setImageResource(item.getIcon());
      button.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
      button.setPadding(padding, padding, padding, padding);
      topRightButtons.addView(button);
      button.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          onContextMenuItemClick(item);
        }
      });
    }
    if (!selectionMode) {
      ImageButton menuButton = new ImageButton(platform);
      menuButton.setImageResource(R.drawable.ic_more_vert_white_24dp);
      menuButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
      menuButton.setPadding(padding, padding, padding, padding);
      topRightButtons.addView(menuButton);
      menuButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          showMenu();
        }
      });
    }
  }


  @Override
  public void onPlatformAvailable(Bundle savedInstanceState) {
    controlLayout = columnLayout = new ColumnLayout(platform);
    super.onPlatformAvailable(savedInstanceState);

    final SplitPaneLayout splitPane = (SplitPaneLayout) rootView;  // Cast: Why?!?
    splitPane.setSplitterSize(0);
    splitPane.setSplitterMovable(false);

    ScrollView scrollView = new ScrollView(platform);

    scrollView.addView(controlLayout.view());

    this.tutorialMode = operation.isTutorial();

    if (undoHistory == null) {
      undoHistory = new ArrayList<String>();
      HutnWriter writer = new HutnWriter(new StringWriter());
      writer.startObject();
      operation.toJson(writer, Artifact.SerializationType.FULL);
      writer.endObject();
      undoHistory.add(writer.close().toString());
    }
    
    pixelPerDp = Views.px(platform, 1);
    
    controller.setVisual(true);

    selection = new SelectionView(this);
    operationView = new EditOperationView(this);
 //   splitPane.setSplitterSize((int) (pixelPerDp * 2)); // Math.min(screenSize.x, screenSize.y) / 20);
 //   splitPane.setSplitterDrawable(new ColorDrawable(0x0ff0099cc));
   //   splitPane.setSplitterDrawable(new ColorDrawable(Color.LTGRAY));
   //   splitPane.setSplitterDraggingDrawable(new ColorDrawable(Color.GRAY));

    outerControlLayout = new LinearLayout(getActivity());
    outerControlLayout.setOrientation(LinearLayout.VERTICAL);
    outerControlLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
   
/*    ShapeDrawable d = new ShapeDrawable();
    d.setIntrinsicHeight(100);
    d.setIntrinsicWidth(100); */

    ColorDrawable d = new ColorDrawable(0x0ff0099cc) {
      @Override
      public int getIntrinsicHeight() {
        return (int) (pixelPerDp * 2);
      }
    };

    outerControlLayout.setDividerDrawable(
        new InsetDrawable(d, 0, 0, 0, (int) (8 * pixelPerDp)));
  //  outerControlLayout.setDividerPadding(100);
    
    fakeActionBar = new TextView(getActivity());
    Views.applyEditTextStyle(fakeActionBar, false);
    fakeActionBar.setPadding(0, (int) (12 * pixelPerDp), 0, (int) (12 * pixelPerDp));
    // The text is added in updateMenu()
    fakeActionBar.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        navigateUp();
      }
    });

    outerControlLayout.addView(fakeActionBar);
    
    outerControlLayout.addView(scrollView);
    tutorialHelpView = new TextView(platform);

    if (operation.isTutorial()) {
      tutorialHelpView.setText(operation.documentation());
      outerControlLayout.addView(tutorialHelpView);
    }
    
    frameLayout = new FrameLayout(getActivity());
    splitPane.addView(outerControlLayout);
    splitPane.addView(frameLayout);
    frameLayout.addView(operationView, LayoutParams.MATCH_PARENT);
    frameLayout.addView(selection, LayoutParams.WRAP_CONTENT);
    
  /*  FrameLayout.LayoutParams backButtonParams = (FrameLayout.LayoutParams) backButton.getLayoutParams();


    backButtonParams.gravity = Gravity.TOP | Gravity.START; */

    LinearLayout rightControls = new LinearLayout(getActivity());
    rightControls.setOrientation(LinearLayout.VERTICAL);
    
    topRightButtons = new LinearLayout(getActivity());
    rightControls.addView(topRightButtons);
    frameLayout.addView(rightControls);
    FrameLayout.LayoutParams rightControlParams = (FrameLayout.LayoutParams) rightControls.getLayoutParams();
    rightControlParams.gravity = Gravity.END;
    rightControlParams.width = LayoutParams.WRAP_CONTENT;
    rightControlParams.height = LayoutParams.MATCH_PARENT;

    LinearLayout.LayoutParams buttonParams = (LinearLayout.LayoutParams) topRightButtons.getLayoutParams();
    buttonParams.width = LayoutParams.WRAP_CONTENT;
    buttonParams.height = LayoutParams.WRAP_CONTENT;
    topRightButtons.setLayoutParams(buttonParams);

    speedBar = new VerticalSeekBar(platform);
    if (tutorialMode) {
      speedBar.setProgress(operation().tutorialData.speed);
    } else {
      speedBar.setProgress(50);
      ImageView fastButton = new ImageView(platform);
      int padding = Views.px(platform, 12);
      fastButton.setPadding(padding, padding, padding, 0);
      fastButton.setImageResource(R.drawable.ic_fast_forward_white_24dp);
      fastButton.setAlpha(0.5f);
      rightControls.addView(fastButton);
      buttonParams = (LinearLayout.LayoutParams) fastButton.getLayoutParams();
      buttonParams.width = LayoutParams.WRAP_CONTENT;
      buttonParams.gravity = Gravity.END;
      fastButton.setLayoutParams(buttonParams);

      //ClipDrawable d1 = (ClipDrawable) ld.);
      speedBar.getProgressDrawable().setColorFilter(0x0ff888888, android.graphics.PorterDuff.Mode.SRC_IN);
      rightControls.addView(speedBar);
      LinearLayout.LayoutParams barParams = (LinearLayout.LayoutParams)
          speedBar.getLayoutParams();
      barParams.weight = 1;
      barParams.gravity = Gravity.END;
      barParams.width = LayoutParams.WRAP_CONTENT;
      barParams.rightMargin = (int) (8 * pixelPerDp);

      final ImageView pauseButton = new ImageView(platform);
      pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
      pauseButton.setAlpha(0.5f);
      rightControls.addView(pauseButton);
      buttonParams = (LinearLayout.LayoutParams) pauseButton.getLayoutParams();
      buttonParams.width = LayoutParams.WRAP_CONTENT;
      buttonParams.gravity = Gravity.END;
      pauseButton.setLayoutParams(buttonParams);
      pauseButton.setPadding(padding, 0, padding, padding);

      OnClickListener speedButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
          int target = v == pauseButton ? 0 : 100;
          speedBar.setProgress(speedBar.getProgress() == target ? 50 : target);
        }
      };
      fastButton.setOnClickListener(speedButtonListener);
      pauseButton.setOnClickListener(speedButtonListener);
    }
    columnLayout.getLayoutParams().width = LayoutParams.MATCH_PARENT;
    columnLayout.getLayoutParams().height = LayoutParams.WRAP_CONTENT;

    updateMenu();
  }

  void setSelectionMode(boolean on) {
    this.selectionMode = on;
    updateMenu();
    if (on) {
      selection.setVisibility(View.VISIBLE);
      selection.setMode(SelectionView.Mode.CUSTOM);
    //  topRightButtons.setVisibility(View.GONE);
    //  selectionButtons.setVisibility(landscapeMode ? View.VISIBLE : View.GONE);
    } else {
      selection.setVisibility(View.INVISIBLE);
      selection.setMode(SelectionView.Mode.CELL);
    //  topRightButtons.setVisibility(landscapeMode ? View.VISIBLE : View.GONE);
    //  selectionButtons.setVisibility(View.GONE);
    }
  }
  
  
  
  protected void updateLayout() {
    Point screenSize = new Point();
    getActivity().getWindowManager().getDefaultDisplay().getSize(screenSize);
    landscapeMode = screenSize.x > screenSize.y;
    final SplitPaneLayout splitPane = (SplitPaneLayout) rootView;
    splitPane.setOrientation(landscapeMode ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
    //splitPane.setSplitterPositionPercent(0.2499f);

    // TODO(haustein) make this nicer...
    if (landscapeMode) {
      columnLayout.setColumnCount(1, 1);
      fakeActionBar.setVisibility(View.VISIBLE);
      tutorialHelpView.setVisibility(View.VISIBLE);
      topRightButtons.setVisibility(View.VISIBLE);
    } else {
      columnLayout.setColumnCount(2, 2);
      fakeActionBar.setVisibility(View.GONE);
      tutorialHelpView.setVisibility(View.GONE);
      topRightButtons.setVisibility(View.GONE);
    }
    setFullscreen(landscapeMode);

    if (splitPane.getOrientation() == SplitPaneLayout.ORIENTATION_HORIZONTAL) {
      // In horizontal mode, we can't depend on the columnLayout count because we need
      // to show the title and help text even if there are no ports.
      new UiTimerTask(platform) {
        @Override
        public void runOnUiThread() {
          splitPane.setSplitterPositionPercent(0.25f);
        }
      }.schedule(100);
    } else {
      MetaLayout.adjustSplitPaneToColumnLayoutDeferred(platform, columnLayout, splitPane);
    }
  }
  
  

  ImageButton addImageButton(ViewGroup layout, int resId) {
    ImageButton button = new ImageButton(this.getActivity());
    button.setImageResource(resId);
    layout.addView(button);
    ViewGroup.LayoutParams refreshParams = /*(LinearLayout.LayoutParams)*/ button.getLayoutParams();
    refreshParams.height = (int) (48 * pixelPerDp);
    refreshParams.width = (int) (48 * pixelPerDp);
    button.setLayoutParams(refreshParams); 
    return button;
  }


  public CustomOperation operation() {
    return operation;
  }
  
  SelectionView selectionView() {
    return selection;
  }
 
  
  void buildTypeFilter() {
    currentAutoConnectStartRow.clear();
    while (true) {
      int row = selection.row() - 1;
      int col = selection.col() + currentTypeFilter.size();
      
      while (row > selection.row - 3 && operation.cell(row, col) == null) {
        row--;
      }
      
      Type t = operation.getOutputType(row, col, Edge.BOTTOM);
      if (t == null) {
        break;
      }
      currentTypeFilter.add(t);
      currentAutoConnectStartRow.add(row + 1);
    }
  }

  void showPopupMenu(Cell cell) {
    ContextMenu menu = new ContextMenu(platform, selection);
    menu.setHelpProvider(platform);
    if (operation().isTutorial()) {
      menu.setDisabledMap(operation().tutorialData.disabledMenus, !tutorialMode, new Callback<Void>() {
        @Override
        public void run(Void value) {
          operation().save();
        }
      });
    }

    currentTypeFilter.clear();
    if (cell != null) {
      Command cmd = cell.command();
      if (cmd == null) {
        menu.add(MENU_ITEM_DELETE_PATH);
      } else if ((cmd instanceof CustomOperation && cmd != operation)
          || (cmd instanceof PropertyCommand)
          || (cmd instanceof PortCommand && !((PortCommand) cmd).peerJson().containsKey("sensor"))
          || (cmd instanceof LocalCallCommand && ((LocalCallCommand) cmd).operation() != operation) ||
          (cmd instanceof ConstructorCommand)) {
        menu.add(MENU_ITEM_EDIT_CELL);
      }
      menu.add(MENU_ITEM_CLEAR_CELL);
    } else {
      buildTypeFilter();
    }      
    operationView.invalidate();

    Cell cellBelow = operation.cell(selection.row + 1, selection.col);
    boolean hasInput = currentTypeFilter.size() > 0;
    boolean showConstantMenu = !hasInput || (cellBelow != null && cellBelow.command() != null);

    if (cell == null || showConstantMenu) {
      ContextMenu ioMenu = menu.addSubMenu(MENU_ITEM_DATA_IO).getSubMenu();
      if (ioMenu != null) {
        ioMenu.add(MENU_ITEM_CONSTANT_VALUE);
        if (cell == null) {
          if (!hasInput) {
            ioMenu.add(MENU_ITEM_INPUT_FIELD);
          }
          ioMenu.add(MENU_ITEM_COMBINED_FIELD);
          if (!hasInput) {
            ContextMenu sensorMenu = ioMenu.addSubMenu("Sensor\u2026").getSubMenu();
            for (String s: SENSOR_MAP.keySet()) {
              sensorMenu.add(s);
            }
          }
          ContextMenu outputMenu = ioMenu.addSubMenu("Output\u2026").getSubMenu();
          outputMenu.add(MENU_ITEM_OUTPUT_FIELD);
          outputMenu.add(MENU_ITEM_CANVAS);
          outputMenu.add(MENU_ITEM_HISTOGRAM);
          outputMenu.add(MENU_ITEM_PERCENT_BAR);
          outputMenu.add(MENU_ITEM_RUN_CHART);
          outputMenu.add(MENU_ITEM_WEB_VIEW);

          ContextMenu firmataMenu = ioMenu.addSubMenu("Firmata\u2026").getSubMenu();
          firmataMenu.add(MENU_ITEM_FIRMATA_ANALOG_INPUT);
          firmataMenu.add(MENU_ITEM_FIRMATA_ANALOG_OUTPUT);
          firmataMenu.add(MENU_ITEM_FIRMATA_DIGITAL_INPUT);
          firmataMenu.add(MENU_ITEM_FIRMATA_DIGITAL_OUTPUT);
          firmataMenu.add(MENU_ITEM_FIRMATA_SERVO_OUTPUT);
        
          ContextMenu testMenu = ioMenu.addSubMenu("Test\u2026").getSubMenu();
          testMenu.add(MENU_ITEM_TEST_INPUT);
          testMenu.add(MENU_ITEM_EXPECTATION);
        }
      }
    }
    
    if (cell == null) {
      menu.add(MENU_ITEM_CONTROL);
      
      if (operation.classifier != null) {
        menu.add(MENU_ITEM_THIS_CLASS);
      }
      if (operation.module().parent() != null) {
        menu.add(MENU_ITEM_THIS_MODULE);
      }
      if (currentTypeFilter.size() > 0 && (currentTypeFilter.get(0) instanceof Classifier)) {
        Classifier classifier = (Classifier) currentTypeFilter.get(0);
        menu.add(classifier.name() + "\u2026");
      }

      menu.add(MENU_ITEM_OPERATIONS_CLASSES);
    }
    
    if (cellBelow != null && cellBelow.command() != null) {
      int index = selection.col - cellBelow.col();
      if (index < cellBelow.inputCount()) {
        if (cellBelow.isBuffered(index)) {
          menu.add(MENU_ITEM_REMOVE_BUFFER);
        } else {
          menu.add(MENU_ITEM_ADD_BUFFER);
        }
      }
    }

    ContextMenu editMenu = menu.addSubMenu(MENU_ITEM_EDIT).getSubMenu();
    if (editMenu != null) {
      ContextMenu.Item pasteItem = editMenu.add(MENU_ITEM_PASTE);
      pasteItem.setEnabled(platform.editBuffer() != null);
      editMenu.add(MENU_ITEM_INSERT_ROW);
      editMenu.add(MENU_ITEM_INSERT_COLUMN);
      editMenu.add(MENU_ITEM_DELETE_ROW);
      editMenu.add(MENU_ITEM_DELETE_COLUMN);
    }
    
    menu.setOnMenuItemClickListener(this);
    menu.show();
  }
  
  @Override
  void timerTick() {
    if (running && controller.pendingCount() == 0 &&
        (operation.isTutorial() || !operation.asyncInput())) {
      inactiveTicks += 2;
      if (inactiveTicks == 10) {
        boolean allSent = true;
        for (Port port: ports()) {
          if (port instanceof  TestPort) {
            TestPort test = (TestPort) port;
            if (test.outputPending()) {
              allSent = false;
            }
          }
        }
        if (!allSent) {
          inactiveTicks = 0;
        } else if (operation.isTutorial()) {
          Log.d(TAG, "scheduling check tutorial success");
          new Timer().schedule(new UiTimerTask(platform) {
            @Override
            public void runOnUiThread() {
              checkTutorialSuccess();
            }
          }, 0); 
        } else {
          stop();
        }
      }
    } else {
      inactiveTicks = 0;
    }

    // This is at the end because it may cause pending activity, preventing tutorial end.
    super.timerTick();
    operationView.postInvalidate();
  }
}
