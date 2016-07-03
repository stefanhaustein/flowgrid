package org.flowgrid.model.api;

import java.util.List;

public class VectorOps {
  public static double length(List<Double> v) {
    if (v.size() == 0) {
      return 0;
    }
    double l = v.get(0);
    for (int i = 1; i < v.size(); i++) {
      double vi = v.get(i);
      l = Math.sqrt(l * l + vi * vi);
    }
    return l;
  }

}
