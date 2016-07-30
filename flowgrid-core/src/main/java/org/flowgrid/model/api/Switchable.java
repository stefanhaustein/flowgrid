package org.flowgrid.model.api;

import org.flowgrid.model.Environment;
import org.flowgrid.model.Type;

public abstract class Switchable {

  abstract protected boolean process(Object input0, Environment environment, int dataOffset);

  public abstract int inputCount();

  public abstract Type inputType(int index);
}
