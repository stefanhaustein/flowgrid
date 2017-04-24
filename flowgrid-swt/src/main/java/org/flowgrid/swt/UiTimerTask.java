package org.flowgrid.swt;

import org.eclipse.swt.widgets.Display;

import java.util.Timer;
import java.util.TimerTask;


public abstract class UiTimerTask extends TimerTask {

    private final Display display;

    public UiTimerTask(Display display) {
        this.display = display;
    }

    @Override
    public final void run() {
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    runOnUiThread();
                } catch (Exception e) {
                    cancel();
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