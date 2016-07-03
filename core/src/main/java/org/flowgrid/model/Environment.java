package org.flowgrid.model;

import java.util.ArrayList;
import java.util.LinkedList;

import org.flowgrid.model.api.PortCommand;
import org.flowgrid.model.container.IntMap;

public class Environment {
  public final Environment parent;
  public final ResultCallback resultCallback;
  final CustomOperation operation;
  public final Controller controller;
  private final Instance instance;
  Object[] dataSlots;
  DataGate[] dataGates;
  public final int resultOffset;
  
  /**
   * Data queuing for any of the data slots.
   */
  IntMap<LinkedList<Object>> dataQueues;
  
  public Environment(Controller controller, Environment parent, CustomOperation operation, Instance instance, ResultCallback resultCallback, int resultOffset) {
    this.parent = parent;
    this.controller = controller;
    this.operation = operation;
    this.instance = instance;
    this.resultCallback = resultCallback;
    this.resultOffset = resultOffset;

    operation.validate();
    dataSlots = new Object[operation.dataSize];
    dataGates = new DataGate[operation.gateSize];
    for (int i = 0; i < operation.gateSize; i++) {
      dataGates[i] = new DataGate();
    }
    if (instance != null || operation.classifier == null) {
      for (ConstantCacheEntry cce : operation.constantCache) {
        setData(cce.cell, cce.index, cce.value, 0);
      }
      for (final Cell cell: operation.parameterLessCache) {
        controller.readyQueue.add(new Runnable() {
          @Override
          public void run() {
            cell.command.process(cell, Environment.this, Controller.AVAILABLE_STACK_DEPTH);
          }
        });
      } 
    }
  }
  
  public final void processIfReady(final Cell cell, final int remainingStackDepth) {
    int inputCount = cell.inputTypeCache.length;
    
    DataGate gate = dataGates[cell.gateOffset];
    synchronized (gate) {
      if (gate.available != (1 << inputCount) - 1) {
        return;
      }
      if (remainingStackDepth <= 0) {
        controller.readyQueue.add(new Runnable() {
          public void run() {
            Environment.this.processIfReady(cell, Controller.AVAILABLE_STACK_DEPTH);
          }
        });
        return;
      }
      gate.available = -1;
    }
    
    try {
      cell.command.process(cell, this, remainingStackDepth - 1);
    } catch (Exception e) {
      throw new RuntimeException ("Error procession cell " + cell +
          " in operation " + operation, e);
    }
    synchronized(gate) {
      gate.available = cell.buffered;
      
      for (int i = 0; i < inputCount; i++) {
        if ((gate.available & (1 << i)) == 0) {
          Object queued = getQueued(cell.dataOffset + i);
          if (queued != null) {
            setData(cell, i, queued, remainingStackDepth - 1);
          } else {
            dataSlots[cell.dataOffset + i] = null;
          }
        }
      }
    }
  }
  
  public Object getData(int index) {
    return dataSlots[index];
  }

  Object getQueued(int index) {
    if (dataQueues == null) {
      return null;
    }
    LinkedList<Object> queue = dataQueues.get(index);
    return queue != null && !queue.isEmpty() ? queue.remove() : null;
  }
  
  public Instance instance() {
    return instance;
  }

  public boolean isAttached() {
    return dataSlots != null;
  }


  public void sendData(Target target, Object data, int remainingStackDepth) {
    if (parent == null && controller.visual) {
      controller.sendVisualData(target, data);
    } else if (target.cell != null) {
      setData(target.cell, target.index, data, remainingStackDepth);
    }
  }


  /** 
   * Called from sendData or ViusalData.
   */
  final void setData(final Cell cell, int index, Object data, int remainingStackDepth) {
    if (cell.command.toString().equals("bounceUp")) {
      remainingStackDepth--;
    }
    int slotIndex = cell.dataOffset + index;
    if (slotIndex >= dataSlots.length) {
      return;
    }

    DataGate gate = dataGates[cell.gateOffset];
    if (cell.command instanceof CustomOperation) {
      CustomOperation op = (CustomOperation) cell.command;
      if (op.asyncInput()) {
        synchronized (gate) {
          if (gate.environment == null) {
         //   op.validate();
            gate.environment = new Environment(controller, this, op, instance, cell, 0);
          }
        }
        PortCommand port = op.inputs.get(index);
        port.sendData(gate.environment, data, remainingStackDepth - 1);
        return;
      }
    }

    synchronized(gate) {
      // input is already available and not buffered (or cell is in processing state)
      if (((gate.available & ~cell.buffered) & (1 << index)) != 0) {
        if (dataQueues == null) {
          dataQueues = new IntMap<LinkedList<Object>>();
        }
        LinkedList<Object> queue = dataQueues.get(slotIndex);
        if (queue == null) {
          queue = new LinkedList<Object>();
          dataQueues.put(slotIndex, queue);
        }
        queue.add(data);
        return;
      }
      dataSlots[slotIndex] = data;
      gate.available |= 1 << index;
    }
    processIfReady(cell, remainingStackDepth);
  }

  /**
   * Used for rendering only
   */
  public synchronized Iterable<Object> peek(int i) {
    ArrayList<Object> result = new ArrayList<Object>();
    if (dataSlots != null && dataSlots.length > i) {
      Object first = dataSlots[i];
      if (first != null) {
        result.add(first);
        if (dataQueues != null) {
          LinkedList<Object> queue = dataQueues.get(i);
          if (queue != null) {
            result.addAll(queue);
          }
        }
      }
    }
    return result;
  }
  
  /**
   * The main purpose is to synchronize on,.
   */
  private static class DataGate {
    /**
     * Bits set for available data.
     */
    int available;
    public Environment environment;
  }

}
