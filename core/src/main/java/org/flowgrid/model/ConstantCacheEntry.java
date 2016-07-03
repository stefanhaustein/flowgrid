package org.flowgrid.model;

public class ConstantCacheEntry {
  final Cell cell;
  final int index;
  final Object value;

  public ConstantCacheEntry(Cell cell, int index, Object value) {
    this.cell = cell;
    this.index = index;
    this.value = value;
  }
}
