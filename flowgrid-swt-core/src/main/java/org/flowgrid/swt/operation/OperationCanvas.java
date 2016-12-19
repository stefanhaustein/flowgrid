package org.flowgrid.swt.operation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Cell;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.Command;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Edge;
import org.flowgrid.model.Module;
import org.flowgrid.model.Position;
import org.flowgrid.model.Property;
import org.flowgrid.model.TutorialData;
import org.flowgrid.model.Type;
import org.flowgrid.model.TypeAndValue;
import org.flowgrid.model.VisualData;
import org.flowgrid.model.api.ConstructorCommand;
import org.flowgrid.model.api.LiteralCommand;
import org.flowgrid.model.api.LocalCallCommand;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.model.api.PropertyCommand;
import org.flowgrid.model.hutn.Hutn;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.model.hutn.HutnWriter;
import org.flowgrid.swt.Colors;
import org.flowgrid.swt.Strings;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.data.DataDialog;
import org.flowgrid.swt.graphics.EmojiTextHelper;
import org.flowgrid.swt.port.TestPortDialog;
import org.flowgrid.swt.port.WidgetPort;
import org.flowgrid.swt.port.WidgetPortDialog;
import org.flowgrid.swt.widget.ContextMenu;
import org.flowgrid.swt.widget.MenuSelectionHandler;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class OperationCanvas extends Canvas implements ContextMenu.ItemClickListener, MenuSelectionHandler {
    static final float ZOOM_STEP = 1.1f;
    static final int PIXEL_SNAP = 16;
    static final int ZOOM_TIME_MS = 1000;

    public static final String[] OPERATION_MENU_FILTER = {"control", "examples", "missions", "system"};
    public static final String[] TUTORIAL_EDITOR_OPERATION_MENU_FILTER = {"control", "missions", "system"};


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
    Button resetButton;
    Button startPauseButton;
    Button fasterButton;
    Button slowerButton;
    boolean changing;
    SwtFlowgrid flowgrid;
    ArrayList<String> undoHistory = new ArrayList<>();
    int currentSpeed = 50;
    ProgressBar speedBar;


    public OperationCanvas(final OperationEditor operationEditor, final Composite parent) {
        super(parent, SWT.DOUBLE_BUFFERED);
        this.operationEditor = operationEditor;
        this.operation = operationEditor.operation;
        this.flowgrid = operationEditor.flowgrid;
        this.sge = new ScaledGraphElements(operationEditor.flowgrid.display(), operationEditor.flowgrid.colors, operationEditor.flowgrid.model());

        operation.ensureLoaded();  // FIXME

        if (!operation.isTutorial()) {
            slowerButton = new Button(this, SWT.PUSH);
            slowerButton.setText("\u2212");
            slowerButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    setSpeedBarPosition(Math.max(10, speedBar.getSelection() - 10));
                    updateButtons();
                }
            });

            speedBar = new ProgressBar(this, SWT.NONE);
            speedBar.setMaximum(100);
            speedBar.setSelection(50);
            speedBar.setMinimum(0);
            speedBar.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent e) {
                    setSpeedBarPosition(50);
                    updateButtons();
                }
            });

            fasterButton = new Button(this, SWT.PUSH);
            fasterButton.setText("+");
            fasterButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    setSpeedBarPosition(Math.min(100, speedBar.getSelection() + 10));
                    updateButtons();
                }
            });
       }

        startPauseButton = new Button(this, SWT.PUSH);
        startPauseButton.setText("\u25b6");
        startPauseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (operationEditor.running) {
                    if (currentSpeed > 0) {
                        currentSpeed = 0;
                    } else {
                        unpause();
                    }
                    updateButtons();
                } else {
                    operationEditor.start();
                }
            }
        });


        resetButton = new Button(this, SWT.PUSH);
        resetButton.setText("\u23f9");
        resetButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                operationEditor.stop();
                unpause();
            }
        });

        menuAnchor = new Label(this, SWT.NONE);

        HutnWriter writer = new HutnWriter(new StringWriter());
        writer.startObject();
        operation.toJson(writer, Artifact.SerializationType.FULL);
        writer.endObject();
        undoHistory.add(writer.close().toString());
        initialCellSize = cellSize;

        addMouseMoveListener(new MouseMoveListener() {

            @Override
            public void mouseMove(MouseEvent e) {
                if (!armed) {
                    return;
                }
                if (!moved) {
                    float dx = e.x - startX;
                    float dy = e.y - startY;
                    if (dx * dx + dy * dy < 8 * 8) {
                        return ;
                    }
                    moved = true;
                    if (mayScroll) {
                        scroll = true;
                    }
                }
                if (scroll) {
                    float dx = e.x - lastX;
                    float dy = e.y - lastY;
                    originX -= dx;
                    originY -= dy;
                    lastX = e.x;
                    lastY = e.y;
                    sge.setState(originX, originY, cellSize);
                } else {
                    int col = (int) Math.floor((e.x + originX) / cellSize);
                    int row = (int) Math.floor((e.y + originY) / cellSize);
                    if (!dragging || (col == lastCol && row == lastRow)) {
                        return ;
                    }

/*      if (!inRange(lastRow) && !inRange(row)) {
        return false;
      } */

                    if (col > lastCol + 1) col = lastCol + 1;
                    if (col < lastCol - 1) col = lastCol - 1;
                    if (row > lastRow + 1) row = lastRow + 1;
                    if (row < lastRow - 1) row = lastRow - 1;
                    if (col != lastCol && row != lastRow) {
                        col = lastCol;
                    }
                    Edge edge2;
                    if (col != lastCol) {
                        edge2 = col > lastCol ? Edge.RIGHT : Edge.LEFT;
                    } else {
                        edge2 = row > lastRow ? Edge.BOTTOM : Edge.TOP;
                    }
                    if (edge2 == lastEdge || !inRange(lastRow, lastCol)) {
                        dragging = false;
                    } else if (operation().connect(lastRow, lastCol, lastEdge, edge2)) {
                        changed = true;
                    } else {
                        dragging = false;
                    }
                    lastCol = col;
                    lastRow = row;
                    lastEdge = edge2.opposite();
                }

                redraw();    // FIXME limit range
            }

        });

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                System.out.println(startPauseButton.toString());

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
                        onScaleEnd();
                    } else if (changed) {
                        afterChange();
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

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseScrolled(MouseEvent e) {
                int fx = e.x;
                int fy = e.y;


                float scale = e.count < 0 ? ZOOM_STEP : 1/ZOOM_STEP;
/*
                originX += lastFocusX - fx;
                originY += lastFocusY - fy;
*/
                // we want fx at the same position after scaling

                // absolute coordiantes before scaling.
                float absFx = originX + fx;
                float absFy = originY + fy;

                absFx *= scale;
                absFy *= scale;

                // fx / fy need to remain at the same point...
                originX = absFx - fx;
                originY = absFy - fy;

                lastFocusX = fx;
                lastFocusY = fy;

                cellSize *= scale;
                sge.setState(originX, originY, cellSize);

               // onScaleEnd();
                redraw();
            }
        });

        setLayout(new Layout(){

            @Override
            protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
                return new Point(wHint == SWT.DEFAULT ? 512 : wHint, hHint == SWT.DEFAULT ? 400 : hHint);
            }

            @Override
            protected void layout(Composite composite, boolean flushCache) {
                if (composite != OperationCanvas.this) {
                    throw new RuntimeException();
                }
                Rectangle bounds = getBounds();

                int spacing = 5;

                Button[] topButtons = new Button[]{startPauseButton, resetButton};

                int buttonW = 0;
                int buttonH = 0;
                for (Button button: topButtons) {
                    Point size = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                    if (size.x > buttonW) {
                        buttonW = size.x;
                    }
                    if (size.y > buttonH) {
                        buttonH = size.y;
                    }
                }

                if (buttonW < buttonH) {
                    buttonW = buttonH;
                }

             /*   if (buttonW > 3 * buttonH / 2) {
                    buttonW = 3 * buttonH / 2;
                }*/
                for (int i = 0; i < topButtons.length; i++) {
                    topButtons[i].setBounds(
                            bounds.width - (buttonW + spacing) * (topButtons.length - i),
                            spacing,
                            buttonW, buttonH);
                }

                if (!operationEditor.tutorialMode) {
                    Control[] bottomControls = new Control[]{slowerButton, speedBar, fasterButton};
                    for (int i = 0; i < bottomControls.length; i++) {
                        int x = bounds.width - (buttonW + spacing) * (bottomControls.length - i);
                        int y = bounds.height - spacing - buttonH;
                        int h = buttonH;
                        if (bottomControls[i] == speedBar) {
                            h = Math.min(buttonH, speedBar.computeSize(-1, -1).y);
                            y += (buttonH - h) / 2;
                        }
                        bottomControls[i].setBounds(x, y, buttonW, h);
                    }

                }

                menuAnchor.setBounds(-1, -1, 1, 1);
            }
        });
    }

    void addLiteral() {
        DataDialog.show(flowgrid, "Add Literal", Type.ANY, operation.module(), null, new Callback<Object>() {
            @Override
            public void run(Object value) {
                beforeChange();
                Type type = flowgrid.model().type(value);
                LiteralCommand literal = new LiteralCommand(flowgrid.model(), type, value);
                operation.setCommand(selection.row(), selection.col(), literal);
                selection.setVisibility(true);
                afterChange();
            }

            @Override
            public void cancel() {
                selection.setVisibility(false);
            }
        });

        flowgrid.editStructuredDataValue(operation(), new String[]{"literal", Position.toString(selection.row, selection.col)},
                menuAnchor, new Callback<TypeAndValue>() {
                    @Override
                    public void run(TypeAndValue variant) {

                    }

                    @Override
                    public void cancel() {
                        selection.setVisibility(false);
                    }
                });
    }

    void addPortCommand(String type, String name, boolean input, boolean output, Object... peerJson) {
        PortCommand portCommand = new PortCommand(name, input, output);
        portCommand.peerJson().put("portType", type);
        for (int i = 0; i < peerJson.length; i += 2) {
            portCommand.peerJson().put((String) peerJson[i], peerJson[i + 1]);
        }
        editPort(portCommand, true);
    }

    private void addWidgetPort(final boolean input, final boolean output, String widget) {
        // TODO(haustein) Move name disambiguation into addPortCommand and remove this?
        HashSet<String> usedNames = new HashSet<>();
        for (WidgetPort i: operationEditor.portWidgets()) {
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
            addPortCommand("MetaControl", name, input, output, "widget", widget);
        } else {
            addPortCommand("MetaControl", name, input, output);
        }
    }


    void afterBulkChange() {
        operationEditor.attachAll();
        afterChange();
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

            redraw();

            operation.save();
            operation.validate();

            updateLayout();

            if (undoHistory.size() == 2) {
                operationEditor.updateMenu();
            }
        }
    }

    public float calculateCellSize(CustomOperation op) {
        int[] size = new int[4];
        op.size(size);
        Point available = getSize();

        if (size[0] >= 0 && size [1] >= 0) {
            float newCellSize = Math.min(available.y / Math.max(8f, size[2] + 1),
                    available.x / Math.max(8f, size[3] + 1));  // operator width, scrollbar

            float newScale = newCellSize / initialCellSize;
            double quantumScale = Math.pow(ZOOM_STEP, Math.floor(Math.log(newScale) / Math.log(ZOOM_STEP)));
            //  scale = (float) (initialCellSize * f) / newCellSize;

            //selection.setVisibility(INVISIBLE);

            newCellSize = (float) (initialCellSize * quantumScale);

            return newCellSize; // * scale;
        }
        return flowgrid.dpToPx(32);
    }

    void beforeBulkChange() {
        beforeChange();
        operationEditor.detachAll();
    }

    void beforeChange() {
        if (changing) {
            flowgrid.log("beforeChange called with changing = true");
        }
        changing = true;
        operationEditor.stop();
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
    public void drawBackground(GC gc, int clipX, int clipY, int clipW, int clipH) {
        sge.setState(originX, originY, cellSize);

        Colors colors = operationEditor.flowgrid.colors;

        gc.setAntialias(SWT.ON);
        // gc.setTextAntialias(SWT.ON);  // FIXME
        gc.setLineJoin(SWT.JOIN_ROUND);
        gc.setLineCap(SWT.CAP_ROUND);

        gc.setBackground(colors.background);
        gc.fillRectangle(clipX, clipY, clipW, clipH);

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
            gc.drawLine(lx, clipY, lx, clipY + clipH);
        }

//        float minX = -100 * cellSize - originX - 1;
 //       float maxX = 100 * cellSize - originX - 1;

        for (int row = -100; row <= 100; row++) {
            gc.setForeground(row == minRow || row == maxRow ? colors.origin : colors.grid);
            int ly = Math.round(row * cellSize - originY - 1);
            gc.drawLine(clipX, ly, clipX + clipW, ly);
        }

        if (selection.isShown()) {
            for (int i = 0; i < currentTypeFilter.size(); i++) {
                Type t = currentTypeFilter.get(i);
                int r0 = currentAutoConnectStartRow.get(i);
                int x = Math.round((selection.col + i) * cellSize + cellSize / 2 - originX);
                sge.drawConnector(gc,
                        x, Math.round(r0 * cellSize - originY),
                        x, Math.round(selection.row * cellSize - originY),
                        t);
            }
        }


        if (tutorialData != null && tutorialData.order == 0) {
            int x0 = Math.round(1.5f * cellSize - originX);
            int y0 = Math.round(4.5f * cellSize - originY);
            gc.setForeground(colors.white);
            gc.setFont(colors.getFont(Math.round(cellSize), 0));
            EmojiTextHelper.drawText(gc, "\u21e9", x0, y0, SWT.CENTER, SWT.TOP);
    /*        if (!landscapeMode) {
                downArrowPaint.setColor(Color.LTGRAY);
                downArrowPaint.setTextSize(cellSize / 3);
                downArrowPaint.setTextAlign(Paint.Align.LEFT);
                TextHelper.drawText(getContext(), canvas, "rotate for help", x0 + cellSize/2, y0, downArrowPaint, TextHelper.VerticalAlign.CENTER);
            }
            */
        }


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
                                    sge.drawData(gc, Math.round(xi + cellSize / 3), Math.round(y0 - cellSize / 3), ii.next(), false, -4);
                                }
                                sge.drawData(gc, Math.round(xi + cellSize / 6), Math.round(y0 - cellSize / 6), nextNext, false, -2);
                            }
                            sge.drawData(gc, xi, y0, next, false, ready ? 1 : 0);
                        }
                    }
                }
            }
        }

        if (zoomData != null) {
            int x0 = Math.round(zoomData.cell.col() * cellSize - originX);
            int y0 = Math.round(zoomData.cell.row() * cellSize - originY);

            gc.setBackground(colors.black);
            gc.setAlpha(zoomData.alpha);
            gc.fillRectangle(x0, y0, Math.round(x0 + zoomData.cell.width() * cellSize),
                    Math.round(y0 + cellSize));
            gc.setAlpha(255);
            // canvas.drawRect(x0, y0, x0 + zoomData.cell.width() * cellSize, y0 + cellSize, zoomData.outlinePaint);

            ScaledGraphElements childSge = new ScaledGraphElements(getDisplay(), colors, flowgrid.model());
            childSge.setState(-x0, -y0, cellSize / zoomData.rows);
            for (Cell childCell: zoomData.operation) {
                childSge.drawCell(gc, childCell, false);
            }
        }

        cellsReady.clear();
        for (VisualData data: operationEditor.controller.getAndAdvanceVisualData(
                operation().isTutorial() ? operation().tutorialData.speed : currentSpeed)) {
            drawData(data, gc);
        }

        /*
        if (operationEditor.controller.isRunning()) {
            gc.drawString(operationEditor.controller.status(), Views.px(getContext(), 12),
                    getHeight() - debugTextPaint.getFontMetrics(null) / 2, debugTextPaint);
        }
        */


        // Update zoom etc.
        if (zoomData != null) {
            float remaining = (zoomData.endTime - System.currentTimeMillis()) / ((float) ZOOM_TIME_MS);
            float completed = 1f - remaining;
            if (completed > 1) {
                originX = zoomData.originalOriginX;
                originY = zoomData.originalOriginY;
                cellSize = zoomData.orginalCellSize;
                flowgrid.openArtifact(zoomData.operation);
                zoomData = null;
            } else {
                zoomData.alpha = Math.min((int) (completed * 1024), 255);

                cellSize = zoomData.orginalCellSize * remaining + zoomData.targetCellSize * completed;
                originX = zoomData.originalOriginX * remaining + zoomData.targetOriginX * completed;
                originY = zoomData.originalOriginY * remaining + zoomData.targetOriginY * completed;
                redraw();
            }
            sge.setState(originX, originY, cellSize);
        }


        int selectionX = sge.screenX(selection.col);
        int selectionY = sge.screenY(selection.row);

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


    private void editPort(final PortCommand portCommand, final boolean creating) {
        Callback<Void> callback = new Callback<Void>() {
            @Override
            public void run(Void value) {
                beforeChange();
                if (creating) {
                    operationEditor.controller.operation.setCommand(selection.row(), selection.col(), portCommand);
                } else {
                    portCommand.detach();
                }
                operationEditor.attachPort(portCommand);
                afterChange();
            }

            @Override
            public void cancel() {
                operationEditor.resetTutorial();
            }
        };

        String portType = OperationEditor.portType(portCommand.peerJson());
        if (portType.equals("Sensor")) {
            callback.run(null);
      /*  } else if (portType.equals("Firmata")) {
            FirmataPortDialog.show(flowgrid, flowgrid.model(), portCommand, creating, callback); */
        } else if (portType.equals("Test")) {
            TestPortDialog.show(flowgrid.shell(), portCommand, creating, callback);
        } else {
            WidgetPortDialog.show(flowgrid, operation.module, portCommand, creating, callback);
        }
    }


    private boolean inRange(int row, int col) {
        return !operationEditor.tutorialMode ||
                (row >= operation().tutorialData.editableStartRow &&
                        row < operation().tutorialData.editableEndRow && col >= 0);
    }


    public void onScaleEnd() {
//    originX -= lastFocusX;
//    originY -= lastFocusY;
        float scale = cellSize / initialCellSize;
        double f = Math.pow(ZOOM_STEP, Math.round(Math.log(scale) / Math.log(ZOOM_STEP)));
        scale = (float) (initialCellSize * f) / cellSize;
        cellSize *= scale;

        originX += lastFocusX * scale - lastFocusX;
        originY += lastFocusY * scale - lastFocusY;

        originX = Math.round(originX / PIXEL_SNAP) * PIXEL_SNAP;
        originY = Math.round(originY / PIXEL_SNAP) * PIXEL_SNAP;
        selection.setVisibility(false);
        sge.setState(originX, originY, cellSize);
        redraw();
    }

    void setSpeedBarPosition(int speed) {
        if (currentSpeed != 0){
            currentSpeed = speed;
        }
        if (speedBar != null) {
            speedBar.setSelection(speed);
        }
    }

    void showPopupMenu(Cell cell) {
        if (menu != null) {
            menu.dispose();
        }
        menu = new ContextMenu(menuAnchor);
        // menu.setHelpProvider(platform);                                     FIXME
        if (operation.isTutorial()) {
            menu.setDisabledMap(operation().tutorialData.disabledMenus, !operationEditor.tutorialMode, new Callback<Void>() {
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
                menu.add(Strings.MENU_ITEM_DELETE_PATH);
            } else if ((cmd instanceof CustomOperation && cmd != operation)
                    || (cmd instanceof PropertyCommand)
                    || (cmd instanceof PortCommand && !((PortCommand) cmd).peerJson().containsKey("sensor"))
                    || (cmd instanceof LocalCallCommand && ((LocalCallCommand) cmd).operation() != operation) ||
                    (cmd instanceof ConstructorCommand)) {
                menu.add(Strings.MENU_ITEM_EDIT_CELL);
            }
            menu.add(Strings.MENU_ITEM_CLEAR_CELL);
        } else {
            buildTypeFilter();
        }

        // operationView.invalidate();  // FIXME

        Cell cellBelow = operation.cell(selection.row + 1, selection.col);
        boolean hasInput = currentTypeFilter.size() > 0;
        boolean showConstantMenu = !hasInput || (cellBelow != null && cellBelow.command() != null);

        if (cell == null || showConstantMenu) {
            ContextMenu ioMenu = menu.addSubMenu(Strings.MENU_ITEM_DATA_IO).getSubMenu();
            if (ioMenu != null) {
                ioMenu.add(Strings.MENU_ITEM_CONSTANT_VALUE);
                if (cell == null) {
                    if (!hasInput) {
                        ioMenu.add(Strings.MENU_ITEM_INPUT_FIELD);
                    }
                    ioMenu.add(Strings.MENU_ITEM_COMBINED_FIELD);
                    if (!hasInput) {
                        /*  FIXME
                        ContextMenu sensorMenu = ioMenu.addSubMenu("Sensor\u2026").getSubMenu();
                        for (String s: SENSOR_MAP.keySet()) {
                            sensorMenu.add(s);
                        }
                        */
                    }
                    ContextMenu outputMenu = ioMenu.addSubMenu("Output\u2026").getSubMenu();
                    outputMenu.add(Strings.MENU_ITEM_OUTPUT_FIELD);
                    outputMenu.add(Strings.MENU_ITEM_CANVAS);
                    outputMenu.add(Strings.MENU_ITEM_HISTOGRAM);
                    outputMenu.add(Strings.MENU_ITEM_PERCENT_BAR);
                    outputMenu.add(Strings.MENU_ITEM_RUN_CHART);
                    outputMenu.add(Strings.MENU_ITEM_WEB_VIEW);

                    ContextMenu firmataMenu = ioMenu.addSubMenu("Firmata\u2026").getSubMenu();
                    firmataMenu.add(Strings.MENU_ITEM_FIRMATA_ANALOG_INPUT);
                    firmataMenu.add(Strings.MENU_ITEM_FIRMATA_ANALOG_OUTPUT);
                    firmataMenu.add(Strings.MENU_ITEM_FIRMATA_DIGITAL_INPUT);
                    firmataMenu.add(Strings.MENU_ITEM_FIRMATA_DIGITAL_OUTPUT);
                    firmataMenu.add(Strings.MENU_ITEM_FIRMATA_SERVO_OUTPUT);

                    ContextMenu testMenu = ioMenu.addSubMenu("Test\u2026").getSubMenu();
                    testMenu.add(Strings.MENU_ITEM_TEST_INPUT);
                    testMenu.add(Strings.MENU_ITEM_EXPECTATION);
                }
            }
        }

        if (cell == null) {
            menu.add(Strings.MENU_ITEM_CONTROL);

            if (operation.classifier != null) {
                menu.add(Strings.MENU_ITEM_THIS_CLASS);
            }
            if (operation.module().parent() != null) {
                menu.add(Strings.MENU_ITEM_THIS_MODULE);
            }
            if (currentTypeFilter.size() > 0 && (currentTypeFilter.get(0) instanceof Classifier)) {
                Classifier classifier = (Classifier) currentTypeFilter.get(0);
                menu.add(classifier.name() + "\u2026");
            }

            menu.add(Strings.MENU_ITEM_OPERATIONS_CLASSES);
        }

        if (cellBelow != null && cellBelow.command() != null) {
            int index = selection.col - cellBelow.col();
            if (index < cellBelow.inputCount()) {
                if (cellBelow.isBuffered(index)) {
                    menu.add(Strings.MENU_ITEM_REMOVE_BUFFER);
                } else {
                    menu.add(Strings.MENU_ITEM_ADD_BUFFER);
                }
            }
        }

        ContextMenu editMenu = menu.addSubMenu(Strings.MENU_ITEM_EDIT).getSubMenu();
        if (editMenu != null) {
            ContextMenu.Item pasteItem = editMenu.add(Strings.MENU_ITEM_PASTE);
            pasteItem.setEnabled(flowgrid.editBuffer() != null);     // FIXME
            editMenu.add(Strings.MENU_ITEM_INSERT_ROW);
            editMenu.add(Strings.MENU_ITEM_INSERT_COLUMN);
            editMenu.add(Strings.MENU_ITEM_DELETE_ROW);
            editMenu.add(Strings.MENU_ITEM_DELETE_COLUMN);
        }

        menu.setOnMenuItemClickListener(this);
        menu.show();

    }

    @Override
    public void menuItemSelected(MenuItem menuItem) {
        String label = menuItem.getText();
        if (Strings.MENU_ITEM_COPY.equals(label)) {
            flowgrid.setEditBuffer(operation.copy(selection.row, selection.col,
                    selection.height, selection.width));

        } else if (Strings.MENU_ITEM_CUT.equals(label)) {
            flowgrid.setEditBuffer(operation.copy(selection.row, selection.col,
                    selection.height, selection.width));
            beforeBulkChange();
            operation.clear(selection.row, selection.col, selection.height, selection.width);
            afterBulkChange();
            setSelectionMode(false);

        } else if (label.equals(Strings.MENU_ITEM_UNDO)) {
            HutnObject json = (HutnObject) Hutn.parse(undoHistory.get(undoHistory.size() - 2));
            flowgrid.log("undo to: " + json.toString());
            beforeBulkChange();
            operation.clear();
            operation.setPublic(false);
            operation.setAsyncInput(false);
            operation.fromJson(json, Artifact.SerializationType.FULL, null);
            afterBulkChange();
            undoHistory.remove(undoHistory.size() - 1);
            undoHistory.remove(undoHistory.size() - 1);
            if (undoHistory.size() == 1) {
                operationEditor.updateMenu();
            }
        }

    }

    @Override
    public boolean onContextMenuItemClick(ContextMenu.Item item) {

        System.out.println("onContextMenuItemClick: " + item.getTitle());

        final String label = item.getTitle().toString();

        /*
        if (Strings.MENU_ITEM_STOP.equals(label)) {
            operationEditor.stop();
            return true;
        }
        if (Strings.MENU_ITEM_PLAY.equals(label)) {
            StringBuilder missing = new StringBuilder();
            if (!operation.asyncInput() && !operationEditor.isInputComplete(missing)) {
                System.out.println("Toast.makeText(platform, \"Missing input: \" + missing, Toast.LENGTH_LONG).show();");  //FIXME
            } else {
                operationEditor.start();
            }
            return true;
        }
        if (Strings.MENU_ITEM_CANCEL.equals(label)) {
            setSelectionMode(false);
            return true;
        }*/

        if (currentTypeFilter.size() > 0 && currentTypeFilter.get(0) instanceof Classifier &&
                label.equals(currentTypeFilter.get(0).name() + "\u2026")) {
            new CommandMenu(menuAnchor, item, operation.module(), currentTypeFilter, new Callback<Command>() {
                @Override
                public void run(Command value) {
                    addMemberCommand(value);
                }
            }).showType(currentTypeFilter.get(0), false);
            return true;
        }

        if (label.equals(Strings.MENU_ITEM_THIS_CLASS)) {
            System.out.println("calling new artifactment.show");
            new CommandMenu(menuAnchor, item, operation.module(), currentTypeFilter, new Callback<Command>() {
                @Override
                public void run(Command value) {
                    addMemberCommand(value);
                }
            }).showType(operation.classifier, true);
            return true;
        }

        /*

        // Needs to be after MENU_ITEM_PUBLIC because we implement slightly different
        // behavior here.
        if (super.onContextMenuItemClick(item)) {
            return true;
        }
*/
        Module module = null;
        String[] filter = {};
        if (label.equals(Strings.MENU_ITEM_OPERATIONS_CLASSES)) {
            module = flowgrid.model().rootModule;
            filter = operation().isTutorial() && !operationEditor.tutorialMode
                    ? TUTORIAL_EDITOR_OPERATION_MENU_FILTER
                    : OPERATION_MENU_FILTER;
        } else if (label.equals(Strings.MENU_ITEM_THIS_MODULE)) {
            module = operation.module();
        } else if (label.equals(Strings.MENU_ITEM_CONTROL)) {
            module = flowgrid.model().rootModule.module("control");
        }
        if (module != null) {
            new CommandMenu(menuAnchor, item, operation.module(), currentTypeFilter, new Callback<Command>() {
                @Override
                public void run(Command result) {
                    addMemberCommand(result);
                }
            }).showModule(module, false, filter);

            return true;
        }
/*
        if (SENSOR_MAP.containsKey(label)) {
            beforeChange();
            addPortCommand("Sensor", label, true, false, "sensor", SENSOR_MAP.get(label));
            afterChange();
            suggestContinuous("Most sensors provide a continous stream of input and may not work for " +
                    "regular operations.");
            return true;
        } */

        Command command = null;
        int row = selection.row();
        int col = selection.col();

        if (label.equals(Strings.MENU_ITEM_ADD_BUFFER) || label.equals(Strings.MENU_ITEM_REMOVE_BUFFER)) {
            Cell below = operation.cell(row + 1, col);
            int index = col - below.col();
            beforeChange();
            below.setBuffered(index, label.equals(Strings.MENU_ITEM_ADD_BUFFER));
            afterChange();
        } else if (label.equals(Strings.MENU_ITEM_DELETE_PATH)) {
            beforeChange();
            operation.removePath(row, col, operationEditor.tutorialMode);
            afterChange();
        } else if (label.equals(Strings.MENU_ITEM_CLEAR_CELL)) {
            beforeChange();
            operation.removeCell(row, col);
            afterChange();
        } else if (label.equals(Strings.MENU_ITEM_EDIT_CELL)) {
            Command cmd = operation.cell(row, col).command();
            if (cmd instanceof CustomOperation) {
                selection.setVisibility(false);
                zoomOpen(operation.cell(row, col));
            } else if (cmd instanceof Artifact) {
                flowgrid.openArtifact((Artifact) cmd);
            } else if (cmd instanceof PropertyCommand) {
                Property p = ((PropertyCommand) cmd).property();
                flowgrid.openProperty(p);
            } else if (cmd instanceof LocalCallCommand) {
                CustomOperation op = ((LocalCallCommand) cmd).operation();
                flowgrid.openOperation(op, true);
            } else if (cmd instanceof PortCommand) {
                editPort((PortCommand) cmd, false);
            } else if (cmd instanceof ConstructorCommand) {
                flowgrid.openClassifier(((ConstructorCommand) cmd).classifier());
            }
        } else if (label.equals(Strings.MENU_ITEM_INPUT_FIELD)) {
            addWidgetPort(true, false, null);
        } else if (label.equals(Strings.MENU_ITEM_OUTPUT_FIELD)) {
            addWidgetPort(false, true, null);
        } else if (label.equals(Strings.MENU_ITEM_TEST_INPUT)) {
            addPortCommand("Test", "TestInput", true, false, "testData", "");
        } else if (label.equals(Strings.MENU_ITEM_EXPECTATION)) {
            addPortCommand("Test", "Expectation", false, true, "testData", "");
        } else if (label.equals(Strings.MENU_ITEM_CANVAS)) {
            addWidgetPort(true, false, "canvas");
        } else if (label.equals(Strings.MENU_ITEM_HISTOGRAM)) {
            addWidgetPort(false, true, "histogram");
        } else if (label.equals(Strings.MENU_ITEM_RUN_CHART)) {
            addWidgetPort(false, true, "runchart");
        } else if (label.equals(Strings.MENU_ITEM_WEB_VIEW)) {
            addWidgetPort(true, true, "webview");
        } else if (label.equals(Strings.MENU_ITEM_PERCENT_BAR)) {
            addWidgetPort(false, true, "percent");
        } else if (label.equals(Strings.MENU_ITEM_COMBINED_FIELD)) {
            addWidgetPort(true, true, null);
        } else if (label.equals(Strings.MENU_ITEM_CONSTANT_VALUE)) {
            addLiteral();
        } else if (label.equals(Strings.MENU_ITEM_INSERT_ROW)) {
            beforeChange();
            operation.insertRow(row);
            afterChange();
        } else if (label.equals(Strings.MENU_ITEM_INSERT_COLUMN)) {
            beforeChange();
            operation.insertCol(col);
            afterChange();
        } else if (label.equals(Strings.MENU_ITEM_DELETE_ROW)) {
            beforeChange();
            operation.deleteRow(row);
            afterChange();
        } else if (label.equals(Strings.MENU_ITEM_DELETE_COLUMN)) {
            beforeChange();
            operation.deleteCol(col);
            afterChange();
        } else if (label.equals(Strings.MENU_ITEM_PASTE)) {
            beforeBulkChange();
            operation.cellsFromJson(flowgrid.editBuffer(), selection.row, selection.col);
            afterBulkChange();
    /*  } else if (label.equals(Strings.MENU_ITEM_FIRMATA_ANALOG_INPUT)) {
            addFirmataPort(true, false, FirmataPort.Mode.ANALOG);
        } else if (label.equals(Strings.MENU_ITEM_FIRMATA_ANALOG_OUTPUT)) {
            addFirmataPort(false, true, FirmataPort.Mode.ANALOG);
        } else if (label.equals(Strings.MENU_ITEM_FIRMATA_DIGITAL_INPUT)) {
            addFirmataPort(true, false, FirmataPort.Mode.DIGITAL);
        } else if (label.equals(Strings.MENU_ITEM_FIRMATA_DIGITAL_OUTPUT)) {
            addFirmataPort(false, true, FirmataPort.Mode.DIGITAL);
        } else if (label.equals(Strings.MENU_ITEM_FIRMATA_SERVO_OUTPUT)) {
            addFirmataPort(false, true, FirmataPort.Mode.SERVO);*/
        }

        if (command != null) {
            beforeChange();
            operation.setCommand(row, col, command);
            afterChange();
        }
        return true;
    }

    private void setSelectionMode(boolean b) {
        System.out.println("FIXME: setSelectionMode: " + b);   // FIXME
    }

    private void addMemberCommand(Command command) {
        beforeChange();
        operation.setCommand(selection.row(), selection.col(), command);
        afterChange();
    }

    private void unpause() {
        if (speedBar != null) {
            currentSpeed = speedBar.getSelection();
        } else {
            currentSpeed = operation.tutorialData.speed;
        }
    }

    /**
     * Called from OperationEditor.updataMenu();
     */
    void updateButtons() {
        startPauseButton.setText(operationEditor.running && currentSpeed > 0 ? "\u23f8" : "\u25b6"); //: */"\u25FC" );
        if (speedBar != null){
            fasterButton.setEnabled(speedBar.getSelection() < 100);
            slowerButton.setEnabled(speedBar.getSelection() > 10);
        }
        resetButton.setEnabled(operationEditor.running);
    }

    void updateLayout() {
        System.out.println("TBD: updateLayout()");  // FIXME
    }

    void autoZoom() {
        cellSize = calculateCellSize(operation);
        sge.setState(originX, originY, cellSize);
        redraw();
    }


    protected void zoomOpen(Cell cell) {
        int[] size = new int[4];
        CustomOperation operation =  (CustomOperation) cell.command();
        operation.size(size);

        zoomData = new ZoomData();
        zoomData.cell = cell;
        zoomData.operation = operation;
        zoomData.rows = size[2] + 2;

        zoomData.targetCellSize = size[2] * calculateCellSize(zoomData.operation);
        zoomData.endTime = System.currentTimeMillis() + ZOOM_TIME_MS;

        zoomData.originalOriginX = originX;
        zoomData.originalOriginY = originY;
        zoomData.orginalCellSize = cellSize;

        zoomData.targetOriginX = cell.col() * zoomData.targetCellSize;
        zoomData.targetOriginY = cell.row() * zoomData.targetCellSize;

        zoomData.alpha = 0;

        redraw();
    }


    class ZoomData {
        Cell cell;
        CustomOperation operation;
        int rows;
        long endTime;

        // Paint outlinePaint;
        // Paint fillPaint;
        int alpha;

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
