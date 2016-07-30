package org.flowgrid.model;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.flowgrid.model.api.PortCommand;

/**

 */
public class Controller {
  static final int AVAILABLE_STACK_DEPTH = 16;

  private ArrayList<VisualData> visualData = new ArrayList<VisualData>();
  LinkedBlockingQueue<Runnable> readyQueue = new LinkedBlockingQueue<Runnable>();
  boolean visual;
  
  /**
   * Running epochs are even, stopped epochs are odd.
   */
  private int epoch;
  private ArrayList<Worker> workers = new ArrayList<Worker>();
  public final CustomOperation operation;
  public Environment rootEnvironment;
  Instance instance;
  private long visualDataTime;
  private Target lastVisualTarget;


  public Controller(CustomOperation operation) {
    this.operation = operation;
  }
  
  
  public synchronized void start() {
    if (isRunning()) {
      return;
    }
    visualDataTime = 0;
    epoch++;
    while (workers.size() > 0) {
      System.out.println("FlowGrid: Waiting for end -- active threads: " + workers.size());
      try {
        wait(10);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    visualData.clear();
    readyQueue.clear();
    rootEnvironment = new Environment(this, null, operation, instance, null, 0);
    for (int i = 0; i < Math.max(1, Runtime.getRuntime().availableProcessors() - 1); i++) {
      Worker worker = new Worker();
      workers.add(worker);
      worker.start();
    }
  }
  
  public synchronized void sendVisualData(Target target, Object data) {
    if (target == lastVisualTarget) {
      visualDataTime = Math.max(System.currentTimeMillis(), visualDataTime + 100);
    } else {
      lastVisualTarget = target;
      visualDataTime = System.currentTimeMillis();
    }
    VisualData vd = new VisualData (this, target.pathStart.row, target.pathStart.col, target.pathStart.edge, data, visualDataTime);
    visualData.add(vd);
  }
  
  public void sendData(Environment environment, Target target, Object data, int remainingStackDepth) {
    // TODO(haustein): Limit visual data to 100 or so...
    if (environment != rootEnvironment || !visual) {
      if (target.cell != null) {
        environment.setData(target.cell, target.index, data, remainingStackDepth);
      }
    } else {
      sendVisualData(target, data);
    }
  }

  public void setVisual(boolean visual) {
    this.visual = visual;
  }
  
  public synchronized Iterable<VisualData> getAndAdvanceVisualData(int speed) {
    // Prevent changes
    ArrayList<VisualData> old;
    old = visualData;
    visualData = new ArrayList<VisualData>();
    for (VisualData vd: old) {
      if (vd.step(speed)) {
        visualData.add(vd);
      }
    }
    return old;
  }
    
  public synchronized void stop() {
    if (isRunning()) {
      epoch++;
      // queues are cleared out as workers terminate
    }
  }

  public synchronized int pendingCount() {
    int workingWorkers = 0;
    for (Worker worker: workers) {
      if (worker.state != Worker.IDLING) {
        workingWorkers++;
      }
    }
    
    return workingWorkers + readyQueue.size() + visualData.size();
  }

  public synchronized String status() {
    StringBuilder sb = new StringBuilder("Worker: ");
    int workingWorkers = 0;
    for (Worker worker: workers) {
      if (worker.state != Worker.IDLING) {
        workingWorkers++;
        sb.append('\u25a0');
      } else {
        sb.append('\u25a1');
      }
    }
    sb.append(" Visual: " + visualData.size() + " Queue: " + readyQueue.size());
    return  sb.toString();
  }


  public void setInstance(Instance instance) {
    this.instance = instance;
  }

  public boolean isRunning() {
    return (epoch & 1) == 1;
  }

  public void invoke(CustomOperation operation, Instance instance, ResultCallback callback, Object... param) {
    if (isRunning()) {
      Environment child = new Environment(this, rootEnvironment, operation, instance, callback, 0);
      for (int i = 0; i < Math.min(operation.inputs.size(), param.length); i++) {
        PortCommand port = operation.inputs.get(i);
        port.sendData(child, param[i], 0);
      }
    }
  }

  class Worker extends Thread {
    static final int STARTING = 0;
    static final int WORKING = 1;
    static final int IDLING = 2;
    static final int TERMINATED = 3;
    
    private int threadEpoch;
    private int state = STARTING;
    
    Worker() {
      this.threadEpoch = Controller.this.epoch;
    }
    
    public void run() {
      try {
        while(threadEpoch == Controller.this.epoch) {
          // Log.d("FlowGrid", "tg: " + threadGeneration + " ag: " + activeGeneration + " next: " + next);
          try {
            Runnable next = readyQueue.poll(10, TimeUnit.MILLISECONDS);
            if (next != null) {
              state = WORKING;
              next.run();
            } else {
              state = IDLING;
            }
          } catch (Exception e) {
            operation.model().platform.error("Error in worker", e);
          }
        }
      }
      finally {
        synchronized(Controller.this) {
          workers.remove(this);
          visualData.clear();
          readyQueue.clear();
        }
      }
      state = TERMINATED;
    }
  }


  public int epoch() {
    return epoch;
  }

}
