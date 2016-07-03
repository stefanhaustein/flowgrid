package org.flowgrid.model.api;

import org.flowgrid.model.Cell;
import org.flowgrid.model.Command;
import org.flowgrid.model.Shape;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Environment;
import org.flowgrid.model.Instance;
import org.flowgrid.model.Type;
import org.flowgrid.model.hutn.HutnSerializer;

public class LocalCallCommand implements Command {
  public final CustomOperation operation;

  public LocalCallCommand(CustomOperation operation) {
    this.operation = operation;
  }


  @Override
  public void process(Cell cell, Environment context, int remainingStackDepth) {
    Instance instance = context.instance();
    Environment child = new Environment(
        context.controller, context, this.operation, instance, cell, 0);
    
    for (int i = 0; i < operation.inputs.size(); i++) {
      PortCommand port = operation.inputs.get(i);
      port.sendData(child, context.getData(cell.dataOffset + i), remainingStackDepth);
    }
  }

  @Override
  public Shape shape() {
    return operation.shape();
  } 
  
  public String name() {
    String name = operation.name();
    return name.substring(name.lastIndexOf('/') + 1);
  }
  
  @Override
  public void serializeCommand(HutnSerializer json, CustomOperation owner) {
    json.writeString("artifact", operation.moduleLocalName(owner.module()));
    json.writeBoolean("implicitInstance", true);
  }

  @Override
  public void detach() {
  }

  public CustomOperation operation() {
    return operation;
  }

  @Override
  public int hasDynamicType() {
    return 0;
  }

  @Override
  public int inputCount() { return operation.inputCount() - 1; }

  @Override
  public Type inputType(int index) {
    return operation.inputType(index + 1);
  }

  @Override
  public int outputCount() {
    return operation.outputCount() - 1;
  }

  @Override
  public Type outputType(int index, Type[] inputSignature) {
    return operation.outputType(index + 1, null);
  }
}
