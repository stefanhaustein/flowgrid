package org.flowgrid.model;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.flowgrid.model.hutn.HutnSerializer;

public class JsonGraphBuilder {
  /**
   * Stores the connections (= the "drawable" part)
   */
  ArrayList<StringBuilder> data;
  /**
   * Stores strings for more complex connections and Commands
   */
  TreeMap<String,Object> cells = new TreeMap<>();
  int originRow;
  int originCol;
  boolean relative;
  
  JsonGraphBuilder(CustomOperation op, int minRow, int minCol, int maxRow, int maxCol, boolean relative) {
    originRow = minRow;
    originCol = minCol;
    this.relative = relative;
    if (!relative && maxCol - minCol < 200 && maxRow - minRow < 1000) {
      data = new ArrayList<StringBuilder>();
    }
    for (int r = minRow; r < maxRow; r++) {
      for (int c = minCol; c < maxCol; c++) {
        Cell cell = op.cell(r, c);
        if (cell != null) {
          add(cell);
        }
      }
    }
  }

  private void addConnection(int row, int col, String connections) {
    if (data != null && connections.length() <= 2) {
      set(row, col, connections);
    } else {
      if (data != null){
        set(row, col, "*");
      }
      addReference(row, col, connections);
    }
  }

  private void addReference(int row, int col, Object o) {
    if (relative) {
      row -= originRow;
      col -= originCol;
    }
    String key = Position.toString(row, col);
    cells.put(key, o);
  }
  
  private void set(int row, int col, String s) {
    row -= originRow;
    col -= originCol;

    while (data.size() <= row) {
      data.add(new StringBuilder());
    }
    StringBuilder sb = data.get(row);
    int x = col * 2;
    while (sb.length() < x + s.length()) {
      sb.append(' ');
    }
    for (int i = 0; i < s.length(); i++) {
      sb.setCharAt(x + i, s.charAt(i));
    }
  }

  private void add(Cell cell) {
    if (cell.command == null) {
      addConnection(cell.row, cell.col, cell.connectionsToString());
    } else {
      if (data != null) {
        int max = cell.width() * 2;
        String label = Position.toString(cell.row - originRow, cell.col - originCol);
        if (label.length() > max) {
          label = "*";
        }
        set(cell.row, cell.col, label);
      }
      addReference(cell.row, cell.col, cell);
    }
  }

  public void toJson(HutnSerializer json) {
    json.writeString("origin", Position.toString(originRow, originCol));
    if (data != null) {
      json.startArray("graph");
      for (StringBuilder sb: data) {
        json.writeString(sb.toString());
      }
      json.endArray();
    }
    json.startObject("cells");
    for (Map.Entry<String, Object> entry: cells.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (value instanceof String) {
        json.writeString(key, (String) value);
      } else {
        json.startObject(entry.getKey());
        ((Cell) value).commandToJson(json);
        json.endObject();
      }
    }
    json.endObject();
  }
}
