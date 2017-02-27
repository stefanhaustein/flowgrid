package org.flowgrid.swt.operation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.flowgrid.model.PortFactory;
import org.flowgrid.swt.ArtifactEditor;
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
import org.flowgrid.swt.ResourceManager;
import org.flowgrid.swt.Strings;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.UiTimerTask;
import org.flowgrid.swt.data.DataComponent;
import org.flowgrid.swt.data.PropertyComponent;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;
import org.flowgrid.swt.port.PortManager;
import org.flowgrid.swt.port.TestPort;
import org.flowgrid.swt.port.WidgetPort;
import org.flowgrid.swt.widget.ColumnLayout;
import org.flowgrid.swt.widget.ContextMenu;
import org.flowgrid.swt.widget.WrappingLabelCage;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class OperationEditor extends ArtifactEditor implements PortManager {

    static String portType(PortCommand portCommand) {
        return portType(portCommand.peerJson());
    }

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
    boolean fullScreen;


    public OperationEditor(final SwtFlowgrid flowgrid, final CustomOperation operation, boolean addTitle) {
        this.flowgrid = flowgrid;
        this.operation = operation;
        this.fullScreen = addTitle;

        tutorialMode = operation.isTutorial();

//        flowgrid.shell().setText(operation.name() + " - FlowGrid");

        classifier = operation.classifier;
        instance = classifier != null ? new Instance(classifier) : null;
        controller = new Controller(operation);
        controller.setInstance(this.instance);


        // operation.ensureLoaded(); // Fixme
        operation.validate();

        // UI Setup

        flowgrid.shell().setLayout(new ColumnLayout(25));

        if (addTitle) {
            Composite scrollParent = new Composite(flowgrid.shell(), SWT.NONE);
            GridLayout scrollParentLayout = new GridLayout();
            scrollParentLayout.marginWidth = 0;
            scrollParentLayout.marginHeight = 0;
            scrollParent.setLayout(scrollParentLayout);

            Composite topBar = new Composite(scrollParent, SWT.NONE);
            topBar.setBackground(flowgrid.resourceManager.blues[2]);
            GridData topBarLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
            topBar.setLayoutData(topBarLayoutData);

            GridLayout topBarLayout = new GridLayout(2, false);
            topBarLayout.marginHeight = 0;
            topBarLayout.marginWidth = 0;
            topBar.setLayout(topBarLayout);

            Button menuButton = new Button(topBar, SWT.PUSH|SWT.FLAT);
            menuButton.setImage(flowgrid.resourceManager.getIcon(ResourceManager.Icon.MENU));

            Label label = new Label(topBar, SWT.NONE);
            label.setText(operation.name());
            label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

            menuButton.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    flowgrid.shell().getMenuBar().setVisible(true);
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    widgetSelected(e);
                }
            });

            scrolledComposite = new ScrolledComposite(scrollParent, SWT.V_SCROLL);
            scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        } else {
            scrolledComposite = new ScrolledComposite(flowgrid.shell(), SWT.V_SCROLL);
        }


        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
