package org.flowgrid.model;

import java.util.List;
import java.util.Map;

import org.flowgrid.model.api.PropertyCommand;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.model.hutn.HutnSerializer;

public class Property extends Member implements ActionFactory, Command {
  private Type type;
  private Object value;
  private static final Action[] ACTIONS = {Action.GET, Action.CLEAR, Action.HAS, Action.SET};
  private static final Action[] CONSTANT_ACTIONS = {Action.GET};
  private static final Action[] PRIMITIVE_ACTIONS = {Action.GET, Action.CLEAR, Action.SET};
  /**
   * Used for constants only. Initial values need to be primitive.
   */
  private Object runtimeValue;
  private int runtimeEpoch;
  private StructuredData structuredData;

  public Property(Container owner, String name, Type type, Object value) {
    super(owner, name);
    this.type = type;
    setValue(value);
  }

  public Action[] actions() {
    return classifier == null ? CONSTANT_ACTIONS : Types.isPrimitive(type) ? PRIMITIVE_ACTIONS : ACTIONS;
  }

  @Override
  public void fromJson(HutnObject json, SerializationType serializationType, Map<Artifact,HutnObject> deferred) {
    super.fromJson(json, serializationType, deferred);
    type = module.typeForName(json.getString("valueType", json.getString("type", "")));
    setValue(module.model().valueFromJson(json.get("value")));
  }
  
  
  public Type type() {
    return type;
  }

  @Override
  public void toJson(HutnSerializer json, SerializationType serializationType) {
    super.toJson(json, serializationType);
    json.writeString("type", type.moduleLocalName(module));
    json.writeString("kind", "property");
    if (value != null) {
      module.model().valueToJson(json, "value", value);
    }
  }

  public String toString(DisplayType type) {
    return name + ": " + this.type.toString() + (value() != null ? " (" + value() + ")" : "");
  }

  public void setType(Type type) {
    this.type = type;
    setValue(this.value());
  }
  
  public Object value() {
    if (value == null) {
      if (type.equals(PrimitiveType.BOOLEAN)) {
        return false;
      }
      if (type.equals(PrimitiveType.NUMBER)) {
        return 0.0;
      }
      if (type.equals(PrimitiveType.TEXT)) {
        return "";
      }
    }
    return value;
  }
  
  public void setValue(Object value) {
    if (value instanceof Number && !(value instanceof Double)) {
      value = ((Number) value).doubleValue();
    }
    Type valueType = model().type(value);
    this.value = type.isAssignableFrom(valueType) ? value : null;
  }

  @Override
  public Command createCommand(Action action, boolean implicitInstance) {
    return classifier != null ? new PropertyCommand(this, action, implicitInstance) : this;
  }

  @Override
  public boolean matches(List<Type> inputTypes) {
    return true; // TBD with options
  }
  
  /**
   * Used in Classifier.isAssignamebleFrom().
   * Property types must match exactly because we don't know if
   * they are used in read access, writhe access, or both.
   */
  @Override
  public boolean matches(Artifact specific, boolean checkVisibility) {
    if (!(specific instanceof Property)) {
      return false;
    }
    Property specificProperty = (Property) specific;
    if (checkVisibility && !visibilityMatches(specificProperty)) {
      return false;
    }
    if (!name.equals(specific.name())) {
      return false;
    }
    if (!type.isAssignableFrom(specificProperty.type)) {
      return false;
    }
    if (!specificProperty.type.isAssignableFrom(type)) {
      return false;
    }
    return true;
  }
  
  @Override
  public double order() {
    return ORDER_PROPERTY;
  }



  @Override
  public StructuredData structuredData() {
    final String expectedName = classifier == null ? "Constant value" : "Initial value";
    if (structuredData == null) {
      structuredData = new StructuredData() {
        void check(String name) {
          if (!expectedName.equals(name)) {
            throw new IllegalArgumentException("Expected '" + expectedName + "' but got '" + name + "'.");
          }
        }

        @Override
        public Object get(String name) {
          check(name);
          return value;
        }

        @Override
        public void set(String name, Object newValue) {
          check(name);
          value = newValue;
        }

        @Override
        public Type type(String name) {
          check(name);
          return type;
        }
      };
    }
    return structuredData;
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

  @Override
  public synchronized void process(Cell cell, Environment environment, int remainingStackDepth) {
    if (environment.controller.epoch() != runtimeEpoch) {
      runtimeValue = model().deepCopy(value);
      runtimeEpoch = environment.controller.epoch();
    }
    environment.sendData(cell.target(0), runtimeValue, remainingStackDepth);
  }

  @Override
  public void serializeCommand(HutnSerializer json, CustomOperation owner) {
    json.writeString("artifact", moduleLocalName(owner.module()));
  }

  @Override
  public Shape shape() {
    return Shape.PARALLELOGRAM;
  }
}
