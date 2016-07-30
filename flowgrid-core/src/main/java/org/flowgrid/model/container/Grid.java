package org.flowgrid.model.container;

import java.util.Iterator;
import java.util.TreeMap;

public class Grid<T> implements Iterable<T> {
  private int startRow;
  private int startCol;
  private int endRow;
  private int endCol;
  private boolean columnBoundsValid = true;
  
  private IntMap<IntMap<T>> data = new IntMap<IntMap<T>>();
  
  public T get(int row, int col) {
    IntMap<T> r = data.get(row);
    return r == null ? null : r.get(col);
  }
  
  public void set(int row, int col, T value) {
    IntMap<T> r = data.get(row);
    if (r == null) {
      r = new IntMap<T>();
      data.put(row, r);
    }
    r.put(col, value);
    if (startRow == endRow) {
      startRow = row;
      endRow = row + 1;
      startCol = col;
      endCol = col + 1;
    } else {
      if (row < startRow) {
        startRow = row;
      }
      if (row >= endRow) {
        endRow = row + 1;
      }
      if (col < startCol) {
        startCol = col;
      }
      if (col >= endCol) {
        endCol = col + 1;
      }
    }
  }
  
  
  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {
      Iterator<IntMap<T>> rowIterator = data.values().iterator();
      Iterator<T> colIterator;
      T next;
      
      @Override
      public boolean hasNext() {
        if (next != null) {
          return true;
        }
        do {
          while (colIterator == null || !colIterator.hasNext()) {
            if (!rowIterator.hasNext()) {
              return false;
            }
            colIterator = rowIterator.next().values().iterator();
          }
          next = colIterator.next();
        } while (next == null);
        return true;
      }

      @Override
      public T next() {
        hasNext();
        T result = next;
        next = null;
        return result;
     }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
  
  public int startRow() {
    return startRow;
  }
  
  public int endRow() {
    return endRow;
  }
  
  public int startColumn() {
    validateColumnBounds();
    return startCol;
  }
  
  public int endColumn() {
    validateColumnBounds();
    return endCol;
  }

  public void remove(int row, int col) {
    IntMap<T> r = data.get(row);
    if (r != null) {
      r.remove(col);
      if (r.isEmpty()) {
        data.remove(row);
      }
      if (data.isEmpty()) {
        startCol = endCol = startRow = endRow = 0;
        columnBoundsValid = true;
      } else {
        if (row == startRow) {
          startRow = data.firstKey();
        }
        if (row == endRow - 1) {
          endRow = data.lastKey() + 1;
        }
        if (col == startCol || col == endCol - 1) {
          columnBoundsValid = false;
        }
      }
    }
  }
  
  private void validateColumnBounds() {
    if (!columnBoundsValid) {
      startCol = endCol = 0;
      for (IntMap<T> row: data.values()) {
        if (startCol == endCol) {
          startCol = row.firstKey();
          endCol = row.lastKey() + 1;
        } else {
          startCol = Math.min(startCol, row.firstKey());
          endCol = Math.max(endCol, row.lastKey() + 1);
        }
      }
      columnBoundsValid = true;
    }
  }
}
