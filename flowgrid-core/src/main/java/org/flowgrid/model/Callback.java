package org.flowgrid.model;

public abstract class Callback<T> {
  public abstract void run(T value);
  public void cancel() {}
}
