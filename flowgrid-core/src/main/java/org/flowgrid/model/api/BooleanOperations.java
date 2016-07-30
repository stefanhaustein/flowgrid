package org.flowgrid.model.api;

import org.flowgrid.model.Shape;

public class BooleanOperations {
  
  @org.flowgrid.model.annotation.Shape(Shape.SQUARE)
  public static boolean and(boolean a, boolean b) {
    return a && b;
  }

  @org.flowgrid.model.annotation.Shape(Shape.SQUARE)
  public static boolean or(boolean a, boolean b) {
    return a || b;
  }

  @org.flowgrid.model.annotation.Shape(Shape.SQUARE)
  public static boolean not(boolean a) {
    return !a;
  }

  @org.flowgrid.model.annotation.Shape(Shape.SQUARE)
  public static boolean xor(boolean a, boolean b) {
    return a ^ b;
  }
}
