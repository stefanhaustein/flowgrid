package org.flowgrid.model;

public interface Port {

  /**
   * Free all type-specific resources for this port.
   */
  void detach();

  /**
   * Sets the value for an output or IO port.
   */
  void setValue(Object data);
  
  /**
   * Non-synchronized operation was started.
   */
  void start();
  
  /**
   * Non-synchronized operation has stopped
   */
  void stop();

  /**
   * Called from the background thread.
   * TODO(haustein): Add wantsTick() to generate a shorter list?
   */
  void timerTick(int count);
  
  /**
   * Called from synchronized operations to trigger a single value.
   */
  void ping();
}
