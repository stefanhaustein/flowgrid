package org.flowgrid.model.api;

import org.flowgrid.model.Cell;
import org.flowgrid.model.Command;
import org.flowgrid.model.Shape;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Environment;
import org.flowgrid.model.ActionFactory.Action;
import org.flowgrid.model.Type;
import org.flowgrid.model.hutn.HutnSerializer;

public class TypeFilterCommand implements Command {
  private final Type type;
  
  public TypeFilterCommand(Type type) {
    this.type = type;
  }
  
  @Override
  public void process(Cell cell, Environment context, int remainingStackDepth) {
    Object o = context.getData(cell.dataOffset);
    if (type.isAssignableFrom(cell.grid().module().model().type(o))) {
      context.sendData(cell.target(0), o, remainingStackDepth);
    }
  }

  @Override
  public void serializeCommand(HutnSerializer json, CustomOperation owner) {
    json.writeString("artifact", type.qualifiedName());
    json.writeString("action", Action.FILTER.name());
  }

  @Override
  public void detach() {    
  }

  @Override
  public Shape shape() {
    return Shape.TRAPEZOID_DOWN;
  }

  @Override
  public int hasDynamicType() {
    return 0;
  }
  
  public String name() {
    return type.name();
  }

  @Override
  public int inputCount() {
    return 1;
  }

  @Override
  public Type inputType(int index) {
    return Type.ANY;
  }

  @Override
  public int outputCount() {
    return 1;
  }

  @Override
  public Type outputType(int index, Type[] inputSignature) {
    return type;
  }
}
