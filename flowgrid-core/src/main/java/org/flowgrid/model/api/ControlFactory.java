package org.flowgrid.model.api;

import org.flowgrid.model.ActionFactory;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Cell;
import org.flowgrid.model.Command;
import org.flowgrid.model.Shape;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Environment;
import org.flowgrid.model.Module;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;
import org.flowgrid.model.hutn.HutnSerializer;

import java.util.List;


public class ControlFactory extends Artifact implements ActionFactory {
  private static final Action[] ACTIONS = {Action.FILTER, Action.SWITCH, Action.COMPUTE};
  private final Switchable switchable;
  private final Command switchCommand;
  private final Command filterCommand;
  private final Command computeCommand;

  public ControlFactory(Module module, String name, Switchable switchable) {
    super(module, name);
    this.switchable = switchable;
    this.computeCommand = new ComputeCommand();
    this.filterCommand = new FilterCommand();
    this.switchCommand = new SwitchCommand();
  }

  // TODO(haustein): Let JavaCommand implemente switchable instead?
  public ControlFactory(Module module, String name, Command computeCommand, Command filterCommand, Command switchCommand) {
    super(module, name);
    this.switchable = null;
    this.computeCommand = computeCommand;
    this.filterCommand = filterCommand;
    this.switchCommand = switchCommand;
  }

  @Override
  public double order() {
    return 0;
  }

  @Override
  public Command createCommand(Action action, boolean implicitInstance) {
    switch (action) {
      case INVOKE:
      case COMPUTE:
        return computeCommand;
      case FILTER:
        return filterCommand;
      case SWITCH:
        return switchCommand;
      default:
        throw new IllegalArgumentException("Unsupported action: " + action);
    }
  }

  @Override
  public Action[] actions() {
    return ACTIONS;
  }

  @Override
  public boolean matches(List<Type> inputTypes) {
    return true;
  }


  private final class ComputeCommand implements Command {
    @Override public void detach() {}
    @Override public int hasDynamicType() { return 0; }
    @Override public int inputCount() { return switchable.inputCount(); }
    @Override public Type inputType(int index) { return switchable.inputType(index); }
    @Override public int outputCount() { return 1; }
    @Override public Type outputType(int index, Type[] inputSignature) { return PrimitiveType.BOOLEAN; }

    @Override
    public void process(Cell cell, Environment context, int remainingStackDepth) {
      Object input0 = context.getData(cell.dataOffset);
      boolean value = switchable.process(input0, context, cell.dataOffset);
      context.sendData(cell.target(0), value, remainingStackDepth);
    }

    @Override
    public void serializeCommand(HutnSerializer json, CustomOperation owner) {
      json.writeString("artifact", qualifiedName());
      json.writeString("action", Action.COMPUTE.name());
    }

    @Override public Shape shape() { return Shape.HEXAGON; }
    @Override public String name() { return name; }
  }

  private final class FilterCommand implements Command {
    @Override public void detach() {}
    @Override public int hasDynamicType() { return 3; }
    @Override public int inputCount() { return switchable.inputCount(); }
    @Override public Type inputType(int index) { return switchable.inputType(index); }
    @Override public int outputCount() {
      return 1;
    }
    @Override public Type outputType(int index, Type[] inputSignature) { return inputSignature[0]; }

    @Override
    public void process(Cell cell, Environment context, int remainingStackDepth) {
      Object input0 = context.getData(cell.dataOffset);
      boolean ok = switchable.process(input0, context, cell.dataOffset);
      if (ok) {
        context.sendData(cell.target(0), input0, remainingStackDepth);
      }
    }

    @Override
    public void serializeCommand(HutnSerializer json, CustomOperation owner) {
      json.writeString("artifact", qualifiedName());
      json.writeString("action", Action.FILTER.name());
    }

    @Override public Shape shape() { return Shape.TRAPEZOID_DOWN; }
    @Override public String name() { return name; }
  }

  private final class SwitchCommand implements Command {
    @Override public void detach() {}
    @Override public int hasDynamicType() { return 3; }
    @Override public int inputCount() { return switchable.inputCount(); }
    @Override public Type inputType(int index) { return switchable.inputType(index); }
    @Override public int outputCount() { return 2; }
    @Override public Type outputType(int index, Type[] inputSignature) { return inputSignature[0]; }

    @Override
    public void process(Cell cell, Environment context, int remainingStackDepth) {
      Object input0 = context.getData(cell.dataOffset);
      boolean value = switchable.process(input0, context, cell.dataOffset);
      context.sendData(cell.target(value ? 0 : 1), input0, remainingStackDepth);
    }

    @Override
    public void serializeCommand(HutnSerializer json, CustomOperation owner) {
      json.writeString("artifact", qualifiedName());
      json.writeString("action", Action.SWITCH.name());
    }

    @Override public Shape shape() { return Shape.RHOMBUS; }
    @Override public String name() { return name; }
  }
}
