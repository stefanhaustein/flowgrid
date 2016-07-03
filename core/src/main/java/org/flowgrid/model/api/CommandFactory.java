package org.flowgrid.model.api;

import org.flowgrid.model.Artifact;
import org.flowgrid.model.Command;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Module;
import org.flowgrid.model.hutn.HutnObject;

/**
 * Artifact for constructing commands that are not just simple actions on this artifact,
 * but require additional data. For simple actions on an existing Artifact, let the
 * Artifact implement ActionFactory instead.
 */
public abstract class CommandFactory extends Artifact {

  CommandFactory(Module module, String name) {
    super(module, name);
  }

  @Override
  public double order() {
    return 0;
  }

  public abstract Command createCommand(HutnObject json, CustomOperation owner);
}
