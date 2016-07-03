package org.flowgrid.model;

import java.util.Locale;

public class Position {
  public final int row;
  public final int col;
  public final Edge edge;

  public static String toString(int row, int col) {
    StringBuilder sb = new StringBuilder();
    if (col < 0) {
      sb.append('-');
      col = -col;
    }
    do {
      sb.append((char) ('a' + (col % 26)));
      col /= 26;
    } while(col > 0);
    sb.append(row);
    return sb.toString();
  }
  
  public static int parseCol(String s) {
    if (s.charAt(0) >= 'A' && s.charAt(0) <= 'Z') {
      return parseRow(s.toLowerCase(Locale.US));
    }
    int pos = 0;
    boolean neg = false;
    if (s.charAt(0) == '-') {
      neg = true;
      pos++;
    }
    int col = 0;
    do {
      col = col * 26 + (s.charAt(pos++) - 'a');
    } while (s.charAt(pos) >= 'a' && s.charAt(pos) <= 'z');
    return neg ? -col : col;
  }
  
  public static int parseRow(String s) {
    if (s.charAt(0) >= 'A' && s.charAt(0) <= 'Z') {
      return parseCol(s.toLowerCase(Locale.US));
    }
    int pos = 1;
    while(s.charAt(pos) >= 'a' && s.charAt(pos) <= 'z') {
      pos++;
    }
    return Integer.parseInt(s.substring(pos));
  }
  
  public Position(int row, int col, Edge edge) {
    this.row = row;
    this.col = col;
    this.edge = edge;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Position)) {
      return false;
    }
    Position p2 = (Position) other;
    boolean result = row == p2.row && col == p2.col && edge == p2.edge;
    return result;
  }
  
  @Override
  public int hashCode() {
    return col + 17 * row + 31 * edge.ordinal();
  }
  
  public String toString() {
    return "" + row + "/" + col + "/" + edge;
  }
}
