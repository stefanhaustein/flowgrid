package org.flowgrid.model.api;

import org.flowgrid.model.Cell;
import org.flowgrid.model.Command;
import org.flowgrid.model.Shape;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Environment;
import org.flowgrid.model.Instance;
import org.flowgrid.model.ActionFactory.Action;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Property;
import org.flowgrid.model.Type;
import org.flowgrid.model.hutn.HutnSerializer;

public class PropertyCommand implements Command {
  final Property property;
  final Action action;
  final boolean implicitInstance;
  
  public PropertyCommand(Property property, Action action, boolean implicitInstance) {
    this.property = property;
    this.action = action;
    this.implicitInstance = implicitInstance;
  }
  
  
  @Override
  public void process(Cell cell, Environment environment, int remainingStackDepth) {
    Object value;
    int offset;
    Instance instance;
    if (implicitInstance) {
      offset = 0;
      instance = environment.instance();
    } else {
      offset = 1;
      instance = (Instance) environment.getData(cell.dataOffset);
    }
    switch (action) {
      case GET:
        value = instance.get(property.name());
        break;
      case SET:
        value = environment.getData(cell.dataOffset + offset);
        instance.set(property.name(), value);
        break;
      case HAS:
        value = instance.get(property.name()) != null;
        break;
      case CLEAR:
        instance.set(property.name(), null);
        value = null;
        break;
      default:
        throw new IllegalStateException("Unsupported property action: " + action);
    }

    if (!implicitInstance) {
      environment.sendData(cell.target(0), instance, remainingStackDepth);
    }
    if (action != Action.CLEAR && value != null) {
      environment.sendData(cell.target(offset), value, remainingStackDepth);
    }
  }

  public Property property() {
    return property;
  }
  
  public boolean implicitInstance() {
    return implicitInstance;
  }

  @Override
  public Shape shape() {
    return Shape.PARALLELOGRAM;
  }

  @Override
  public void serializeCommand(HutnSerializer json, CustomOperation owner) {
    json.writeString("artifact", property.moduleLocalName(owner.module()));
    json.writeString("action", action.toString());
    if (implicitInstance) {
      json.writeBoolean("implicitInstance", implicitInstance);
    }
  }

  @Override
  public String name() {
    return property.name();
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
    return (implicitInstance ? 0 : 1) + (action == Action.SET ? 1 : 0);
  }

  @Override
  public Type inputType(int index) {
    return (index == 0 && !implicitInstance) ? property.classifier : action == Action.SET ? property.type() : Type.ANY;
  }

  @Override
  public int outputCount() {
    return (implicitInstance ? 0 : 1) + (action == Action.CLEAR ? 0 : 1);
  }

  @Override
  public Type outputType(int index, Type[] inputSignature) {
    return (!implicitInstance) && index == 0 ? property.classifier : action == Action.HAS ? PrimitiveType.BOOLEAN : property.type();
  }

  public String toString() {
    return action.toString() + " " + property.name();
  }
}
