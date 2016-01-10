package org.flowgrid.android.operation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.flowgrid.android.Views;
import org.flowgrid.android.graphics.Colors;
import org.flowgrid.android.graphics.ScaledGraphElements;
import org.flowgrid.android.port.TestPort;
import org.flowgrid.model.ArrayType;
import org.flowgrid.model.Cell;
import org.flowgrid.model.Shape;
import org.flowgrid.model.Edge;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.TutorialData;
import org.flowgrid.model.Controller;
import org.flowgrid.model.Type;
import org.flowgrid.model.Types;
import org.flowgrid.model.VisualData;
import org.flowgrid.model.Command;
import org.flowgrid.model.api.PortCommand;
import org.kobjects.emoji.android.TextHelper;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;

public class EditOperationView extends View implements OnScaleGestureListener {
  static final String BREAK_CHARS = "\n\t -.,;:+)?/*";
  
  static final float ZOOM_STEP = 1.2f;
  
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
  ScaleGestureDetector scaleDetector;
  GestureDetector gestureDetector;
  SelectionView selection;
  float cellSize = 32;
  
  float originX = 0;
  float originY = 0;
  float initialCellSize;
  float connectorRadius;
  
  private ScaledGraphElements sge;
  EditOperationFragment fragment;
  private Controller controller;
  Set<Cell> cellsReady = new HashSet<Cell>();
  private float lastFocusX;
  private float lastFocusY;
  boolean autoZoom = true;
  final Paint gridPaint = new Paint();
  final Paint debugTextPaint = new Paint();
  final Paint downArrowPaint = new Paint();


  public EditOperationView(final EditOperationFragment fragment) {
    super(fragment.getActivity());
    
    this.setBackgroundColor(0xff000000);
    this.fragment = fragment;
    this.controller = fragment.controller;
    this.selection = fragment.selectionView();
    this.scaleDetector = new ScaleGestureDetector(fragment.getActivity(), this);
    this.gestureDetector = new GestureDetector(fragment.getActivity(), new GestureDetector.SimpleOnGestureListener() {
      @Override
      public void onLongPress(MotionEvent e) {
        fragment.setSelectionMode(true);
      }
    });
    this.gestureDetector.setIsLongpressEnabled(true);

    gridPaint.setStyle(Paint.Style.STROKE);
    gridPaint.setAntiAlias(true);

    debugTextPaint.setColor(Color.GRAY);
    debugTextPaint.setAntiAlias(true);
    debugTextPaint.setTextSize(Views.px(fragment.platform(), 16));

    sge = new ScaledGraphElements(fragment.platform(), cellSize);
    setCellSize(cellSize * fragment.getActivity().getResources().getDisplayMetrics().density);
    initialCellSize = cellSize;

    selection.setVisibility(INVISIBLE);
  }

  
  private void drawData(VisualData data, Canvas canvas) {
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
      sge.drawData(canvas, col * cellSize + cellSize / 2 - originX, row * cellSize - originY, data.value(), false, ready != null ? 1 : 0);
      return;
    }

