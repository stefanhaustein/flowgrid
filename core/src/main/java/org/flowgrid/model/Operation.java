package org.flowgrid.model;

import java.util.List;

import org.flowgrid.model.hutn.HutnSerializer;


public abstract class Operation extends Member implements ActionFactory, Command {
  private static final Action[] ACTIONS = {Action.INVOKE};

  protected Operation(Container owner, String name) {
    super(owner, name);
  }
  
  public abstract int inputCount();
  
  public abstract int outputCount();
  
  public abstract Type inputType(int index);
  
  public abstract Type outputType(int index, Type[] inputSignature);

  @Override
  public Action[] actions() {
    return ACTIONS;
  }

  /**
   * Used in assignableFrom
   */
  public boolean matches(Artifact specific, boolean checkVisibility) {
    if (!(specific instanceof Operation)) {
      return false;
    }
    Operation specificOperation = (Operation) specific;
    if (checkVisibility && !visibilityMatches(specificOperation)) {
      return false;
    }
    if (inputCount() != specificOperation.inputCount() ||
        outputCount() != specificOperation.outputCount()) {
      return false;
    }
    // The concrete parameters must be assignable from the interface parameter types
    for (int i = 1; i < inputCount(); i++) {
      if (!specificOperation.inputType(i).isAssignableFrom(inputType(i))) {
        return false;
      }
    }
    // the interface output must be assignable from the specific operation params.
    for (int i = 1; i < outputCount(); i++) {
      if (!outputType(i, null).isAssignableFrom(specificOperation.outputType(i, null))) {
        return false;
      }
    }
    return true;
  }
  
  @Override
  public boolean matches(List<Type> available) {
    if (available != null) {
      for (int i = 0; i < Math.min(inputCount(), available.size()); i++) {
        if (!inputType(i).isAssignableFrom(available.get(i))) {
          System.out.println("Available: " + available + " but input type(" + i + ") is " + inputType(i));
          return false;
        }
      }
    }
    return true;
  }
  
  
  @Override
  public int hasDynamicType() {
    return 0;
  }


  public double order() {
    return ORDER_OPERATION;
  }
  

  @Override
  public void serializeCommand(HutnSerializer json, CustomOperation owner) {
    json.writeString("artifact", moduleLocalName(owner.module()));
  }

  @Override
  public Shape shape() {
    return classifier == null ? Shape.RECTANGLE : Shape.ROUNDED_RECTANGLE;
  }

  public String toString(DisplayType type) {
    int sub = classifier == null ? 0 : 1;
    switch (type) {
      case MENU:
      case LIST:
        return name() + " (" + (inputCount() - sub) + " : " + (outputCount() - sub)  + ")";
      case DETAILED: {
        StringBuilder sb = new StringBuilder();
        if (isPublic) {
          sb.append("public ");
        }
        sb.append(name);
        sb.append(" (");
        for (int i = sub; i < inputCount(); i++) {
          if (i > sub) {
            sb.append(", ");
          }
          sb.append(inputType(i).toString());
        }
        sb.append(')');
        if (outputCount() > sub) {
          sb.append(": ");
          for (int i = sub; i < outputCount(); i++) {
            if (i > sub) {
              sb.append(", ");
            }
            sb.append(outputType(i, null).toString());
          }
        }
        return sb.toString();
      }
      case TITLE:
        return "Operation '" + name() + "'";
      default:
        return super.toString();
    }
  }

  @Override
  public void detach() {
  }
  
  @Override
  public Command createCommand(Action action, boolean implicitInstance) {
    return this;
  }
}
