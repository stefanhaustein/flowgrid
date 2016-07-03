package org.flowgrid.model.api;

import org.flowgrid.model.Cell;
import org.flowgrid.model.Environment;
import org.flowgrid.model.Module;
import org.flowgrid.model.Operation;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;

public class LoopOperation extends Operation {

  LoopOperation(Module module) {
    super(module, "loop");
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
    return PrimitiveType.NUMBER;
  }

  @Override
  public Type outputType(int index, Type[] inputSignature) {
    return PrimitiveType.NUMBER;
  }

  @Override
  public void process(Cell cell, Environment context, int remainingStackDepth) {
    int count = ((Number) context.getData(cell.dataOffset)).intValue();
    for (double i = 1; i <= count; i++) {
      context.sendData(cell.target(0), i, remainingStackDepth);
    }
  }
}
