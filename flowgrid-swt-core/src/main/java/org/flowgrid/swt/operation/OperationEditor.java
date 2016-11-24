package org.flowgrid.swt.operation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Cell;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.Controller;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Instance;
import org.flowgrid.model.Port;
import org.flowgrid.model.Property;
import org.flowgrid.model.TutorialData;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.swt.DefaultSelectionAdapter;
import org.flowgrid.swt.Strings;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.UiTimerTask;
import org.flowgrid.swt.data.DataWidget;
import org.flowgrid.swt.data.PropertyWidget;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;
import org.flowgrid.swt.port.PortManager;
import org.flowgrid.swt.port.TestPort;
import org.flowgrid.swt.port.WidgetPort;
import org.flowgrid.swt.widget.MenuAdapter;
import org.flowgrid.swt.widget.MenuSelectionHandler;
import org.flowgrid.swt.widget.Widget;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class OperationEditor implements PortManager, MenuSelectionHandler {

    static String portType(HutnObject peerJson) {
        String portType = peerJson.getString("portType", "");
        if (!portType.isEmpty()) {
            return portType;
        }
        if (peerJson.containsKey("sensor")) {
            return "Sensor";
        }
        if (peerJson.containsKey("pin")) {
            return "Ioio";
        }
        if (peerJson.containsKey("testData")) {
            return "Test";
        }
        return "Ui";
    }


    SwtFlowgrid flowgrid;
    CustomOperation operation;
    Instance instance;
    Classifier classifier;
    Controller controller;
    Composite controlPanel;
    int inactiveTicks;
    OperationCanvas operationCanvas;
    boolean tutorialMode;
    public int counted;
    public int countedToRow;
    boolean running;
    ScrolledComposite scrolledComposite;
    Timer timer;
    MenuAdapter menuAdapter = new MenuAdapter(this);
    boolean landscapeMode = true;


    public OperationEditor(final SwtFlowgrid flowgrid, CustomOperation operation) {
        this.flowgrid = flowgrid;
        this.operation = operation;

        flowgrid.shell().setText(operation.name() + " - FlowGrid");

        classifier = operation.classifier;
        instance = classifier != null ? new Instance(classifier) : null;
        controller = new Controller(operation);
        controller.setInstance(this.instance);


        // operation.ensureLoaded(); // Fixme
        operation.validate();

        // UI Setup

        final GridLayout shellLayout = new GridLayout(4, true);
        shellLayout.marginWidth = 0;
        shellLayout.marginHeight = 0;
        flowgrid.shell().setLayout(shellLayout);

        scrolledComposite = new ScrolledComposite(flowgrid.shell(), SWT.V_SCROLL);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        controlPanel = new Composite(scrolledComposite, SWT.NONE);

        final GridLayout controlLayout = new GridLayout(1, false);
        controlLayout.marginHeight = 0;
        controlLayout.marginWidth = 0;
        controlPanel.setLayout(controlLayout);
       // Label label = new Label(controlPanel, SWT.NONE);
        //label.setText(operation.name());


        if (classifier != null) {
            final Button classifierButton = new Button(controlPanel, SWT.PUSH);
            classifierButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            classifierButton.setText(classifier.name());
            classifierButton.addSelectionListener(new DefaultSelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    flowgrid.openClassifier(classifier);
                }
            });
            /*
            TextView separator = new TextView(platform);
            separator.setText(classifier.name() + " properties");
            Views.applyEditTextStyle(separator, false);
            separator.setPaintFlags(separator.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            controlLayout.addView(separator);
            */
            for (Property property: classifier.properties(null)) {
                DataWidget input = new PropertyWidget(flowgrid, property, instance);
                //Control control =
                input.createControl(controlPanel).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
                //                controlLayout.addView(view);
              //  propertyWidgets.add(input);
            }
        }



        scrolledComposite.setContent(controlPanel);


        controller.setVisual(true);
        operationCanvas = new OperationCanvas(this, flowgrid.shell());

        GridData canvasGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        canvasGridData.horizontalSpan = 3;
        operationCanvas.setLayoutData(canvasGridData);

        attachAll();

        for (int i = 0; i < 10; i++) {
            System.out.println("");
        }

       // flowgrid.shell().layout(true, true);  // FIXME


        updateMenu();
    }


    @Override
    public void addInput(WidgetPort inputPort) {
       /* int pos = 0;
        for (;pos < controlLayout.getChildCount(); pos++) {
            View view = controlLayout.getChildAt(pos);
            WidgetPort other = findWidgetPort(view);
            if (other == null || other.port.cell().compareTo(input.port.cell()) > 0) {
                break;
            }
        }
        View view = input.view();
        int colSpan = input.port.peerJson().getInt("width", 1);
        int rowSpan = input.port.peerJson().getInt("height", 1);
        controlLayout.addView(pos, view, colSpan, rowSpan); */

        inputPort.createControl(controlPanel).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    }


    Port attachPort(PortCommand portCommand) {
        Port result;

        String portType = portType(portCommand.peerJson());
        /*
        if (portType.equals("Sensor")) {
            result = new SensorPort(this, portCommand);
        } else*/ if (portType.equals("Test")) {
            result = new TestPort(this, portCommand);
     /*   } else if (portType.equals("Firmata")) {
            result = new FirmataPort(this, portCommand); */
        } else {
            result = new WidgetPort(this, portCommand);
        }
        // ports.add(result);
        portCommand.setPort(result);
        return result;
    }

    void attachAll() {
        detachAll();
        for (Cell cell: operation) {
            if (cell.command() instanceof PortCommand) {
                attachPort((PortCommand) cell.command());
            }
        }
        operation.validate();

 //       controlPanel.layout();

        Point minSize = controlPanel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        System.out.println("****attachAll: control panel min size: " + minSize);
        scrolledComposite.setMinSize(minSize);

        flowgrid.shell().layout(true, true);
    }

    void checkTutorialSuccess() {
        final TutorialData tutorialData = operation.tutorialData;

        final AlertDialog alert = new AlertDialog(flowgrid.shell());
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
                        if (it.next() == operation) {
                            if (it.hasNext()) {
                                Artifact nextArtifact = it.next();
                                // Might be main.png, so we need to be careful here.
                                if (nextArtifact instanceof CustomOperation) {
                                    next = (CustomOperation) nextArtifact;
                                }
                            }
                            break;
                        }
                    }
                    if (next != null) {
                        flowgrid.openOperation(next, true);
                    } else {
                        System.out.println("FIXME: navigateUp();");    // FIXME
                    }
                }
            });
            this.countedToRow = tutorialData.editableStartRow;

