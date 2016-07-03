package org.flowgrid.model;

import java.util.Collection;
import java.util.HashMap;

import org.flowgrid.model.hutn.HutnArray;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.model.hutn.HutnSerializer;

public class Cell implements Comparable<Cell>, ResultCallback {
  public int dataOffset;
  int gateOffset;
  CustomOperation operation;
  int row;
  int col;
  final Command command;
  Edge[] connections;
  /** Valid for cells and connections */
  Type[] inputTypeCache;
  int buffered;
  Object[] constants;
  private Target[] targetCache;

  static final char[] indicesToArrow = new char[256];
  static final HashMap<Character,Integer> arrowToIndices = new HashMap<Character,Integer>();
  
  private static final void addArrow(Edge from, Edge to, char arrow) {
    int indices = from.ordinal() * 16 + to.ordinal();
    indicesToArrow[indices] = arrow;
    arrowToIndices.put(arrow, indices);
  }
  
  static {
    addArrow(Edge.TOP, Edge.BOTTOM, '\u2193');
    addArrow(Edge.TOP, Edge.LEFT, '\u21b5');
    addArrow(Edge.TOP, Edge.RIGHT, '\u21b3');
    
    addArrow(Edge.LEFT, Edge.RIGHT, '\u2192');
    addArrow(Edge.LEFT, Edge.TOP, '\u2197');
    addArrow(Edge.LEFT, Edge.BOTTOM, '\u2198');

    addArrow(Edge.RIGHT, Edge.LEFT, '\u2190');
    addArrow(Edge.RIGHT, Edge.TOP, '\u2196');
    addArrow(Edge.RIGHT, Edge.BOTTOM, '\u2199');

    addArrow(Edge.BOTTOM, Edge.TOP, '\u2191');
    addArrow(Edge.BOTTOM, Edge.LEFT, '\u21b0');
    addArrow(Edge.BOTTOM, Edge.RIGHT, '\u21b1');
  }
  
  
  public Cell(CustomOperation operation, int row, int col, Command operator) {
    this.operation = operation;
    this.row = row;
    this.col = col;
    this.command = operator;
  }
  
  
  public Type getOutputType(int index, Edge edge) {
    if (command != null) {
      if (command.shape() == Shape.BRANCH) {
        switch(edge) {
        case LEFT:
          return command.outputType(0, inputTypeCache);
        case BOTTOM:
          return command.outputType(1, inputTypeCache);
        case RIGHT:
          return command.outputType(2, inputTypeCache);
        default:
          return null;
        }
      }
      return edge == Edge.BOTTOM && index < command.outputCount() ? command.outputType(index, inputTypeCache) : null;
    }
    for (int i = 0; i < connections.length; i++) {
      if (connections[i] == edge) {
        return inputTypeCache == null || inputTypeCache[i] == null ? Type.ANY : inputTypeCache[i];
      }
    }
    return null;
  }
  
  public boolean hasOutputConnector(int index, Edge edge) {
    return getOutputType(index, edge) != null;
  }

  public boolean hasInputConnector(int index, Edge edge) {
    if (command != null) {
      return edge == Edge.TOP && index < Math.max(1, command.inputCount());
    }
    if (connections != null) {
      return connections[edge.ordinal()] != null;
    }
    return false;
  }

  @Override
  public void handleResult(Environment environment, int index, Object data, int remainingStackDepth) {
//  Cell targetCell = environment.operationCall.cell;
  // TODO(haustein) Comment what index means here
    Target target = target(index);
    environment.parent.sendData(target, data, remainingStackDepth); 
  }
  
  void clearCache() {
    targetCache = null;
    int inputCount = command == null ? 4 : Math.max(inputCount(), 1);
    if (inputCount != inputTypeCache.length) {
      operation.model().platform.log("Invalid input type cache for " + this);
      inputTypeCache = new Type[inputCount];
    }
    for (int i = 0; i < inputCount; i++) {
      inputTypeCache[i] = null;
    }
  }
  

  public Target target(int index) {
    return targetCache[index];
  }
  
  // Called on operators. Returns true if the output paths could be validated.
  boolean validate(Model model, Collection<Cell> newCandidates) {
    if (command == null) {
      return true;
    }
    
    //Log.d("FlowGrid", "Validating: " + this);

    // Make sure we don't carry over stale buffers.
    buffered &= (1 << inputCount()) - 1; 
    
    int required = command.hasDynamicType();
    int len = inputTypeCache.length;
    for (int i = 0; i < len; i++) {
      if (inputTypeCache[i] == null) {
        if (constants != null) {
          Object constant = constants[i];
          if (constant != null) {
            inputTypeCache[i] = model.type(constant);
            continue;
          } 
        }

        if ((required & (1 << i)) != 0) {
          return false;
        }
      }
    }
  //  Log.d("FlowGrid", "Enough input available for: " + this);

   // Type[] outputTypes = api.outputSignature(inputTypeCache);
    targetCache = new Target[command.outputCount()];
    if (command.shape() == Shape.BRANCH) {
      Type ot0 = command.outputType(0, inputTypeCache);
      Type ot1 = command.outputType(1, inputTypeCache);
      Type ot2 = command.outputType(2, inputTypeCache);
      if (ot0 != null) {
        targetCache[0] = operation.propagateType(row, col - 1, Edge.RIGHT, ot0, newCandidates);
      }
      if (ot1 != null) {
        targetCache[1] = operation.propagateType(row + 1, col, Edge.TOP, ot1, newCandidates);
      }
      if (ot2 != null) {
        targetCache[2] = operation.propagateType(row, col + 1, Edge.LEFT, ot2, newCandidates);
      }
    } else {
      for (int i = 0; i < command.outputCount(); i++) {
        try {
          Target t = operation.propagateType(row + 1, col + i, Edge.TOP, command.outputType(i, inputTypeCache), newCandidates);
          targetCache[i] = t;
        } catch (Exception e) {
          throw new RuntimeException("Command: " + command.getClass(), e);
        }
         
      }
    }
    return true;
  }

