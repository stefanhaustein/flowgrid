package org.flowgrid.model.api;

import org.flowgrid.model.Command;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Module;
import org.flowgrid.model.Type;
import org.flowgrid.model.hutn.HutnObject;

class LiteralCommandFactory extends CommandFactory {

  LiteralCommandFactory(Module module) {
    super(module, "Literal");
  }

  @Override
  public Command createCommand(HutnObject json, CustomOperation owner) {
    Object value = model().valueFromJson(json.get("value"));
    Type type;
    try {
      type = owner.module().typeForName(json.getString("type", model().type(value).qualifiedName()));
    } catch (RuntimeException e) {
      type = model().type(value);
    }
    return new LiteralCommand(model(), type, value);
  }
}