package org.flowgrid.model.api;

import org.flowgrid.model.annotation.MultipleResults;

public class Color {
  final int argb;

  static private int percentToInt8(double n) {
    return Math.max(0, Math.min(255, (int) Math.round(n * 2.55)));
  }

  static private int fractionToInt8(double n) {
    return Math.max(0, Math.min(255, (int) Math.round(n * 255)));
  }

  static private double int8ToPercent(int i) {
    return i / 2.55;
  }

  static private double int8ToFraction(int i) {
    return i / 255.0;
  }

  public static Color fromRgbA(double r, double g, double b, double a ) {
    return new Color((percentToInt8(a) << 24) | (percentToInt8(r) << 16) |
        (percentToInt8(g) << 8) | (percentToInt8(b)));
  }

  public static Color fromRgb(double r, double g, double b) {
    return fromRgbA(r, g, b, 100);
  }

  public static Color fromHsvA(double h, double s, double v, double a) {
    s /= 100;
    // v /= 100;
    int hi = (int) (h/60);
    double f = f = h/60 - hi;
    double p = v*(1 - s);
    double q = v*(1 - s*f);
    double t = v*(1 - s*(1 - f));
    double r;
    double g;
    double b;
    switch(hi) {
      case 1:
        r = v;
        g = t;
        b = p;
        break;
      case 2:
        r = q;
        g = v;
        b = p;
        break;
      case 3:
        r = p;
        g = q;
        b = v;
        break;
      case 4:
        r = t;
        g = p;
        b = v;
        break;
      case 5:
        r = v;
        g = p;
        b = q;
        break;
      default:
        r = v;
        g = t;
        b = p;
    }
    return fromRgbA(r, g, b, a);
  }

  public static Color fromHsv(double h, double s, double v) {
    return fromHsvA(h, s, v, 100);
  }

  @MultipleResults(4)
  public Double[] toHsvA() {
    double r = ((argb >>> 16) & 255) / 255.0;
    double g = ((argb >>> 8) & 255) / 255.0;
    double b = (argb & 255) / 255.0;
    double max = Math.max(Math.max(r, g), b);
    double min = Math.min(Math.min(r, g), b);
    double mm = max - min;
    double h;
    double s;
    if (mm == 0) {
      h = 0;
      s = 0;
    } else {
      if (max == r) {
        h = 60 * (g - b)/mm;
      } else if (max == g) {
        h = 60 * (2 + (b - r)/mm);
      } else {
        h = 60 * (4 + (r - g)/mm);
      }
      s = 100*mm/max;
    }
    if (h < 0) {
      h += 360;
    }
    return new Double[]{h, s, 100 * max, int8ToPercent(argb >>> 24)};
  }

  @MultipleResults(3)
  public Double[] toHsv() {
    return toHsvA();
  }

  @MultipleResults(4)
  public Double[] toRgbA() {
    return new Double[] {int8ToPercent(argb >>> 24), int8ToPercent((argb >>> 16) & 255),
      int8ToPercent((argb >>> 8) & 255), int8ToPercent(argb & 255)};
  }

  @MultipleResults(3)
  public Double[] toRgb() {
    return toRgbA();
  }

  public String toString() {
    return String.format("#%08X", argb);
  }

  private Color(int argb) {
    this.argb = argb;
  }


  public int argb() {
    return argb;
  }
}

