package org.flowgrid.model.api;

import org.flowgrid.model.Command;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Module;
import org.flowgrid.model.hutn.HutnObject;

public class PortCommandFactory extends CommandFactory {

  PortCommandFactory(Module module) {
    super(module, "Port");
  }

  @Override
  public Command createCommand(HutnObject json, CustomOperation owner) {
    PortCommand pc = new PortCommand(json.getString("name"),
        json.getBoolean("input", false), json.getBoolean("output", false));

    // We need to fall back from dataType to type because type may still be "Port"
    String dataTypeName = json.getString("dataType", json.getString("type", ""));
    if (dataTypeName != null && !dataTypeName.isEmpty()) {
      pc.type = owner.module().typeForName(dataTypeName);
    }
    pc.portJson = json.getJsonObject("peer");
    return pc;
  }

}