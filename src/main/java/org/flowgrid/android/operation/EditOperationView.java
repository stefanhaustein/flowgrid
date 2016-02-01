package org.flowgrid.android.operation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.flowgrid.android.Views;
import org.flowgrid.android.graphics.Colors;
import org.flowgrid.android.port.TestPort;
import org.flowgrid.model.ArrayType;
import org.flowgrid.model.Artifact;
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
import android.os.Handler;
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
  private ZoomData zoomData;

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

    sge = new ScaledGraphElements(fragment.platform(), operation());
    cellSize *= fragment.getActivity().getResources().getDisplayMetrics().density;
    initialCellSize = cellSize;

    selection.setVisibility(INVISIBLE);

    sge.setState(originX, originY, cellSize);
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

    float x = col * cellSize + sge.xOffset(edge) * (1-progress) + sge.xOffset(target) * progress - originX;
    float y = row * cellSize + sge.yOffset(edge) * (1-progress) + sge.yOffset(target) * progress - originY;
    sge.drawData(canvas, x, y, data.value(), false, 0);
  }
  



  private boolean inRange(int row, int col) {
    return !fragment.tutorialMode || 
        (row >= operation().tutorialData.editableStartRow &&
            row < operation().tutorialData.editableEndRow && col >= 0);
  }
  

  protected void zoomOpen(Cell cell) {
    int[] size = new int[4];
    CustomOperation operation =  (CustomOperation) cell.command();
    operation.size(size);

    zoomData = new ZoomData();
    zoomData.cell = cell;
    zoomData.operation = operation;
    zoomData.rows = size[2] + 2;
    zoomData.targetCellSize = size[2] * autoZoom(zoomData.operation, getWidth(), getHeight());
    zoomData.endTime = System.currentTimeMillis() + 1000;

    zoomData.originalOriginX = originX;
    zoomData.originalOriginY = originY;
    zoomData.orginalCellSize = cellSize;

    zoomData.targetOriginX = cell.col() * zoomData.targetCellSize;
    zoomData.targetOriginY = cell.row() * zoomData.targetCellSize;
    postInvalidate();
  }

  class ZoomData {
    Cell cell;
    CustomOperation operation;
    int rows;
    long endTime;

    float originalOriginX;
    float originalOriginY;
    float orginalCellSize;
    float targetOriginX;
    float targetOriginY;
    float targetCellSize;
  }

  
  @Override
  protected void onDraw(android.graphics.Canvas canvas) {
    int minRow = 0;
    int maxRow = 0;

    gridPaint.setStrokeWidth(cellSize / 32);

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

      boolean ready = cell.command() != null && cellsReady.contains(cell);

      sge.drawCell(canvas, cell, ready);

      if (cell.command() != null) {
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

    if (zoomData != null) {
      float x0 = zoomData.cell.col() * cellSize - originX;
      float y0 = zoomData.cell.row() * cellSize - originY;

      canvas.drawRect(x0, y0, x0 + zoomData.cell.width() * cellSize, y0 + cellSize, sge.operatorBoxPaint);
      canvas.drawRect(x0, y0, x0 + zoomData.cell.width() * cellSize, y0 + cellSize, sge.operatorOutlinePaint);

      ScaledGraphElements childSge = new ScaledGraphElements(fragment.platform(), zoomData.operation);
      childSge.setState(-x0, -y0, cellSize / zoomData.rows);
      for (Cell childCell: zoomData.operation) {
        childSge.drawCell(canvas, childCell, false);
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

    if (zoomData != null) {
      float fraction = 1f - (zoomData.endTime - System.currentTimeMillis()) / 1000f;
      if (fraction > 1) {
        originX = zoomData.originalOriginX;
        originY = zoomData.originalOriginY;
        cellSize = zoomData.orginalCellSize;
        fragment.platform().openArtifact(zoomData.operation);
        zoomData = null;
      } else {
        cellSize = zoomData.orginalCellSize * (1 - fraction) + zoomData.targetCellSize * fraction;
        originX = zoomData.originalOriginX * (1 - fraction) + zoomData.targetOriginX * fraction;
        originY = zoomData.originalOriginY * (1 - fraction) + zoomData.targetOriginY * fraction;
        postInvalidate();
      }
      sge.setState(originX, originY, cellSize);
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
      sge.setState(originX, originY, cellSize);
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

    cellSize *= scale;
    sge.setState(originX, originY, cellSize);
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
    cellSize *= scale;

    originX += lastFocusX * scale - lastFocusX;
    originY += lastFocusY * scale - lastFocusY;
    
    originX = Math.round(originX / cellSize) * cellSize;
    originY = Math.round(originY / cellSize) * cellSize;
    selection.setVisibility(INVISIBLE);
    sge.setState(originX, originY, cellSize);
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


  protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    if (autoZoom  && originX == 0 && originY == 0) {
      cellSize = autoZoom(operation(), right - left, bottom - top);
      autoZoom = false;
      sge.setState(originX, originY, cellSize);
      invalidate();
    }
  }


  public float autoZoom(CustomOperation op, int availableWidth, int availableHeight) {
    int[] size = new int[4];
    op.size(size);
    
    if (size[0] >= 0 && size [1] >= 0) {
      float newCellSize = Math.min(availableHeight / Math.max(8f, size[2]),
          availableWidth / Math.max(8f, size[3] + 1));  // operator width, scrollbar

      float scale = newCellSize / initialCellSize;
      double f = Math.pow(ZOOM_STEP, Math.floor(Math.log(scale) / Math.log(ZOOM_STEP)));
      scale = (float) (initialCellSize * f) / newCellSize;

      //selection.setVisibility(INVISIBLE);

      return newCellSize * scale;
    }
    return 32 * fragment.getActivity().getResources().getDisplayMetrics().density;
  }
}