//            operationView.postInvalidate();
            operationCanvas.redraw();

            new UiTimerTask(flowgrid.display()) {
                @Override
                public void runOnUiThread() {
                    if (countedToRow < tutorialData.editableEndRow) {
                        countedToRow++;
                        operationCanvas.redraw();
                    } else {
                        cancel();

                        operationCanvas.beforeChange();
                        tutorialData.passedWithStars = counted <= tutorialData.optimalCellCount ? 3
                                : counted <= tutorialData.optimalCellCount * 4 / 3 ? 2 : 1;
                        operationCanvas.afterChange();
                        alert.setTitle((tutorialData.passedWithStars == 3 ? "Perfect " : "Success ") + "\u2b50\u2b50\u2b50".substring(0, tutorialData.passedWithStars));
                        alert.setMessage("Used cell units: " + counted + "\n" +
                                "Optimal cell units: " + tutorialData.optimalCellCount);
                        alert.show();
                    }
                }
            }.schedule(100, 100);
        } else {
            if (operation.hasDocumentation() // && !landscapeMode                     FIXME
                 ) {
                alert.setNeutralButton("Help", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OperationHelpDialog.show(OperationEditor.this);
                    }
                });
            }
            alert.show();
        }
    }

    @Override
    public Controller controller() {
        return controller;
    }

    public void detachAll() {
        for (PortCommand portCommand : operation.portCommands()) {
            portCommand.detach();
        }
    }


    @Override
    public SwtFlowgrid flowgrid() {
        return flowgrid;
    }

    /**
     * Checks whether all input is available. If the missing parameter is
     * not null, missing input names will be accumulated there, and
     * Missing fields will be highlighted.
     */
    boolean isInputComplete(StringBuilder missing) {
        boolean checkOnly = missing == null;
        if (checkOnly) {
            missing = new StringBuilder();
        }
        for (WidgetPort widget: portWidgets()) {
            if (widget.port.input && widget.value() == null) {
                if (missing.length() > 0) {
                    missing.append(", ");
                }
                missing.append(widget.port.name());
                /*
                if (!checkOnly) {
                    widget.view().setBackgroundColor(0x088ff0000);         FIXME
                }
                */
            }
        }
        return missing.length() == 0;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public CustomOperation operation() {
        return operation;
    }


    public Iterable<Port> ports() {
        return new Iterable<Port>() {
            @Override
            public Iterator<Port> iterator() {
                final Iterator<PortCommand> base = operation.portCommands().iterator();
                return new Iterator<Port>() {
                    private Port next;

                    @Override
                    public boolean hasNext() {
                        while (next == null && base.hasNext()) {
                            next = base.next().port();
                        }
                        return next != null;
                    }

                    @Override
                    public Port next() {
                        hasNext();
                        Port result = next;
                        next = null;
                        return result;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            };
        };
    }


    public Iterable<WidgetPort> portWidgets() {
        return new Iterable<WidgetPort>() {
            @Override
            public Iterator<WidgetPort> iterator() {
                return new Iterator<WidgetPort>() {
                    private Iterator<Port> base = ports().iterator();
                    private WidgetPort next;

                    @Override
                    public WidgetPort next() {
                        hasNext();
                        WidgetPort result = next;
                        next = null;
                        return result;
                    }

                    @Override
                    public boolean hasNext() {
                        if (next != null) {
                            return true;
                        }
                        while (base.hasNext()) {
                            Port n = base.next();
                            if (n instanceof  WidgetPort) {
                                next = (WidgetPort) n;
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public void remove() {
                        base.remove();
                    }
                };
            };
        };
    }

    @Override
    public void removeWidget(Widget widget) {
        System.out.println("TBD: OperationEditor.removeWidget()");
    }

    void resetTutorial() {
        countedToRow = -1;
        start();
        stop();
        operationCanvas.redraw();
    }


    @Override
    public void start() {
        if (!operation.asyncInput() && !isInputComplete(null)) {
            return;
        }

        if (running) {
            stop();
        }

        inactiveTicks = 0;
        if (controller != null) {
            controller.start();
        }
        running = true;
        timer = new Timer();
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            timerTick();
                        } catch (Exception e) {
                            flowgrid.error("TimerTick", e);
                        }
                    }
                }, 33, 33);

        for (Port port : ports()) {
            if (operation.asyncInput()) {
                port.start();
            } else {
                port.ping();
            }
        }

        flowgrid.display().asyncExec(new Runnable() {
            @Override
            public void run() {
                updateMenu();
            }
        });
    }

    void stop() {
        if (!running) {
            return;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (operation.asyncInput()) {
            for (Port port: ports()) {
                port.stop();
            }
        }
        if (controller != null) {
            controller.stop();
        }
        running = false;
        flowgrid.display().asyncExec(new Runnable() {
            @Override
            public void run() {
                updateMenu();
                operationCanvas.redraw();
            }
        });
    }

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
//                    Log.d(TAG, "scheduling check tutorial success");
                    new Timer().schedule(new UiTimerTask(flowgrid.display()) {
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

        // This is at the end because it may cause pending activity, preventing tutorial end.
        for (Port port : ports()) {
            port.timerTick(2);
        }
        flowgrid.display().asyncExec(new Runnable() {
            @Override
            public void run() {
                operationCanvas.redraw();
            }
        });
    }


    void fillArtifactMenu(Menu menu) {
        menuAdapter.addItem(menu, running ? Strings.MENU_ITEM_STOP : Strings.MENU_ITEM_START);
    }


    protected void updateMenu() {
        operationCanvas.updateButtons();

        Menu menuBar = flowgrid.createMenuBar();
        MenuItem operationMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        operationMenuItem.setText("Operation");
        Menu operationMenu = new Menu(operationMenuItem);
        fillArtifactMenu(operationMenu);
        flowgrid.shell().setMenuBar(menuBar);

        /*
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
        } */
    }


    @Override
    public void menuItemSelected(MenuItem menuItem) {
        String label = menuItem.getText();
        if (Strings.MENU_ITEM_STOP.equals(label)) {
            stop();
        } else if (Strings.MENU_ITEM_START.equals(label)) {
            start();
        }
    }
}
