package org.flowgrid.swt.operation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.flowgrid.model.*;
import org.flowgrid.model.api.ConstructorCommand;
import org.flowgrid.model.api.LocalCallCommand;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.model.api.PropertyCommand;
import org.flowgrid.model.hutn.HutnWriter;
import org.flowgrid.swt.Colors;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.widget.ContextMenu;
import sun.rmi.runtime.Log;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class OperationCanvas extends Canvas implements ContextMenu.ItemClickListener {
    static final float ZOOM_STEP = 1.1f;

    // Move elsewhere?
    protected static final String MENU_ITEM_CANCEL = "Cancel";
    protected static final String MENU_ITEM_ADD_DOCUMENTATION = "Add documentation";
    protected static final String MENU_ITEM_COPY = "Copy";
    protected static final String MENU_ITEM_CUT = "Cut";
    protected static final String MENU_ITEM_DELETE = "Delete";
    protected static final String MENU_ITEM_DOCUMENTATION = "Documentation";
    protected static final String MENU_ITEM_PLAY = "Play";
    protected static final String MENU_ITEM_PUBLIC = "Public";
    protected static final String MENU_ITEM_RENAME = "Rename";
    protected static final String MENU_ITEM_RENAME_MOVE = "Rename / move";
    protected static final String MENU_ITEM_RESTART = "Restart";
    protected static final String MENU_ITEM_RESTORE = "Restore";
    protected static final String MENU_ITEM_STOP = "Stop";


    static final String MENU_ITEM_CANVAS = "Canvas";
    static final String MENU_ITEM_COMBINED_FIELD = "Combined field";
    static final String MENU_ITEM_CONSTANT_VALUE = "Constant value\u2026";
    static final String MENU_ITEM_CONTINUOUS_INPUT = "Continuous input";
    static final String MENU_ITEM_CLEAR_CELL = "Clear cell";
    static final String MENU_ITEM_CREATE_SHORTCUT = "Create shortcut";
    static final String MENU_ITEM_DATA_IO = "Data / IO\u2026";
    static final String MENU_ITEM_DELETE_COLUMN = "Delete column";
    static final String MENU_ITEM_DELETE_PATH = "Delete path";
    static final String MENU_ITEM_DELETE_ROW = "Delete row";
    static final String MENU_ITEM_EDIT = "Edit\u2026";
    static final String MENU_ITEM_EDIT_CELL = "Edit cell";
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


    float startX;
    float startY;
    float lastX;
    float lastY;
    Edge lastEdge;
    int pointerId;
    int lastCol;
    int lastRow;
    boolean dragging;
    boolean moved;
    boolean changed;
    boolean mayScroll;
    boolean scroll;
    boolean armed;

    float originX;
    float originY;
    float cellSize = 32;

    float initialCellSize;
    float connectorRadius;

    ScaledGraphElements sge;

    OperationEditor operationEditor;
    //private Controller controller;

    Set<Cell> cellsReady = new HashSet<Cell>();
    private float lastFocusX;
    private float lastFocusY;
    boolean autoZoom = true;
    private ZoomData zoomData;

    CustomOperation operation;
    Selection selection = new Selection();
    ContextMenu menu;
    ArrayList<Type> currentTypeFilter = new ArrayList<>();
    ArrayList<Integer> currentAutoConnectStartRow = new ArrayList<>();
    Label menuAnchor;
    Button playButton;
    boolean changing;
    SwtFlowgrid flowgrid;
    ArrayList<String> undoHistory = new ArrayList<>();

    Slider speedBar;

    public OperationCanvas(final OperationEditor operationEditor, Composite parent) {
        super(parent, SWT.RIGHT_TO_LEFT);
        this.operationEditor = operationEditor;
        this.operation = operationEditor.operation;
        this.flowgrid = operationEditor.flowgrid;
        this.sge = new ScaledGraphElements(operationEditor.flowgrid.display(), operationEditor.flowgrid.colors, operationEditor.flowgrid.model());

        operation.ensureLoaded();  // FIXME

        setLayout(new RowLayout());

        speedBar = new Slider(this, SWT.HORIZONTAL);

        playButton = new Button(this, SWT.PUSH);
        playButton.setText("\u25b6");
        playButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                operationEditor.controller.start();
            }
        });

        menuAnchor = new Label(this, SWT.NONE);

        addMouseListener(new MouseListener() {
            boolean armed = false;
            @Override
            public void mouseDoubleClick(MouseEvent e) {
            }

            @Override
            public void mouseDown(MouseEvent e) {

                System.out.println(playButton.toString());

                armed = true;
                autoZoom = false;
                changed = false;
                dragging = false;
                moved = false;
                scroll = false;
                selection.setVisibility(false);

                startX = lastX = e.x;
                startY = lastY = e.y;

                float absX = e.x + originX;
                float absY = e.y + originY;

                float bestD2 = 9e9f;

                int touchCol = (int) Math.floor(absX / cellSize);
                int touchRow = (int) Math.floor(absY / cellSize);

                selection.setPosition(touchRow, touchCol, 1, 1);

                for (int row = touchRow - 1; row <= touchRow + 1; row++) {
                    for (int col = touchCol - 1; col <= touchCol + 1; col++) {
                        for (Edge edge: Edge.values()) {
                            if (operation().hasOutputConnector(row, col, edge) &&
                                    !operation().hasInputConnector(row + edge.row, col + edge.col, edge.opposite())) {
                                float conX = col * cellSize + sge.xOffset(edge);
                                float conY = row * cellSize + sge.yOffset(edge);
                                float dx = conX - absX;
                                float dy = conY - absY;
                                float d2 = dx * dx + dy * dy;
                                if (d2 < bestD2) {
                                    lastCol = col + edge.col;
                                    lastRow = row + edge.row;
                                    dragging = true;
                                    lastEdge = edge.opposite();
                                    bestD2 = d2;
                                }
                            }
                        }
                    }
                }
                // Dragging ends when we have connected something -- and we don't want to scroll in this
                // case. So we track this in a separate variable.
                mayScroll = !dragging;
            }

            @Override
            public void mouseUp(MouseEvent e) {
                if (!armed) {
                    return;
                }
                armed = false;
                if (moved) {
                    if (scroll) {
//                        onScaleEnd(null);   FIXME
                    } else if (changed) {
  //                      fragment.afterChange();    FIXME
                    }
                    changed = false;
                } else {
                    int col = (int) Math.floor((lastX + originX) / cellSize);
                    int row = (int) Math.floor((lastY + originY) / cellSize);

                    Cell cell = operation().cell(row, col);
                    if (cell != null) {
                        selection.setPosition(cell.row(), cell.col(), 1, cell.width());
                    } else {
                        selection.setPosition(row, col, 1, 1);
                    }
                    selection.setVisibility(true);
                    if (inRange(row, col)) {
                        showPopupMenu(cell);
                    }
                }
                pointerId = -1;
            }
        });

        HutnWriter writer = new HutnWriter(new StringWriter());
        writer.startObject();
        operation.toJson(writer, Artifact.SerializationType.FULL);
        writer.endObject();
        undoHistory.add(writer.close().toString());
    }

    void attachAll() {
        System.out.println("TBD: attachAll");   // FIXME
    }

    void detachAll() {
        System.out.println("TBD: detachAll");   // FIXME
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
            flowgrid.log("beforeChange called with changing = true");
        }
        changing = true;
        stop();
    }

    void afterChange() {
        if (!changing) {
            flowgrid.log("afterChange called with changing = false");
        }
        selection.setVisibility(false);

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
            flowgrid.log("push json: " + currentJson);
            undoHistory.add(currentJson);

            // operationView.invalidate(); FIXME

            operation.save();
            operation.validate();

            updateLayout();

            if (undoHistory.size() == 2) {
                updateMenu();
            }
        }
    }



    CustomOperation operation() {
        return operation;
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

    @Override
    public void drawBackground(GC gc, int x, int y, int width, int height) {
        sge.setState(originX, originY, cellSize);

        Colors colors = operationEditor.flowgrid.colors;

        gc.setAntialias(SWT.ON);
        // gc.setTextAntialias(SWT.ON);  // FIXME
        gc.setLineJoin(SWT.JOIN_ROUND);
        gc.setLineCap(SWT.CAP_ROUND);

        gc.setBackground(colors.background);
        gc.fillRectangle(x, y, width, height);

        gc.setLineWidth(Math.round(cellSize / 32));

        TutorialData tutorialData = operation().tutorialData;
        operationEditor.counted = 0;

        int minRow = 0;
        int maxRow = 0;

        if (tutorialData != null) {
            minRow = tutorialData.editableStartRow;
            maxRow = tutorialData.editableEndRow;
        }

   //     int minY = Math.round(-100 * cellSize - originY - 1);
     //   int maxY = Math.round(100 * cellSize - originY - 1);

        for (int col = -100; col <= 100; col++) {
            gc.setForeground(col == 0 ? colors.origin : colors.grid);
            int lx = Math.round(col * cellSize - originX - 1);
            gc.drawLine(lx, y, lx, y + height);
        }

//        float minX = -100 * cellSize - originX - 1;
 //       float maxX = 100 * cellSize - originX - 1;

        for (int row = -100; row <= 100; row++) {
            gc.setForeground(row == minRow || row == maxRow ? colors.origin : colors.grid);
            int ly = Math.round(row * cellSize - originY - 1);
            gc.drawLine(x, ly, x + width, ly);
        }

        /*                                 FIXME
        if (selection.isShown()) {
            for (int i = 0; i < fragment.currentTypeFilter.size(); i++) {
                Type t = fragment.currentTypeFilter.get(i);
                int r0 = fragment.currentAutoConnectStartRow.get(i);
                float x = (selection.col + i) * cellSize + cellSize / 2 - originX;
                sge.drawConnector(canvas,
                        x, r0 * cellSize - originY,
                        x, selection.row * cellSize - originY,
                        t);
            }
        }
        */

        /*

        if (tutorialData != null && tutorialData.order == 0) {
            float x0 = 1.5f * cellSize - originX;
            float y0 = 4.5f * cellSize - originY;
            downArrowPaint.setStyle(Paint.Style.FILL);
            downArrowPaint.setColor(Color.WHITE);
            downArrowPaint.setTextSize(cellSize);
            downArrowPaint.setTextAlign(Paint.Align.CENTER);
            TextHelper.drawText(getContext(), canvas, "\u21e9", x0, y0, downArrowPaint, TextHelper.VerticalAlign.CENTER);
            if (!fragment.landscapeMode) {
                downArrowPaint.setColor(Color.LTGRAY);
                downArrowPaint.setTextSize(cellSize / 3);
                downArrowPaint.setTextAlign(Paint.Align.LEFT);
                TextHelper.drawText(getContext(), canvas, "rotate for help", x0 + cellSize/2, y0, downArrowPaint, TextHelper.VerticalAlign.CENTER);
            }
        }
*/

        for (Cell cell: operation()) {
            int x0 = Math.round(cell.col() * cellSize - originX);
            int y0 = Math.round(cell.row() * cellSize - originY);

            if (tutorialData != null && cell.row() < operationEditor.countedToRow && cell.row() >= minRow) {
                Color color = (operationEditor.counted < operation().tutorialData.optimalCellCount
                        ? operationEditor.flowgrid.colors.greens[2]
                        : operationEditor.flowgrid.colors.oranges[2]); // & 0x3fffffff; FIXME
                sge.highlightCell(gc, x0, y0, color);
                operationEditor.counted += cell.command() == null ? 1 : 2;
            }

            boolean ready = cell.command() != null && cellsReady.contains(cell);

            sge.drawCell(gc, cell, ready);

            if (cell.command() != null) {
                for (int i = 0; i < cell.inputCount(); i++) {
                    int xi = Math.round(x0 + i * cellSize + cellSize / 2);
                    Object constant = cell.constant(i);
                    if (constant != null) {
                        sge.drawData(gc, xi, y0, constant, true, ready ? 1 : 0);
                    } else if (operationEditor.controller.isRunning()) {
                        Iterator<?> ii = operationEditor.controller.rootEnvironment.peek(cell.dataOffset + i).iterator();
                        if (ii.hasNext()) {
                            Object next = ii.next();
                            if (ii.hasNext()) {
                                Object nextNext = ii.next();
                                if (ii.hasNext()) {
                                    sge.drawData(gc, Math.round(x + cellSize / 3), Math.round(y0 - cellSize / 3), ii.next(), false, -4);
                                }
                                sge.drawData(gc, Math.round(x + cellSize / 6), Math.round(y0 - cellSize / 6), nextNext, false, -2);
                            }
                            sge.drawData(gc, x, y0, next, false, ready ? 1 : 0);
                        }
                    }
                }
            }
        }

        /*
        if (zoomData != null) {
            float x0 = zoomData.cell.col() * cellSize - originX;
            float y0 = zoomData.cell.row() * cellSize - originY;

            canvas.drawRect(x0, y0, x0 + zoomData.cell.width() * cellSize, y0 + cellSize, zoomData.fillPaint);
            // canvas.drawRect(x0, y0, x0 + zoomData.cell.width() * cellSize, y0 + cellSize, zoomData.outlinePaint);

            ScaledGraphElements childSge = new ScaledGraphElements(fragment.platform(), zoomData.operation);
            childSge.setState(-x0, -y0, cellSize / zoomData.rows);
            for (Cell childCell: zoomData.operation) {
                childSge.drawCell(canvas, childCell, false);
            }
        } */

        cellsReady.clear();
        for (VisualData data: operationEditor.controller.getAndAdvanceVisualData(speedBar.getSelection())) {
            drawData(data, gc);
        }

        /*
        if (operationEditor.controller.isRunning()) {
            canvas.drawText(operationEditor.controller.status(), Views.px(getContext(), 12),
                    getHeight() - debugTextPaint.getFontMetrics(null) / 2, debugTextPaint);
        }


        // Update zoom etc.
        if (zoomData != null) {
            float remaining = (zoomData.endTime - System.currentTimeMillis()) / ((float) ZOOM_TIME_MS);
            float completed = 1f - remaining;
            if (completed > 1) {
                originX = zoomData.originalOriginX;
                originY = zoomData.originalOriginY;
                cellSize = zoomData.orginalCellSize;
                fragment.platform().openArtifact(zoomData.operation);
                zoomData = null;
            } else {
                int alpha = Math.min((int) (completed * 1024), 255);
                zoomData.fillPaint.setAlpha(alpha);
                zoomData.outlinePaint.setAlpha(alpha);

                cellSize = zoomData.orginalCellSize * remaining + zoomData.targetCellSize * completed;
                originX = zoomData.originalOriginX * remaining + zoomData.targetOriginX * completed;
                originY = zoomData.originalOriginY * remaining + zoomData.targetOriginY * completed;
                postInvalidate();
            }
            sge.setState(originX, originY, cellSize);
        }

        */

        int selectionX = sge.screenX(selection.col);
        int selectionY = sge.screenX(selection.row);

        gc.setBackground(colors.selection);
        gc.fillRectangle(selectionX, selectionY,
                Math.round(cellSize * selection.width), Math.round(cellSize * selection.height));
    }

    private void drawData(VisualData data, GC gc) {
        float progress = data.progress();
        if (progress < 0) {
            return;
        }
        int row = data.row();
        int col = data.col();
        Edge edge = data.edge();

        Cell ready = data.cellReady();
        if (ready != null) {
            cellsReady.add(ready);
        }

        Cell cell = operation().cell(row, col);
        if (cell == null) {
            return;
        }
        Edge target = cell.connection(edge);
        if (target == null) {
            sge.drawData(gc, Math.round(col * cellSize + cellSize / 2 - originX),
                    Math.round(row * cellSize - originY), data.value(), false, ready != null ? 1 : 0);
            return;
        }

        int x = Math.round(col * cellSize + sge.xOffset(edge) * (1-progress) + sge.xOffset(target) * progress - originX);
        int y = Math.round(row * cellSize + sge.yOffset(edge) * (1-progress) + sge.yOffset(target) * progress - originY);
        sge.drawData(gc, x, y, data.value(), false, 0);
    }



    private boolean inRange(int row, int col) {
        return !operationEditor.tutorialMode ||
                (row >= operation().tutorialData.editableStartRow &&
                        row < operation().tutorialData.editableEndRow && col >= 0);
    }

    void showPopupMenu(Cell cell) {
        if (menu != null) {
            menu.dispose();
        }
        menu = new ContextMenu(menuAnchor);
        // menu.setHelpProvider(platform);
     /*   if (operation.isTutorial()) {                                     // FIXME
            menu.setDisabledMap(operation().tutorialData.disabledMenus, !tutorialMode, new Callback<Void>() {
                @Override
                public void run(Void value) {
                    operation().save();
                }
            });
        } */


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

        // operationView.invalidate();  // FIXME

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
                        /*  FIXME
                        ContextMenu sensorMenu = ioMenu.addSubMenu("Sensor\u2026").getSubMenu();
                        for (String s: SENSOR_MAP.keySet()) {
                            sensorMenu.add(s);
                        }
                        */
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
            // pasteItem.setEnabled(platform.editBuffer() != null);     // FIXME
            editMenu.add(MENU_ITEM_INSERT_ROW);
            editMenu.add(MENU_ITEM_INSERT_COLUMN);
            editMenu.add(MENU_ITEM_DELETE_ROW);
            editMenu.add(MENU_ITEM_DELETE_COLUMN);
        }

        menu.setOnMenuItemClickListener(this);
        menu.show();

    }

    @Override
    public boolean onContextMenuItemClick(ContextMenu.Item item) {
        final String label = item.getTitle().toString();

        if (MENU_ITEM_STOP.equals(label)) {
            stop();
            updateMenu();
            return true;
        }
/*        if (MENU_ITEM_PLAY.equals(label)) {
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
            platform.log("undo to: " + json.toString());
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
            // flowgrid.openOperation(operation, false);  FIXME
        } else if (label.equals(MENU_ITEM_CREATE_SHORTCUT)) {
            // createShortcut();                              FIXME
        } else if (label.equals(MENU_ITEM_DELETE_PATH)) {
            beforeChange();
            operation.removePath(row, col, operationEditor.tutorialMode);
            afterChange();
        } else if (label.equals(MENU_ITEM_CLEAR_CELL)) {
            beforeChange();
            operation.removeCell(row, col);
            afterChange();
        } else if (label.equals(MENU_ITEM_EDIT_CELL)) {
            Command cmd = operation.cell(row, col).command();
            if (cmd instanceof CustomOperation) {
                selection.setVisibility(false);
                operationView.zoomOpen(operation.cell(row, col));
            } else if (cmd instanceof Artifact) {
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

                 */

        return true;
    }

    void stop() {
        System.out.println("TBD: stop()");   // FIXME
    }


    void updateMenu() {
        System.out.println("TBD: updateMenu()");  // FIXME
    }

    void updateLayout() {
        System.out.println("TBD: updateLayout()");  // FIXME
    }


    class ZoomData {
        Cell cell;
        CustomOperation operation;
        int rows;
        long endTime;

        // Paint outlinePaint;
        // Paint fillPaint;

        float originalOriginX;
        float originalOriginY;
        float orginalCellSize;
        float targetOriginX;
        float targetOriginY;
        float targetCellSize;
    }


    class Selection {
        int row;
        int col;
        int width;
        int height;
        boolean visible;

        void setVisibility(boolean visible) {
            if (visible != this.visible) {
                this.visible = visible;
                redraw(sge.screenX(col), sge.screenY(row), Math.round(cellSize * width), Math.round(cellSize * height), true);
            }
        }

        int row() {
            return row;
        }

        int col() {
            return col;
        }

        void setPosition(int row, int col, int height, int width) {
            if (row != this.row || col != this.col || height != this.height || width != this.width) {
                int newX = sge.screenX(col);
                int newY = sge.screenY(row);
                int newW = Math.round(cellSize * width);
                int newH = Math.round(cellSize * height);

                int oldX = sge.screenX(this.col);
                int oldY = sge.screenY(this.row);
                int oldW = Math.round(cellSize * this.width);
                int oldH = Math.round(cellSize * this.height);

                this.row = row;
                this.col = col;
                this.height = height;
                this.width = width;

                menuAnchor.setBounds(newX + newW, newY, 1, 1);

                redraw(oldX, oldY, oldW, oldH, true);
                redraw(newX, newY, newW, newH, true);
            }
        }

        public boolean isShown() {
            return visible;
        }
    }

}
