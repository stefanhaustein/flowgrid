package org.flowgrid.model;

public interface ResultCallback {
  void handleResult(Environment environment, int index, Object data, int remainingStackDepth); 
}
