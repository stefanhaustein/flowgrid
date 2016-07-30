package org.flowgrid.model;

import java.util.List;

/**
 * Something we can create a api for..
 *
 */
public interface ActionFactory {
  public enum Action {
    INVOKE, SET, GET, FILTER, CREATE, CLEAR, HAS, COMPUTE, SWITCH, action, THIS
  }

  Command createCommand(Action action, boolean implicitInstance);

  Action[] actions();

  // TODO: What exactly is this supposed to do????
  boolean matches(List<Type> inputTypes);
}