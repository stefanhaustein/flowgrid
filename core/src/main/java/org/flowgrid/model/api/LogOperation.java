package org.flowgrid.model.api;

import org.flowgrid.model.Cell;
import org.flowgrid.model.Shape;
import org.flowgrid.model.Environment;
import org.flowgrid.model.Module;
import org.flowgrid.model.Operation;
import org.flowgrid.model.Platform;
import org.flowgrid.model.Type;

public class LogOperation extends Operation {
  private Platform platform;
  
  LogOperation(Module module) {
    super(module, "log");
    this.platform = module.model().platform;
  }

  @Override
  public int hasDynamicType() {
    return 1;
  }

  @Override
  public void process(Cell cell, Environment context, int remainingStackDepth) {
    Object input = context.getData(cell.dataOffset);
    platform.log(cell.grid().name() + " " + cell.positionToString() + ": " + input);
    context.sendData(cell.target(0), input, remainingStackDepth);
  }

  @Override
  public Shape shape() {
    return Shape.RECTANGLE;
  }

  @Override
  public int inputCount() {
    return 1;
  }

  @Override
  public int outputCount() {
    return 1;
  }

  @Override
  public Type inputType(int index) {
    return Type.ANY;
  }

  @Override
  public Type outputType(int index, Type[] inputSignature) {
    return inputSignature == null ? Type.ANY : inputSignature[0];
  }
}
