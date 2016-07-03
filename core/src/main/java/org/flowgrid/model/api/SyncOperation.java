package org.flowgrid.model.api;

import org.flowgrid.model.Cell;
import org.flowgrid.model.Shape;
import org.flowgrid.model.Container;
import org.flowgrid.model.Environment;
import org.flowgrid.model.Operation;
import org.flowgrid.model.Type;

public class SyncOperation extends Operation {

  SyncOperation(Container module) {
    super(module, "sync");
  }

  @Override
  public int hasDynamicType() {
    return 3;
  }

  @Override
  public void process(Cell cell, Environment context, int remainingStackDepth) {
    context.sendData(cell.target(0), context.getData(cell.dataOffset), remainingStackDepth);
    context.sendData(cell.target(1), context.getData(cell.dataOffset + 1), remainingStackDepth);
  }

  @Override
  public Shape shape() {
    return Shape.BAR;
  }

  @Override
  public int inputCount() {
    return 2;
  }

  @Override
  public int outputCount() {
    return 2;
  }

  @Override
  public Type inputType(int index) {
    return Type.ANY;
  }

  @Override
  public Type outputType(int index, Type[] inputSignature) {
    return inputSignature == null ? Type.ANY : inputSignature[index];
  }
}
