package org.flowgrid.model;

public enum Edge {
  TOP(-1,0), LEFT(0,-1), RIGHT(0,1), BOTTOM(1,0);
  private static Edge[] VALUES_CACHE;
  public final int row;
  public final int col;
  Edge(int row, int col) {
    this.row = row;
    this.col = col;
  }
  public Edge opposite() {
    switch (this) {
    case TOP:
      return BOTTOM;
    case BOTTOM:
      return TOP;
    case LEFT:
      return RIGHT;
    case RIGHT:
      return LEFT;
    }
    return null;
  }
  
  public char key() {
    return "tlrb".charAt(ordinal());
  }
  
  public static Edge forKey(char key) {
    return forIndex("tlrb".indexOf(key));
  }
  
  public static Edge forIndex(int i) {
    if (VALUES_CACHE == null) {
      VALUES_CACHE = values();
    }
    return VALUES_CACHE[i];
  }
}