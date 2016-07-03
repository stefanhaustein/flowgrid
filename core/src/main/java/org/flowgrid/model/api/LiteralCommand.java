package org.flowgrid.model.api;

import org.flowgrid.model.Cell;
import org.flowgrid.model.Command;
import org.flowgrid.model.Shape;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Environment;
import org.flowgrid.model.Model;
import org.flowgrid.model.Type;
import org.flowgrid.model.hutn.HutnSerializer;

public class LiteralCommand implements Command {
  public Object value = 0;
  Type type;
  final Model model;

  public LiteralCommand(Model model, Type type, Object value) {
    this.model = model;
    this.type = type;
    if (value instanceof Number && !(value instanceof Double)) {
      value = ((Number) value).doubleValue();
    }
    this.value = value;
  }
  
  public String name() {
    if (value instanceof Number) {
      double d = ((Number) value).doubleValue();
      if (d == (int) d) {
        return String.valueOf((int) d);
      }
      return String.valueOf(d);
    } 
    if (value instanceof String) {
      return "\"" + value + '\"';
    }
    return String.valueOf(value);
  }

  @Override
  public void process(Cell cell, Environment context, int remainingStackDepth) {
    context.sendData(cell.target(0), value, remainingStackDepth);
  }

  @Override
  public void serializeCommand(HutnSerializer json, CustomOperation owner) {
    json.writeString("artifact", "/system/Literal");
    model.valueToJson(json, "value", value);
    json.writeString("type", type.moduleLocalName(owner.module()));
  }

  @Override
  public void detach() {
  }

  @Override
  public Shape shape() {
    return Shape.PARALLELOGRAM;
  }

  @Override
  public int hasDynamicType() {
    return 0;
  }

  @Override
  public int inputCount() {
    return 0;
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
