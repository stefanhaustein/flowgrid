package org.flowgrid.model;

import org.flowgrid.model.hutn.HutnSerializer;

public interface Command {  
  void detach();

  /**
   * Bit combination of the input port types required to
   * determine the output types. 
   */
  int hasDynamicType();

  int inputCount();
  Type inputType(int index);
  int outputCount();
  Type outputType(int index, Type[] inputSignature);
  void process(Cell cell, Environment context, int remainingStackDepth);
  void serializeCommand(HutnSerializer json, CustomOperation owner);
  Shape shape();

  String name();

}