  void connect(String description) {
    int pos = 0;
    int len = description.length();
    while (pos < len) {
      char c = description.charAt(pos++);
      Integer indices = arrowToIndices.get(c);
      if (indices != null) {
        connect(Edge.forIndex(indices / 16), Edge.forIndex(indices % 16));
      } else {
        Edge from = Edge.forKey(c);
        Edge to = Edge.forKey(description.charAt(pos++));
        connect(from, to);
      }
    }
  }
  
  boolean connect(Edge from, Edge to) {
    if (command != null || from == to) {
      return false;
    }
    if (connections == null) {
      connections = new Edge[4];
    }
    if (connections[from.ordinal()] != null || connections[to.ordinal()] != null) {
      return false;
    }
    connections[from.ordinal()] = to;
    return true;
  }
  
  public int row() {
    return row;
  }

  public int col() {
    return col;
  }

  public CustomOperation grid() {
    return operation;
  }

  /*public void reset() {
    if (operator != null) {
      if (operator.inputSignature().length == 0) {
        operator.process(operation, 0, row, col);
      }
    }
  }*/
  

  public int inputCount() {
    return command == null ? -1 : command.inputCount();
  }

  public int outputCount() {
    return command == null ? -1 : command.outputCount();
  }

  public int width() {
    if (command == null || command.shape() == Shape.BRANCH) {
      return 1;
    }
    return Math.max(command.shape() == Shape.RECTANGLE && command.toString().length() < 5 ? 1 : 2,
        Math.max(command.inputCount(), command.outputCount()));
  }

  public Edge connection(int index) {
    return connections == null ? null : connections[index];
  }
  
  public Edge connection(Edge edge) {
    return connections == null ? null : connections[edge.ordinal()];
  }
  
  public void detach() {
    if (command != null) {
      command.detach();
    }
  }
  
  // Parse arrows before adding this.
  /*
  public Object add(Cell cell) {
    if (api != null) {
    
  }
  */
  
  public void commandToJson(HutnSerializer json) {
    if (buffered != 0) {
      json.writeLong("buffered", buffered);
    }
    if (constants != null) {
      json.startArray("constants");
      for (int i = 0; i < constants.length; i++) {
        operation.model().valueToJson(json, constants[i]);
      }
      json.endArray();
    }
    command.serializeCommand(json, operation);
  }
  
  public String connectionsToString() {
    StringBuilder sb = new StringBuilder();
    if (connections != null) {
      for (int i = 0; i < connections.length; i++) {
        if (connections[i] != null) {
          sb.append(indicesToArrow[i * 16 + connections[i].ordinal()]);
        }
      } 
    }
    return sb.toString();
  }

  /*
  public void add(Hutn.JsonSerializer cellMap, int rowOffset, int colOffset) throws JSONException {
    String pos = Position.toString(row + rowOffset, col + colOffset);
    if (command == null) {
      String s = connectionsToString();
      if (s.length() > 0) {
        cellMap.writeLong(pos, s);
      }
    } else {
      cellMap.startObject(pos);
      commandToJson(cellMap);
      cellMap.writeLong("pos", pos);
      cellMap.endObject();
    }
  }*/

  public Command command() {
    return command;
  }
  
  public boolean isConnection() {
    return command == null;
  }
 

  public void fromJson(HutnObject json) {
    if (command != null) {
      buffered = json.getInt("buffered", 0);
      HutnArray constantsJson = json.getJsonArray("constants");
      if (constantsJson != null) {
        constants = new Object[inputCount()];
        int len = Math.min(constantsJson.size(), inputCount());
        for (int i = 0; i < len; i++) {
          Object constant = operation.model().valueFromJson(constantsJson.get(i));
          if (constant != null) {
            setConstant(i, constant);
          }
        }
      }
    }
  }


  public void setBuffered(int index, boolean b) {
    if (b) {
      buffered |= 1 << index;
    } else {
      buffered &= ~(1 << index);
      if (constants != null) {
        constants[index] = null;
      }
    }
  }

  public boolean isBuffered(int i) {
    return (buffered & (1 << i)) != 0;
  }

  public Type inputType(int i) {
    if (i >= inputTypeCache.length) {
      new IndexOutOfBoundsException("inputType access OOB; index: " + i + " size: " + inputTypeCache.length).printStackTrace();
      return null;
    }
    return inputTypeCache[i];
  }
  
  public String toString() {
    return "Cell " + positionToString() + " " + command;
  }

  public String positionToString() {
    return Position.toString(row, col);
  }
  
  @Override
  public int compareTo(Cell another) {
    if (row > another.row) {
      return 1;
    }
    if (row < another.row) {
      return -1;
    }
    if (col > another.col) {
      return 1;
    } 
    if (col < another.col) {
      return -1;
    }
    return 0;
  }


  public void setConstant(int index, Object value) {
    if (constants == null) {
      if (value == null) {
        return;
      }
      constants = new Object[inputCount()];
    }
    if (value == null) {
      // Only reset buffered if there was a constant before.
      if (constants[index] != null) {
        setBuffered(index, false);
      }
    } else {
      setBuffered(index, true);
      if (value instanceof Number && !(value instanceof Double)) {
        value = Double.valueOf(((Number) value).doubleValue());
      }
    }
    constants[index] = value;
  }


  public Object constant(int i) {
    if (constants == null || i >= constants.length) {
      return null;
    }
    return constants[i];
  }
}
