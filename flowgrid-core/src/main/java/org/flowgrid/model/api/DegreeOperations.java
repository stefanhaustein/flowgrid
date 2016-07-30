package org.flowgrid.model.api;


import org.flowgrid.model.annotation.MultipleResults;
import org.flowgrid.model.annotation.Name;

public class DegreeOperations {

  static final double DEG_TO_RAD = Math.PI / 180;
  static final double RAD_TO_DEG = 180 / Math.PI;

  @Name("acos°")
  public static double acos(double x) {
    return Math.acos(x) * RAD_TO_DEG;
  }

  @Name("asin°")
  public static double asin(double x) {
    return Math.asin(x) * RAD_TO_DEG;
  }

  @Name("atan°")
  public static double atan(double x) {
    return Math.atan(x) * RAD_TO_DEG;
  }

  @Name("cos°")
  public static double cos(double deg) {
    return Math.cos(deg * DEG_TO_RAD);
  }

  @Name("sin°")
  public static double sin(double deg) {
    return Math.sin(deg * DEG_TO_RAD);
  }

  @Name("tan°")
  public static double tan(double deg) {
    return Math.tan(deg * DEG_TO_RAD);
  }


  public static double toRadians(double d) {
    return d * DEG_TO_RAD;
  }

  @Name("toPolar°")
  @MultipleResults(2)
  public static Double[] toPolar(double x, double y) {
    return new Double[] {Math.sqrt(x*x + y*y), Math.atan2(y, x) * RAD_TO_DEG};
  }

  @Name("toCartesian°")
  @MultipleResults(2)
  public static Double[] toCartesian(double r, double t) {
    double a = t * DEG_TO_RAD;
    return new Double[] {Math.sin(a) * r, Math.cos(a) * r};
  }
}
