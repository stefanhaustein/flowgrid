package org.flowgrid.model;

import org.flowgrid.model.annotation.Blocking;

public interface Sound {
  @Blocking
  void play();
  
  /** Sampling rate in Hz */
  int samplingRate();

  int sampleCount();

  /** Length in seconds */
  double length();

  void getData(float[] target);
}
