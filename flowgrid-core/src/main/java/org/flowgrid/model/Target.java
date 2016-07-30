package org.flowgrid.model;

public class Target {
  final Position pathStart; 
  final Cell cell;
  final int index;
  
  Target(Cell cell, int index, Position pathStart) {
    this.cell = cell;
    this.index = index;
    this.pathStart = pathStart;
  }
}
