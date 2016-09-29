package org.flowgrid.android;

import java.util.Timer;
import java.util.TimerTask;


public abstract class UiTimerTask extends TimerTask {

  private final MainActivity platform;
  
  public UiTimerTask(MainActivity platform) {
    this.platform = platform;
  }
  
  @Override
  public final void run() {
    platform.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        try {
          runOnUiThread();
        } catch (Exception e) {
          cancel();
          platform.error("Error in deferred execution on the UI thread", e);
        }
      }
    });
  }
  
  public void schedule(int delay) {
    new Timer().schedule(this, delay);
  }
  
  public void schedule(int delay, int period) {
    new Timer().schedule(this, delay, period);
  }
  
  abstract public void runOnUiThread();
}
