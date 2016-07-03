package org.flowgrid.model.api;

import org.flowgrid.model.Cell;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.Command;
import org.flowgrid.model.Shape;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Environment;
import org.flowgrid.model.Model;
import org.flowgrid.model.ActionFactory;
import org.flowgrid.model.Type;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.model.hutn.HutnSerializer;

public class ConstructorCommand implements Command {
  private Classifier type;

  public ConstructorCommand(Classifier classifier) {
    this.type = classifier;
  }

  public ConstructorCommand(Model provider, HutnObject json) {
    this(provider.classifier(json.getString("classifier")));
  }

  @Override
  public void process(Cell cell, Environment context, int remainingStackDepth) {
    context.sendData(cell.target(0), type.newInstance(), remainingStackDepth);
  }

  @Override
  public void serializeCommand(HutnSerializer json, CustomOperation owner) {
    json.writeString("artifact", type.moduleLocalName(owner.module()));
    json.writeString("action", ActionFactory.Action.CREATE.name());
  }

  public String name() {
    String name = type.name();
    return name.substring(name.lastIndexOf('/') + 1);
  }
  
  @Override
  public void detach() {
  }

  @Override
  public Shape shape() {
    return Shape.STAR;
  }

  public Classifier classifier() {
    return type;
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
