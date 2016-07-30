package org.flowgrid.model.api;

import org.flowgrid.model.Cell;
import org.flowgrid.model.Shape;
import org.flowgrid.model.Container;
import org.flowgrid.model.Environment;
import org.flowgrid.model.Module;
import org.flowgrid.model.Operation;
import org.flowgrid.model.Type;

public final class BranchOperation extends Operation {

  static final Type[] INPUT_SIGNATURE = {Type.ANY};

  private final boolean left;
  private final boolean middle;
  private final boolean right;

  public static void register(Module module) {
    module.addArtifact(new BranchOperation(module, "Branch left", true, true, false));
    module.addArtifact(new BranchOperation(module, "Branch right", false, true, true));
    module.addArtifact(new BranchOperation(module, "Branch left and right only", true, false, true));
    module.addArtifact(new BranchOperation(module, "Branch 3-way", true, true, true));
  }

  public BranchOperation(Container module, String name, boolean left, boolean middle, boolean right) {
    super(module, name);
    this.left = left;
    this.middle = middle;
    this.right = right;
    
    this.documentation = "Branches the input data into " + 
        (left && right && middle ? "three" : "two") + 
        " identical outputs at the corresponding postions.";
  }
    
  @Override
  public void process(Cell cell, Environment context, int remainingStackDepth) {
    Object data = context.getData(cell.dataOffset);
    if (left) {
      context.sendData(cell.target(0), data, remainingStackDepth);
    }
    if (middle) {
      context.sendData(cell.target(1), data, remainingStackDepth);
    }
    if (right) {
      context.sendData(cell.target(2), data, remainingStackDepth);
    }
  }
  
  @Override
  public Shape shape() {
    return Shape.BRANCH;
  }

  @Override
  public int hasDynamicType() {
    return 1;
  }

  @Override
  public int inputCount() {
    return 1;
  }

  @Override
  public int outputCount() {
    return 3;  // Some of which are not valid
  }

  @Override
  public Type inputType(int index) {
    return Type.ANY;
  }

  @Override
  public Type outputType(int index, Type[] inputSignature) {
    Type resultType = inputSignature == null ? Type.ANY : inputSignature[0];
    switch (index) {
    case 0:
      return left ? resultType : null;
    case 1:
      return middle ? resultType : null;
    case 2:
      return right ? resultType : null;
    }
    throw new IndexOutOfBoundsException();
  }
}
