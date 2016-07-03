package org.flowgrid.model.api;

import org.flowgrid.model.Cell;
import org.flowgrid.model.Command;
import org.flowgrid.model.Shape;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Environment;
import org.flowgrid.model.Port;
import org.flowgrid.model.Type;
import org.flowgrid.model.hutn.Hutn;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.model.hutn.HutnSerializer;


public class PortCommand implements Command {
  public final boolean input;  // receives data
  public final boolean output;  // emits data
  Type type = Type.ANY;
  String name = "";
  Cell cell;
  private Port port;
  HutnObject portJson;

  public PortCommand(String name, boolean input, boolean output) {
    this.name = name;
    this.input = input;
    this.output = output;
  }

  public void setCell(Cell cell) {
    this.cell = cell;
  }

  public void setPort(Port port) {
    this.port = port;
  }

  public HutnObject peerJson() {
    if (portJson == null) {
      portJson = new HutnObject();
    }
    return portJson;
  }

  public void serializeCommand(HutnSerializer json, CustomOperation owner) {
    json.writeString("artifact", "/system/Port");
    if (input) {
      json.writeBoolean("input", input);
    }
    if (output) {
      json.writeBoolean("output", output);
    }
    json.writeString("name", name);
    json.writeString("type", type.moduleLocalName(owner.module()));
    if (portJson != null) {
      Hutn.serialize(json, "peer", portJson);
    }
  }

  @Override
  public void process(Cell cell, Environment environment, int remainingStackDepth) {
    if (!output) {
      return;
    }
    Object data = environment.getData(cell.dataOffset);
    // Runtime check needs to go first because there may be listeners when the grid is being editied
    if (environment.resultCallback != null) {
      int index = cell.grid().outputs.indexOf(this) + environment.resultOffset;
      environment.resultCallback.handleResult(environment, index, data, remainingStackDepth);
    } else if (port != null) {
      if (data != null) {
        port.setValue(data);
      }
    }
  }

  @Override
  public void detach() {
    if (port != null) {
      port.detach();
      port = null;
    }
  }

  @Override
  public Shape shape() {
    return Shape.OVAL;
//    return input && output ? Kind.INPUT_OUTPUT : input ? Kind.INPUT : Kind.OUTPUT;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDataType(Type dataType) {
    type = dataType;
  }

  public Type dataType() {
    return type;
  }

  public void sendData(Environment context, Object value, int remainingStackDepth) {
    context.sendData(cell.target(0), value, remainingStackDepth);
  }

  public String toString() {
    return name;
  }

  public String name() {
    return name;
  }

  @Override
  public int hasDynamicType() {
    return 0;
  }

  public Port port() {
    return port;
  }

  public Cell cell() {
    return cell;
  }

  @Override
  public int inputCount() {
    return output ? 1 : 0;
  }

  @Override
  public Type inputType(int index) {
    return output ? type : Type.ANY;
  }

  @Override
  public int outputCount() {
    return input ? 1 : 0;
  }

  @Override
  public Type outputType(int index, Type[] inputSignature) {
    return type;
  }
}
