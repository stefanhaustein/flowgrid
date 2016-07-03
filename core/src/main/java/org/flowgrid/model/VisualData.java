package org.flowgrid.model;

import org.flowgrid.model.api.LocalCallCommand;

public class VisualData {
  private final Controller controller;
  private int col;
  private int row;
  private Edge edge;
  private Object value;
  private float progress = -0.1f;
  private Cell cellReady;
  private long timestamp;
  
  VisualData(Controller controller, int row, int col, Edge edge, Object value, long timestamp) {
    this.controller = controller;
    this.row = row;
    this.col = col;
    this.edge = edge;
    this.value = value;
    this.timestamp = timestamp;
  }

  /**
   * Returns false when this data is delivered.
   * Call site is synchronized to the controller.
   */
  boolean step(int speed) {
    Cell cell = controller.operation.cell(row, col);
    if (cell == null || !controller.isRunning()) {
      return false;
    }

    long newTime = System.currentTimeMillis();
    if (timestamp > newTime) {
      return true;
    }

    progress += (newTime - timestamp) * speed * speed / 750000f;
    timestamp = newTime;

    if (cell.command() != null) {
      int dataIndex = col - cell.col();
      if (cellReady != cell) {
        boolean ready = true;
        if (!(cell.command() instanceof LocalCallCommand) || !((LocalCallCommand) cell.command()).operation().asyncInput()) {
          int count = cell.inputCount();
          for (int i = 0; i < count; i++) {
            if (dataIndex != i && !controller.rootEnvironment.peek(cell.dataOffset + i).iterator().hasNext()) {
              ready = false;
              break;
            }
          }
        }
        if (ready) {
          cellReady = cell;
        }
      }
      if (progress >= 1 || cellReady != cell) {
        if (edge == Edge.TOP) {
          controller.rootEnvironment.setData(cell, col - cell.col(), value, 0);
        }
        return false;
      }      
    } else {
      if (progress >= 1){
        progress = 0;
        Edge endEdge = cell.connection(edge);
        if (endEdge == null) {
          return false;
        }
        row += endEdge.row;
        col += endEdge.col;
        edge = endEdge.opposite();
      }
    }
    return true;
  }

  public int col() {
    return col;
  }

  public int row() {
    return row;
  }
  
  public float progress() {
    return progress;
  }
  
  public Edge edge() {
    return edge;
  }

  public Object value() {
    return value;
  }
  
  public Cell cellReady() {
    return cellReady;
  }
}