//
        controlPanel = new Composite(scrolledComposite, SWT.NONE);

        final GridLayout controlLayout = new GridLayout(1, false);
        controlLayout.marginHeight = 0;
        controlLayout.marginWidth = 0;
        controlPanel.setLayout(controlLayout);

        scrolledComposite.setContent(controlPanel);

        controller.setVisual(true);
        operationCanvas = new OperationCanvas(this, flowgrid.shell());

        GridData canvasGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        canvasGridData.horizontalSpan = 3;
        operationCanvas.setLayoutData(canvasGridData);

        attachAll();

        if (classifier != null) {
            final Button classifierButton = new Button(controlPanel, SWT.PUSH | SWT.FLAT);
            classifierButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            classifierButton.setText(classifier.name());
            classifierButton.addSelectionListener(new DefaultSelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    flowgrid.openClassifier(classifier);
                }
            });

            for (Property property: classifier.properties(null)) {
                DataComponent input = new PropertyComponent(controlPanel, flowgrid, property, instance);
                input.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            }
        }

        Composite labelCage = new Composite(controlPanel, SWT.NONE);
        labelCage.setLayout(new WrappingLabelCage());
        labelCage.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        final Label documentationLabel = new Label(labelCage, SWT.WRAP);
        final Runnable updateDocumentation = new Runnable() {
            public void run() {
                String documentation = operation.documentation();
                if (documentation == null || documentation.trim().isEmpty()) {
                    documentation = "(no documentation)";
                }
                documentationLabel.setText(documentation);
            }
        };
        documentationLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                if (!tutorialMode) {
                    editDocumentation(updateDocumentation);
                }
            }
        });
        updateDocumentation.run();
        operationCanvas.autoZoom();
    }


    @Override
    public void addInput(WidgetPort inputPort) {
        int pos = 0;
        for (Cell cell: operation) {
            if (cell.command() instanceof PortCommand) {
                PortCommand portCommand = (PortCommand) cell.command();
                String portType = portType(portCommand);
                if (!portType.equals("Test") && !portType.equals("Sensor")) {
                    if (inputPort.port() == portCommand) {
                        break;
                    }
                    pos++;
                }
            }
        }

        Control[] children = controlPanel.getChildren();
        Control insertBefore = pos >= children.length ? null : children[pos];
/*
        View view = input.view();
        int colSpan = input.port.peerJson().getInt("width", 1);
        int rowSpan = input.port.peerJson().getInt("height", 1);
        controlLayout.addView(pos, view, colSpan, rowSpan); */

        Control control = inputPort.createControl(controlPanel);
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        if (insertBefore != null) {
            control.moveBelow(insertBefore);
        }
        controlPanel.layout();
    }


    Port attachPort(PortCommand portCommand) {
        Port result;

        String portType = portType(portCommand);

        PortFactory portFactory = flowgrid().model().portFactory(portType);

        if (portFactory != null) {
            result = portFactory.create(controller, portCommand);
        } else

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
                    new Label(alert.getContentContainer(), SWT.NONE).setText("The program did not generate the expected output. Consider the help text and try again!");
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
                        new Label(alert.getContentContainer(), SWT.NONE).setText("Used cell units: " + counted + "\n" +
                                                "Optimal cell units: " + tutorialData.optimalCellCount);
                        alert.show();
                    }
                }
            }.schedule(100, 100);
        } else {
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
                updateMenu();  // Includes updateButtons();
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
                updateMenu();  // includes updateButtons
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

    public void fillMenu(Menu menu) {
        if (!fullScreen) {
            fillMenuImpl(menu);
        }
    }

    public void fillMenuImpl(Menu menu) {
        ContextMenu operationMenu = new ContextMenu(menu);
        operationMenu.setOnMenuItemClickListener(this);

        /*
        SpannableString title = new SpannableString("\u2039 " + operation().name());
        if (operation().asyncInput()) {
            title.setSpan(new UnderlineSpan(), 2, title.length(), 0);
        }
        if (artifact.isPublic()) {
            title.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 2, title.length(), 0);
        }

        fakeActionBar.setText(title);
        setArtifact(operation());  // Updates the action bar.
*/
        if (!operation.isTutorial()) {
            /*
            operationMenu.addItem(Strings.MENU_ITEM_RUN_MODE);
            if (operation.name().equals("main")) {
                operationMenu.addItem(Strings.MENU_ITEM_CREATE_SHORTCUT);
            }
            */
            operationMenu.addCheckable(Strings.MENU_ITEM_PUBLIC).setChecked(operation.isPublic());
            operationMenu.addCheckable(Strings.MENU_ITEM_CONTINUOUS_INPUT).setChecked(operation.asyncInput());
        } else if (!tutorialMode) {
            operationMenu.add(Strings.MENU_ITEM_TUTORIAL_SETTINGS);
        }

        if (operation.isTutorial() && flowgrid.settings().developerMode()) {
            operationMenu.addCheckable(Strings.MENU_ITEM_TUTORIAL_MODE).setChecked(tutorialMode);
        }

        if (operation.isTutorial()) {
            operationMenu.add(Strings.MENU_ITEM_RESET);
        }

        if (!tutorialMode) {
           if (operation().classifier == null) {
              operationMenu.add(Strings.MENU_ITEM_RENAME_MOVE);
            } else {
              operationMenu.add(Strings.MENU_ITEM_RENAME);
            }
            operationMenu.add(Strings.MENU_ITEM_DELETE);
        }
    }

    @Override
    public String getMenuTitle() {
        return "Operation";
    }

    @Override
    public CustomOperation getArtifact() {
        return operation;
    }


    protected void updateMenu() {
        operationCanvas.updateButtons();
        flowgrid.updateMenu();
    }


    @Override
    public boolean onContextMenuItemClick(ContextMenu.Item menuItem) {
        String label = menuItem.getTitle();
        if (label.equals(Strings.MENU_ITEM_TUTORIAL_MODE)) {
            tutorialMode = !menuItem.isChecked();
            updateMenu();
            return true;
        }
        if (label.equals(Strings.MENU_ITEM_RESET)) {
            if (operation.isTutorial()) {
                TutorialData tutorialData = operation.tutorialData;
                operationCanvas.beforeChange();
                operation.clear(tutorialData.editableStartRow, 0, tutorialData.editableEndRow - tutorialData.editableStartRow, Integer.MAX_VALUE / 2);
                if (flowgrid.settings().developerMode()) {
                    tutorialData.passedWithStars = 0;
                }
                operationCanvas.afterChange();
            }
            return true;
        }
        if (label.equals(Strings.MENU_ITEM_TUTORIAL_SETTINGS)) {
            TutorialSettingsDialog.show(this);
            return true;
        }
        if (label.equals(Strings.MENU_ITEM_RUN_MODE)) {
            flowgrid.openOperation(operation, false);
            return true;
        }
        if (label.equals(Strings.MENU_ITEM_CONTINUOUS_INPUT)) {
            operationCanvas.beforeChange();
            operation.setAsyncInput(!operation.asyncInput());
            operationCanvas.afterChange();
            operationCanvas.updateButtons();
            return true;
        }
        if (Strings.MENU_ITEM_PUBLIC.equals(label)) {
            operationCanvas.beforeChange();
            operation.setPublic(!operation.isPublic());
            operationCanvas.afterChange();
            operationCanvas.updateButtons();
            return true;
        }
        if (label.equals(Strings.MENU_ITEM_CREATE_SHORTCUT)) {
            System.out.println("FIXME: createShortcut();");                               // FIXME
            return true;
        }
        return super.onContextMenuItemClick(menuItem);
    }
}
