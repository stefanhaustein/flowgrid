package org.flowgrid.model.api;

import org.flowgrid.model.Shape;
import org.flowgrid.model.annotation.Name;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class MathOperations {

  static final HashMap<String, DecimalFormat> formatCache = new HashMap<>();

  @Name("+")
  @org.flowgrid.model.annotation.Shape(Shape.CIRCLE)
  public static double add(double a, double b) {
    return a + b;
  }

  @Name("\u2212")
  @org.flowgrid.model.annotation.Shape(Shape.CIRCLE)
  public static double sub(double a, double b) {
    return a - b;
  }

  @Name("\u00d7")
  @org.flowgrid.model.annotation.Shape(Shape.CIRCLE)
  public static double mul(double a, double b) {
    return a * b;
  }
  
  @Name("\u00f7")
  @org.flowgrid.model.annotation.Shape(Shape.CIRCLE)
  public static double div(double a, double b) {
    return a / b;
  }

  public static double root(double a, double b) {
    return Math.pow(b, 1/a);
  }

  public static double clamp(double value, double min, double max) {
    return min > max ? value : value < min ? min : value > max ? max : value;
  }

  public static String format(double n, String fmt) {
    synchronized (formatCache) {
      DecimalFormat df = formatCache.get(fmt);
      if (df == null) {
        df = new DecimalFormat(fmt);
        formatCache.put(fmt, df);
      }
      String formatted = df.format(n);
      return formatted;
    }
  }
}
