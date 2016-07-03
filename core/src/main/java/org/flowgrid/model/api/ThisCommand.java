package org.flowgrid.model.api;

import org.flowgrid.model.Cell;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.Command;
import org.flowgrid.model.Shape;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Environment;
import org.flowgrid.model.ActionFactory;
import org.flowgrid.model.Type;
import org.flowgrid.model.hutn.HutnSerializer;

public class ThisCommand implements Command {

  private Classifier classifier;

  public ThisCommand(Classifier classifier) {
    this.classifier = classifier;
  }

  @Override
  public void detach() {
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
  public int outputCount() {
    return 1;
  }

  @Override
  public Type inputType(int index) {
    return Type.ANY;
  }

  @Override
  public Type outputType(int index, Type[] inputSignature) {
    return classifier;
  }

  @Override
  public void process(Cell cell, Environment context, int remainingStackDepth) {
    context.sendData(cell.target(0), context.instance(), remainingStackDepth - 1);
  }

  @Override
  public void serializeCommand(HutnSerializer json, CustomOperation owner) {
    json.writeString("artifact", classifier.moduleLocalName(owner.module()));
    json.writeString("action", ActionFactory.Action.THIS.name());

  }

  @Override
  public Shape shape() {
    return Shape.PARALLELOGRAM;
  }

  @Override
  public String name() {
    return "this";
  }
}