    float x = col * cellSize + xOffset(edge) * (1-progress) + xOffset(target) * progress - originX;
    float y = row * cellSize + yOffset(edge) * (1-progress) + yOffset(target) * progress - originY;
    sge.drawData(canvas, x, y, data.value(), false, 0);
  }
  

  void drawConnector(Canvas canvas, int row, int col, Edge fromEdge, Edge toEdge, Type type) {
    float x0 = col * cellSize - originX;
    float y0 = row * cellSize - originY;
    float toX = x0 + xOffset(toEdge);
    float toY = y0 + yOffset(toEdge);
    sge.drawConnector(canvas, x0 + xOffset(fromEdge), y0 + yOffset(fromEdge), toX, toY, type);
    int toRow = row + toEdge.row;
    int toCol = col + toEdge.col;
    
    if (!fragment.operation.hasInputConnector(toRow, toCol, toEdge.opposite())) {
      sge.drawOpenConnection(canvas, toX, toY, type);
    }
  }
  
  private void drawConnections(Cell cell, Canvas canvas) {
    for (int i = 0; i < 4; i++) {
      Edge from = Edge.forIndex(i);
      Edge to = cell.connection(i);
      if (to != null) {
        drawConnector(canvas, cell.row(), cell.col(), from, to, cell.inputType(i));
      }
    }
  }


  private boolean inRange(int row, int col) {
    return !fragment.tutorialMode || 
        (row >= operation().tutorialData.editableStartRow &&
            row < operation().tutorialData.editableEndRow && col >= 0);
  }
  
  
  private void drawOperator(Cell cell, Canvas canvas, float x0, float y0, boolean ready) {
    Command cmd = cell.command();
    int width = cell.width();

    if (cmd.shape() == Shape.BRANCH) {
   //   float border = cellSize / 10f;
      Type type = cell.inputType(0);

      if (ready) {
        sge.highlightCell(canvas, x0, y0, Color.WHITE & 0x3fffffff);
      }

      if (cmd.outputType(0, null) != null) {
        drawConnector(canvas, cell.row(), cell.col(), Edge.TOP, Edge.LEFT, type);
      }
      if (cmd.outputType(1, null) != null) {
        drawConnector(canvas, cell.row(), cell.col(), Edge.TOP, Edge.BOTTOM, type);
      }
      if (cmd.outputType(2, null) != null) {
        drawConnector(canvas, cell.row(), cell.col(), Edge.TOP, Edge.RIGHT, type);
      }
      return;
    }
    
    Type[] inputSignature = new Type[cmd.inputCount()];

    // Input connectors
    float yM = y0 + cellSize / 2;
    if (cell.inputCount() == 0) {
      float x = x0 + cellSize / 2;
      sge.drawConnector(canvas, x, y0, x, yM, null);
      Type type = cell.inputType(0);
      if (type != null) {
        while (Types.isArray(type)) {
          type = ((ArrayType) type).elementType;
        }
        sge.drawConnector(canvas, x - cellSize / 10, y0, x + cellSize / 10, y0, cell.inputType(0));
      }
    } else {
      for (int i = 0; i < cell.inputCount(); i++) {
        float x = x0 + i * cellSize + cellSize / 2;

        Type actualType = cell.inputType(i);
        if (actualType != null && cmd.inputType(i).isAssignableFrom(actualType)) {
          inputSignature[i] = actualType;
        } else {
          inputSignature[i] = cmd.inputType(i);
        }
        sge.drawConnector(canvas, x, y0, x, yM, cell.inputType(i));
      }
    }
    
    // Output connectors
    for (int i = 0; i < cmd.outputCount(); i++) {
      Type oti = cmd.outputType(i, inputSignature);
      if (oti != null) {
        drawConnector(canvas, cell.row(), cell.col() + i, null, Edge.BOTTOM, oti);
      }
    }

    boolean regular = true;
    if (cmd instanceof PortCommand) {
      PortCommand portCommand = (PortCommand) cmd;
      if (portCommand.port() instanceof TestPort) {
        ((TestPort) portCommand.port()).draw(fragment.platform(), canvas, x0, y0, cellSize, ready);
        regular = false;
      }
    }
    if (regular) {
      sge.drawOperator(canvas, cmd, x0, y0, width, ready);
    }

    for (int i = 0; i < inputSignature.length; i++) {
      if (cell.inputType(i) != null && cell.inputType(i) != inputSignature[i]) {
        float x = x0 + i * cellSize + cellSize / 2;
        sge.drawErrorMarker(canvas, x, y0);
      }
    }
  }
  
  
  @Override
  protected void onDraw(android.graphics.Canvas canvas) {
    int minRow = 0;
    int maxRow = 0;

    TutorialData tutorialData = operation().tutorialData;
    fragment.counted = 0;
    
    if (tutorialData != null) {
      minRow = tutorialData.editableStartRow;
      maxRow = tutorialData.editableEndRow;
    }
    
    float minY = -100 * cellSize - originY - 1;
    float maxY = 100 * cellSize - originY - 1;
        
    for (int col = -100; col <= 100; col++) {
      gridPaint.setColor(col == 0 ? Color.GRAY : Color.DKGRAY);
      float x = col * cellSize - originX - 1;
      canvas.drawLine(x, minY, x, maxY, gridPaint);
    }
    
    float minX = -100 * cellSize - originX - 1;
    float maxX = 100 * cellSize - originX - 1;
    
    for (int row = -100; row <= 100; row++) {
      gridPaint.setColor(row == minRow || row == maxRow ? Color.GRAY : Color.DKGRAY);
      float y = row * cellSize - originY - 1;
      canvas.drawLine(minX, y, maxX, y, gridPaint);
    }
    
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

    if (tutorialData != null && tutorialData.order == 0) {
      float x0 = 1.5f * cellSize - originX;
      float y0 = 4.5f * cellSize - originY;
      downArrowPaint.setStyle(Paint.Style.FILL);
      downArrowPaint.setColor(Color.WHITE);
      downArrowPaint.setTextSize(cellSize);
      downArrowPaint.setTextAlign(Paint.Align.CENTER);
      TextHelper.drawText(getContext(), canvas, "\u21e9", x0, y0, downArrowPaint, TextHelper.VerticalAlign.CENTER);
    }


    for (Cell cell: operation()) {
      float x0 = cell.col() * cellSize - originX;
      float y0 = cell.row() * cellSize - originY;
      
      if (tutorialData != null && cell.row() < fragment.countedToRow && cell.row() >= minRow) {
        int color = (fragment.counted < operation().tutorialData.optimalCellCount ? Colors.GREEN[2] : Colors.ORANGE[2]) & 0x3fffffff;
        sge.highlightCell(canvas, x0, y0, color);
        fragment.counted += cell.command() == null ? 1 : 2;
      }

      if (cell.command() == null) {
        drawConnections(cell, canvas);
      } else {
        boolean ready = cellsReady.contains(cell);
        float radius = cellSize / 6;
        drawOperator(cell, canvas, x0, y0, ready);
        
        if (cell.command().shape() == Shape.BAR) {
        	y0 += cellSize / 2 - radius;
        }
        
        for (int i = 0; i < cell.inputCount(); i++) {
          if (cell.isBuffered(i)) {
            sge.drawBuffer(canvas, x0 + (i + 0.5f) * cellSize, y0);
          }
        }
       
        for (int i = 0; i < cell.inputCount(); i++) {
          float x = x0 + i * cellSize + cellSize / 2;
          Object constant = cell.constant(i);
          if (constant != null) {
            sge.drawData(canvas, x, y0, constant, true, ready ? 1 : 0);
          } else  if (controller.isRunning()) {
            Iterator<?> ii = controller.rootEnvironment.peek(cell.dataOffset + i).iterator();
            if (ii.hasNext()) {
              Object next = ii.next();
              if (ii.hasNext()) {
                Object nextNext = ii.next();
                if (ii.hasNext()) {
                  sge.drawData(canvas, x + cellSize / 3, y0 - cellSize / 3, ii.next(), false, -4);
                }
                sge.drawData(canvas, x + cellSize / 6, y0 - cellSize / 6, nextNext, false, -2);
              }
              sge.drawData(canvas, x, y0, next, false, ready ? 1 : 0);
            }
          }
        }
      }
    }
    
    cellsReady.clear();
    for (VisualData data: controller.getAndAdvanceVisualData(fragment.speedBar.getProgress())) {
      drawData(data, canvas);
    }
    
    if (controller.isRunning()) {
      canvas.drawText(controller.status(), Views.px(getContext(), 12),
          getHeight() - debugTextPaint.getFontMetrics(null) / 2, debugTextPaint);
    }
  }
  
  
  private boolean onSelectionMove(MotionEvent ev) {
    if (pointerId != -1) {
      final int pointerIndex = ev.findPointerIndex(pointerId);
      if (pointerIndex != -1) {
        int touchCol = (int) Math.floor((ev.getX() + originX) / cellSize);
        int touchRow = (int) Math.floor((ev.getY() + originY) / cellSize);
        selection.setPosition(selection.row(), selection.col(), 
            Math.max(1, touchRow - selection.row()), Math.max(1, touchCol - selection.col()));
      }
    }
    return false;
  }
  

  private void onTouchActionDown(float x, float y) {
    changed = false;
    dragging = false;
    moved = false;
    scroll = false;
    selection.setVisibility(INVISIBLE);

    startX = lastX = x;
    startY = lastY = y;
    
    float absX = x + originX;
    float absY = y + originY;
    
    float bestD2 = 9e9f;

    int touchCol = (int) Math.floor(absX / cellSize);
    int touchRow = (int) Math.floor(absY / cellSize);

    selection.setPosition(touchRow, touchCol, 1, 1);
    
    for (int row = touchRow - 1; row <= touchRow + 1; row++) {
      for (int col = touchCol - 1; col <= touchCol + 1; col++) {
        for (Edge edge: Edge.values()) {
          if (operation().hasOutputConnector(row, col, edge) && 
              !operation().hasInputConnector(row + edge.row, col + edge.col, edge.opposite())) {
            float conX = col * cellSize + xOffset(edge);
            float conY = row * cellSize + yOffset(edge);
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
    // Dragging ends when we have connected something -- and we don't want to scroll in this
    // case. So we track this in a separate variable.
    mayScroll = !dragging;
  }
  
  boolean onTouchActionMove(float x, float y) {
    if (!moved) {
      float dx = x - startX;
      float dy = y - startY;
      if (dx * dx + dy * dy < 8 * 8) {
        return true;
      }
      moved = true;
      if (mayScroll) {
        scroll = true;
      }
    }
    if (scroll) {
      float dx = x - lastX;
      float dy = y - lastY;
      originX -= dx;
      originY -= dy;
      lastX = x;
      lastY = y; 
    } else {
      int col = (int) Math.floor((x + originX) / cellSize);
      int row = (int) Math.floor((y + originY) / cellSize);
      if (!dragging || (col == lastCol && row == lastRow)) {
        return true;
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
      Edge edge2 = null;
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
   
    invalidate();
    return true;
  }
  
  private boolean onTouchActionUp() {
    if (moved) {
      if (scroll) {
        onScaleEnd(null);
      } else if (changed) {
        fragment.afterChange();
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
      selection.setVisibility(VISIBLE);
      if (inRange(row, col)) {
        fragment.showPopupMenu(cell);
      }
    }
    pointerId = -1;
    return true;
  }
  
  
  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    // Let the scaleGestureDetector inspect all events.
    final int action = ev.getActionMasked(); //ev.getAction();
    if (selection.mode == SelectionView.Mode.CUSTOM) {
      if (action == MotionEvent.ACTION_DOWN) {
        fragment.setSelectionMode(false);
      } else if (action == MotionEvent.ACTION_MOVE) {
        return onSelectionMove(ev);
      } else {
        return false;
      }
    }
    
    scaleDetector.onTouchEvent(ev);
    if (scaleDetector.isInProgress()) {
      pointerId = -1;
      return true;
    }
    gestureDetector.onTouchEvent(ev);

    switch (action) {
    case MotionEvent.ACTION_POINTER_DOWN:
      // 2nd touch triggers scroll in any case
      if (!moved) {
        scroll = true;
      }
      return true;
      
    case MotionEvent.ACTION_DOWN:
      pointerId = ev.getPointerId(0);
      onTouchActionDown(ev.getX(), ev.getY());
      return true;

    case MotionEvent.ACTION_MOVE:
      if (pointerId != -1) {
        final int pointerIndex = ev.findPointerIndex(pointerId);
        if (pointerIndex != -1) {
          return onTouchActionMove(ev.getX(pointerIndex), ev.getY(pointerIndex));
        }
      }
      return false;

    case MotionEvent.ACTION_UP:
      if (pointerId != -1) {
        return onTouchActionUp();
      }
      return false;

    default:
      return false;
    }
  }

  @Override
  public boolean onScale(ScaleGestureDetector detector) {
    float fx = detector.getFocusX();
    float fy = detector.getFocusY();
    float scale = detector.getScaleFactor();
    
    originX += lastFocusX - fx;
    originY += lastFocusY - fy;
    
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

    setCellSize(cellSize * scale);
    invalidate();
    return true;
  }

  @Override
  public boolean onScaleBegin(ScaleGestureDetector detector) {
    selection.setVisibility(INVISIBLE);
    
    lastFocusX = detector.getFocusX();
    lastFocusY = detector.getFocusY();
    
    return onScale(detector);
  }

  /** Also called when a move gesture ends */
  @Override
  public void onScaleEnd(ScaleGestureDetector detector) {
//    originX -= lastFocusX;
//    originY -= lastFocusY;
    float scale = cellSize / initialCellSize;
    double f = Math.pow(ZOOM_STEP, Math.round(Math.log(scale) / Math.log(ZOOM_STEP)));
    scale = (float) (initialCellSize * f) / cellSize;
    setCellSize(cellSize * scale);

    originX += lastFocusX * scale - lastFocusX;
    originY += lastFocusY * scale - lastFocusY;
    
    originX = Math.round(originX / cellSize) * cellSize;
    originY = Math.round(originY / cellSize) * cellSize;
    selection.setVisibility(INVISIBLE);

    invalidate();
  }

  CustomOperation operation() {
    return fragment.operation();
  }
  


  /*
  public void reset() {
    originX = 0;
    originY = 0;
    invalidate();
  }*/
  
  public float xOffset(Edge edge) {
    if (edge == null) {
      return cellSize / 2;
    }
    switch(edge) {
    case TOP:
    case BOTTOM:
      return cellSize / 2;
    case RIGHT:
      return cellSize;
    default:
      return 0;
    }
  }

  public float yOffset(Edge edge) {
    if (edge == null) {
      return cellSize / 2;
    }
    switch (edge) {
    case LEFT:
    case RIGHT:
      return cellSize / 2;
    case BOTTOM:
      return cellSize;
    default:
      return 0;
    }
  }

  protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    if (autoZoom) {
      autoZoom(right - left, bottom - top);
      autoZoom = false;
    }
  }

  private void setCellSize(float cellSize) {
    this.cellSize = cellSize;
    sge.setCellSize(cellSize);
    gridPaint.setStrokeWidth(cellSize / 32);
  }


  public void autoZoom(int availableWidth, int availableHeight) {    
    int[] size = new int[4];
    operation().size(size);
    
    if (size[0] >= 0 && size [1] >= 0 && originX == 0 && originY == 0) {
      cellSize = Math.min(availableHeight / Math.max(8f, size[2]), 
          availableWidth / Math.max(8f, size[3] + 1));  // operator width, scrollbar

      float scale = cellSize / initialCellSize;
      double f = Math.pow(ZOOM_STEP, Math.floor(Math.log(scale) / Math.log(ZOOM_STEP)));
      scale = (float) (initialCellSize * f) / cellSize;

      selection.setVisibility(INVISIBLE);
      setCellSize(cellSize * scale);
      invalidate();
    }
  }
}
