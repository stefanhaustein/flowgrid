package org.flowgrid.model.api;

import org.flowgrid.model.Environment;
import org.flowgrid.model.Module;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;

import java.util.List;

public class IfFactory extends ControlFactory {
  private static final Action[] ACTIONS = {Action.FILTER, Action.SWITCH};

  IfFactory(Module module) {
    super(module, "if", new Switchable() {
      @Override
      protected boolean process(Object input0, Environment environment, int dataOffset) {
        return (Boolean) environment.getData(dataOffset + 1);
      }

      @Override
      public int inputCount() {
        return 2;
      }

      @Override
      public Type inputType(int index) {
        return index == 0 ? Type.ANY : PrimitiveType.BOOLEAN;
      }
    });
  }

  @Override
  public Action[] actions() {
    return ACTIONS;
  }

  @Override
  public boolean matches(List<Type> inputTypes) {
    if (inputTypes.size() > 1) {
      return inputTypes.get(1) == PrimitiveType.BOOLEAN;
    }
    return true;
  }
}
