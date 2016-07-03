package org.flowgrid.model;

import java.util.List;

import org.flowgrid.model.api.TypeFilterCommand;

public abstract class AbstractType extends Artifact implements Type {

  protected final Class<?> javaClass;
  private static final Action[] ACTIONS = {Action.FILTER};

  protected AbstractType(String name, Class<?> javaClass) {
    super(null, name);
    this.javaClass = javaClass;
  }

  @Override
  public Action[] actions() {
    return ACTIONS;
  }

  /**
   * Overwritten in classifier.
   */
  public boolean isAssignableFrom(Type type) {
 /*   if (javaClass == Instance.class && type.javaClass == Instance.class) {
      return false;
    } */
    return javaClass.isAssignableFrom(type.javaClass());
  }

  
  public Class<?> javaClass() {
      return javaClass;
  }

  @Override
  public double order() {
    return ORDER_TYPE;
  }
  

  @Override
  public Command createCommand(Action action, boolean implicitInstance) {
    if (action == Action.FILTER) {
      return new TypeFilterCommand(this);
    }
    throw new IllegalArgumentException();
  }

  @Override
  public boolean matches(List<Type> inputTypes) {
    return true;
  }
}
