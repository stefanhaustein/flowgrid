package org.flowgrid.model;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.flowgrid.model.api.CommandFactory;
import org.flowgrid.model.api.LiteralCommand;
import org.flowgrid.model.api.LocalCallCommand;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.model.hutn.Hutn;
import org.flowgrid.model.hutn.HutnArray;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.model.hutn.HutnObjectBuilder;
import org.flowgrid.model.hutn.HutnSerializer;
import org.flowgrid.model.hutn.HutnWriter;
import org.flowgrid.model.container.Grid;


public class CustomOperation extends Operation implements Iterable<Cell> {
  public static final String FILE_EXTENSION = ".fgo";

  private static final String TAG = "CustomOperation";
  
  private Grid<Cell> cells = new Grid<>();
  ArrayList<ConstantCacheEntry> constantCache = new ArrayList<>();
  int dataSize;
  int gateSize;
  public List<PortCommand> outputs = new ArrayList<>();
  public List<PortCommand> inputs = new ArrayList<>();

  List<Cell> parameterLessCache = new ArrayList<>();
  List<Type> inputSignatureCache = new ArrayList<>();
  List<Type> outputSignatureCache = new ArrayList<>();
  
  boolean validated;
  boolean loaded;
  boolean typeErrors;
  boolean asyncInput;
  BitSet bufferedCache = new BitSet();
  HutnObject savedData;
  TreeMap<String, Object> portData;
  Instance instanceData;
  private StructuredData structuredData;

  public final TutorialData tutorialData;
  
  static void put(Grid<Cell> cells, int row, int col, Cell cell) {
    cells.set(row, col, cell);
    cell.row = row;
    cell.col = col;
  }

  public CustomOperation(Container owner, String name, boolean newOperation) {
    super(owner, name);
    tutorialData = qualifiedName().startsWith("/missions/") ? new TutorialData() : null;
    this.loaded = newOperation;
    if (newOperation && classifier == null && name.equals("main")) {
      setAsyncInput(true);
    }
  }

  public void addPort(int row, int col, boolean input, boolean output, Type type, String name) {
    PortCommand portCommand = new PortCommand(name, input, output);
    portCommand.setDataType(type);
    setCommand(row, col, portCommand);
  }
  
  public boolean buffered(int index) {
    return bufferedCache.get(index);
  }

  public Cell cell(int row, int col) {
    for (int i = col; i >= cells.startColumn(); i--) {
      Cell cell = cells.get(row, i);
      if (cell != null) {
        return cell.col() + cell.width() > col ? cell : null;
      }
    }
    return null;
  }

  public boolean connect(int row, int col, Edge from, Edge to) {
    Cell cell = cell(row, col);
    if (cell != null) {
      if (cell.command() != null) {
        return false;
      } 
    } else {
      cell = new Cell(this, row, col, null);
      addCell(cell);
    }
    validated = false;
    return cell.connect(from, to);
  }

  public void connect(int row, int col, String description) {
    if (description != null && description.length() > 0) {
      Cell cell = cell(row, col);
      if (cell == null) {
        cell = new Cell(this, row, col, null);
        addCell(cell);
      }
      validated = false;
      cell.connect(description);
    }
  }
  
  
  public HutnObject copy(int row, int col, int height, int width) {
    JsonGraphBuilder graphBuilder = new JsonGraphBuilder(this, row, col, row + height, col + width, true);
    HutnObjectBuilder jsonBuilder = new HutnObjectBuilder();
    jsonBuilder.startObject();
    graphBuilder.toJson(jsonBuilder);
    jsonBuilder.endObject();
    return (HutnObject) jsonBuilder.build();
  }
  
  public void clear() {
    clear(cells.startRow(), cells.startColumn(),
        cells.endRow() - cells.startRow(),
        cells.endColumn() - cells.startColumn());
  }
  
  public void clear(int startRow, int startCol, int height, int width) {
    int endRow = Math.min(cells.endRow() + 1, startRow + height);
    int endCol = Math.min(cells.endColumn() + 1, startCol + width);
    startRow = Math.max(startRow, cells.startRow());
    startCol = Math.max(startCol, cells.startColumn());
    for (int r = startRow; r < endRow; r++) {
      for (int c = startCol; c < endCol; c++) {
        removeCell(r, c);
      }
    }
  }
  
  public void delete(boolean includeModel) {
    if (classifier != null) {
      classifier.removeOperation(this);
      classifier.save();
    } else {
      module.deleteFile(name() + FILE_EXTENSION);
      if (includeModel) {
        module.remove(this);
        module.saveSignatureCache();
      } else {
        module.sync();
      }
    }
    name = null;
  }

  public void detachAll() {
    for(Cell cell: this) {
      cell.detach();
    }
  }

  public void deleteCol(int col) {
    for (int r = cells.startRow(); r < cells.endRow(); r++) {
      removeCell(r, col);
    }
    Grid<Cell> newCells = new Grid<Cell>();
    for (Cell cell: this) {
      put(newCells, cell.row, cell.col - (cell.col > col ? 1 : 0), cell);
    }
    cells = newCells;
  }

  public void deleteRow(int row) {
    for (int c = cells.startColumn(); c < cells.endColumn(); c++) {
      removeCell(row, c);
    }
    Grid<Cell> newCells = new Grid<Cell>();
    for (Cell cell: this) {
      put(newCells, cell.row - (cell.row > row ? 1 : 0), cell.col, cell);
    }
    cells = newCells;
  }

  public void findStart(Position pos, boolean multiple, HashSet<Position> seen, Collection<Position> result) {
    if (seen.contains(pos)) {
      result.add(pos);
      return;
    }
    seen.add(pos);
    int prevRow = pos.row + pos.edge.row;
    int prevCol = pos.col + pos.edge.col;
    Cell cell = cell(prevRow, prevCol);
    if (cell == null || cell.connections == null) {
      result.add(pos);
      return;
    }
    Edge target = pos.edge.opposite();
    Position candidate = null;
    for (int i = 0; i < 4; i++) {
      if (cell.connection(i) == target) {
        if (multiple || candidate == null) {
          candidate = new Position(prevRow, prevCol, Edge.values()[i]);
          if (multiple) {
            findStart(candidate, true, seen, result);
          } 
        } else {
          // Multiple connections, stop here.
          candidate = null;
          break;
        }
      }
    }
    if (!multiple) {
      if (candidate == null) {
        result.add(pos);
      } else {
        findStart(candidate, false, seen, result);
      }
    } 
  }

  @Override
  public void fromJson(HutnObject json, SerializationType serializationType, Map<Artifact, HutnObject> deferred) {
    super.fromJson(json, serializationType, deferred);
    if (isTutorial()) {
      this.tutorialData.passedWithStars = json.getInt("passedWithStars", 0);
      this.tutorialData.order = json.getDouble("order", 0);
    }
    this.asyncInput = json.getBoolean("asyncInput", false) || isTutorial();
    
    if (serializationType == SerializationType.SIGNATURE) {
      inputSignatureCache.clear();
      HutnArray inputArray = json.getJsonArray("input");
      if (inputArray != null) {
        for (int i = 0; i < inputArray.size(); i++) {
          inputSignatureCache.add(module.typeForName(inputArray.getString(i, "")));
        }
      }
      outputSignatureCache.clear();
      HutnArray outputArray = json.getJsonArray("output");
      if (outputArray != null) {
        for (int i = 0; i < outputArray.size(); i++) {
          outputSignatureCache.add(module.typeForName(outputArray.getString(i, "")));
        }
      }
    } else {
      loaded = true;
      cellsFromJson(json, 0, 0);
      if (isTutorial()) {
        HutnObject tutorialDataJson = json.getJsonObject("tutorialData");
        if (tutorialDataJson != null) {
          tutorialData.fromJson(tutorialDataJson);
        }
      }
    }
  }
  
  static String grabKey(String s, int p0) {
    int len = s.length();
    if (p0 >= len || s.charAt(p0) == ' ') {
      return null;
    }
    int pos = p0 + 1;
    if (pos >= len || s.charAt(pos) == ' ') {
      return s.substring(p0, pos);
    }
    while(++pos < len) {
      char c = s.charAt(pos);
      boolean cont = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_';
      if (!cont) {
        break;
      }
    }
    return s.substring(p0, pos);
  }

  public void fromJsonGraph(int originRow, int originCol, HutnArray graphJson, HutnObject cellsJson) {
    for (int i = 0; i < graphJson.size(); i++) {
      int row = i + originRow;
      String s = graphJson.getString(i);
      int pos = 0;
      while (pos < s.length()) {
        int col = pos / 2 + originCol;
        String key = grabKey(s, pos);
        if (key == null) {
          pos += 2;
          continue;
        }
        if (key.charAt(0) >= 'A' && key.charAt(0) <= 'Z') {
          // Lookup
          Object o = cellsJson.get(key);
          if (o instanceof String) {
            connect(row, col, (String) o);
          } else {
            HutnObject cellJson = (HutnObject) o;
            try {
              loadCell(row, col, cellJson);
            } catch(Exception e) {
              System.err.println("Error parsing cell " + key + "@" + Position.toString(row, col) + cellJson);
              e.printStackTrace();
            }
          }
          pos += ((key.length() + 1) / 2) * 2;
        } else {
          for (int j = 0; j < key.length(); j++) {
            if (Cell.arrowToIndices.get(key.charAt(j)) == null) {
              key = key.substring(0, j);
              break;
            }
          }
          connect(row, col, key);
          pos += 2;
        } 
        
      }
    }
  }
  
  public void cellsFromJson(HutnObject json, int rowOffset, int colOffset) {
    Object cellsJson = json.get("cells");
    if (cellsJson instanceof HutnObject) {
      HutnObject cellMapJson = (HutnObject) cellsJson;
      String origin = json.getString("origin");
      boolean useGraph = json.containsKey("graph") && json.containsKey("origin");
      if (useGraph) {
        int originRow = Position.parseRow(origin);
        int originCol = Position.parseCol(origin);
        fromJsonGraph(originRow, originCol, json.getJsonArray("graph"), cellMapJson);
      }
      for (Map.Entry<String,Object> entry: cellMapJson.entrySet()) {
        String key = entry.getKey();
        if (useGraph && key.charAt(0) >= 'A' && key.charAt(0) <= 'Z') {
          continue;
        }
        int row = Position.parseRow(key) + rowOffset;
        int col = Position.parseCol(key) + colOffset;
        Object cellObject = entry.getValue();
        HutnObject cellJson;
        if (cellObject instanceof String) {
          String s = (String) cellObject;
          if (s.startsWith("/")) {
            cellJson = new HutnObject();
            cellJson.put("name", cellObject);
          } else {
            connect(row, col, s);
            continue;
          }
        } else {
          cellJson = (HutnObject) cellObject;
        }
        try {
          loadCell(row, col, cellJson);
        } catch (Exception e) {
          System.err.println("Error parsing cell " +  key + ": " + cellJson);
          e.printStackTrace();
        }
      }
    } else if (cellsJson instanceof HutnArray) {
      // Legacy case
      HutnArray cellList = (HutnArray) cellsJson;
      for (int i = 0; i < cellList.size(); i++) {
        HutnObject cellObject = cellList.getJsonObject(i);
        try {
          loadCellWithOffset(cellObject, rowOffset, colOffset);
        } catch(Exception e) {
          System.err.println("Error parsing " + cellObject);
          e.printStackTrace();
        }
      }
    }
    // Legacy case
    Object connectionsJson = json.get("connections");
    if (connectionsJson instanceof HutnArray) {
      HutnArray connectionList = (HutnArray) connectionsJson;
      if (connectionList != null) {
        for (int i = 0; i < connectionList.size(); i += 4) {
          int row = connectionList.getInt(i);
          int col = connectionList.getInt(i + 1);
          int fromIndex = connectionList.getInt(i + 2);
          int toIndex = connectionList.getInt(i + 3);
          connect(row + rowOffset, col + colOffset, Edge.forIndex(fromIndex), Edge.forIndex(toIndex));
        }
      }
    } else if (connectionsJson instanceof String) {
      String[] parts = ((String) connectionsJson).split(",");
      for (String part : parts) {
        int cut = part.length();
        while (part.charAt(cut - 1) < '0' || part.charAt(cut - 1) > '9') {
          cut--;
        }
        String pos = part.substring(0, cut);
        int row = Position.parseRow(pos);
        int col = Position.parseCol(pos);
        connect(row + rowOffset, col + colOffset, part.substring(cut));
      }
    }
  }
 

  public boolean isTutorial() {
    return tutorialData != null;
  }

  @Override
  public void ensureLoaded() {
    if (loaded) {
      return;
    }
    loaded = true;
    try {
      fromJson(Model.loadJson(model().platform.storageFileSystem(),
          jsonFilename()), SerializationType.FULL, null);
    } catch (Exception e) {
      model().platform.error("Failed loading operation " + qualifiedName(), e);
    }
  }
  
  public Type getOutputType(int row, int col, Edge edge) {
    Cell cell = cell(row, col);
    return cell == null ? null : cell.getOutputType(col - cell.col(), edge);
    
  }

  public boolean hasInputConnector(int row, int col, Edge edge) {
    Cell cell = cell(row, col);
    if (cell == null) {
      return false;
    }
    return cell.hasInputConnector(col - cell.col(), edge);
  }

  public boolean hasOutputConnector(int row, int col, Edge edge) {
    Cell cell = cell(row, col);
    return cell != null && cell.hasOutputConnector(col - cell.col(), edge);
  }
  
  public void insertCol(int col) {
    Grid<Cell> newCells = new Grid<Cell>();
    for (Cell cell: this) {
      put(newCells, cell.row, cell.col + (cell.col >= col ? 1 : 0), cell);
    }
    cells = newCells;
    for (int r = cells.startRow(); r < cells.endRow(); r++) {
      if (hasOutputConnector(r, col - 1, Edge.RIGHT) && hasInputConnector(r, col + 1, Edge.LEFT)) {
        connect(r, col, Edge.LEFT, Edge.RIGHT);
      } else if (hasOutputConnector(r, col + 1, Edge.LEFT)  && hasInputConnector(r, col - 1, Edge.RIGHT)) {
        connect(r, col, Edge.RIGHT, Edge.LEFT);
      }
    }
  }

  public void insertRow(int row) {
    Grid<Cell> newCells = new Grid<Cell>();
    for (Cell cell: this) {
      put(newCells, cell.row + (cell.row >= row ? 1 : 0), cell.col, cell);
    }
    cells = newCells;
    for (int c = cells.startColumn(); c < cells.endColumn(); c++) {
      if (hasOutputConnector(row - 1, c, Edge.BOTTOM) && hasInputConnector(row + 1, c, Edge.TOP)) {
        connect(row, c, Edge.TOP, Edge.BOTTOM);
      } else if (hasOutputConnector(row + 1, c, Edge.TOP) && hasInputConnector(row - 1, c, Edge.BOTTOM)) {
        connect(row, c, Edge.BOTTOM, Edge.TOP);
      }
    }
  }

  @Override
  public Iterator<Cell> iterator() {
    return new Iterator<Cell>() {
      Iterator<Cell> iterator = cells.iterator();
      Cell next;
      Cell previous;
      
      @Override
      public boolean hasNext() {
        if (next != null) {
          return true;
        }
        do {
          if (!iterator.hasNext()) {
            next = null;
            return false;
          }
          next = iterator.next();
        } while (next == previous);
        previous = next;
        return true;
      }
  
      @Override
      public Cell next() {
        hasNext();
        Cell result = next;
        next = null;
        return result;
     }
  
      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public void loadCellWithOffset(HutnObject json, int rowOffset, int colOffset) {
    int row = rowOffset;
    int col = colOffset;
    String pos = json.getString("pos", "");
    if (pos != null && !pos.isEmpty()) {
      row += Position.parseRow(pos);
      col += Position.parseCol(pos);
    } else {
      row += json.getInt("row");
      col += json.getInt("col");
    }
    loadCell(row, col, json);
  }
    
  public void loadCell(int row, int col, HutnObject json) {
    Model model = module.model();
    String artifactName = json.getString("artifact", "");
    Action action = null;
    boolean implicitInstance = false;

    if (!artifactName.isEmpty()) {
      if (!artifactName.startsWith("/")) {
        artifactName = module().qualifiedName() + "/" + artifactName;
      }
      implicitInstance = json.getBoolean("implicitInstance", false);
      action = JSONEnums.optEnum(json, "action", Action.INVOKE);

    } else {
      // Legacy support
      String type = json.getString("type", "Builtin");
      if ("Builtin".equals(type) || "Java".equals(type) || 
          "OperationCall".equals(type) || "Property".equals(type) || 
          "VirtualCall".equals(type)) {
        String classifierName = json.getString("classifier", "");
        if (classifierName.isEmpty()) {
          artifactName = json.getString("name", "");
        } else {
          artifactName = classifierName + "/" + json.getString("name", "");
        }
        implicitInstance = !json.getBoolean("requiresInstance", false);
        if ("Property".equals(type)) {
          action = json.getBoolean("write", false) ? Action.SET : Action.GET;
        }
      } else if ("Resource".equals(type)) {
        artifactName = json.getString("qualifiedName", "");
      } else if ("Constructor".equals(type)) {
        artifactName = json.getString("classifier", "");
        action = Action.CREATE;
      } else if ("TypeFilter".equals(type)) {
        artifactName = json.getString("filter", "");
        action = Action.FILTER;
      } else {
        artifactName = "/system/" + type;
      }
    }

    // More legacy support...
    if (artifactName.startsWith("/control/filter/")) {
      action = Action.FILTER;
      artifactName = "/control/compare/" + artifactName.substring("/control/filter/".length());
    } else if (artifactName.startsWith("/control/switch/")) {
      action = Action.SWITCH;
      if (artifactName.equals("/control/switch/?")) {
        artifactName = "/control/?";
      } else {
        artifactName = "/control/compare/" + artifactName.substring("/control/switch/".length());
      }
    } else if (artifactName.startsWith("/control/comparison/")) {
      action = Action.COMPUTE;
      artifactName = "/control/compare/" + artifactName.substring("/control/comparison/".length());
    }


    Artifact artifact = model.artifact(artifactName);
    Command command;
    if (artifact instanceof ActionFactory) {
      command = ((ActionFactory) artifact).createCommand(action, implicitInstance);
    } else if (artifact instanceof CommandFactory) {
      command = ((CommandFactory) artifact).createCommand(json, this);
    } else {
      throw new RuntimeException(
          "Artifact '" + artifactName + "' (object '" + artifact + "') is neither an ActionFactory nor a Command or CommandFactory.");
    }
    Cell cell = new Cell(this, row, col, command);
    cell.fromJson(json);
    addCell(cell);
  }

  public Module module() {
    return module;
  }
    
  /**
   * Returns where the path goes and updates the cached types on the path.
   * If any of the types change (including from null), the target cell
   * is added to newCandidates, because it may need a type update.
   */
  public Target propagateType(int row, int col, Edge edge, Type dataType, Collection<Cell> newCandidates) {
    Position pathStart = new Position(row, col, edge);
    HashSet<Position> seen = new HashSet<Position>();
 //   int i = 0;  // Fix the need for this..
    boolean typeChanged = false;
    do {
      Cell cell = cell(row, col);
//      Log.d("FlowGrid", "Propagate type at cell " + cell);
      if (cell == null) {
      //  Log.d("FlowGrid", "Returning null target b/c cell is null");
        break;
      }
      Command cmd = cell.command;
      if (cmd != null) {
        int index = col - cell.col;
        //Type[] inputSignature = cell.api.inputSignature();
        if (edge == Edge.TOP && index < cell.inputTypeCache.length) {
          Type oldType = cell.inputTypeCache[index];
          if (!dataType.equals(oldType)) {
            typeChanged = true;
            if (oldType != null) {
              dataType = Types.commonSuperType(dataType, oldType);
            }
          }
          cell.inputTypeCache[index] = dataType;
          if (cmd.inputCount() == 0 || cmd.inputType(index).isAssignableFrom(dataType)) {
            if (typeChanged) {
              newCandidates.add(cell);
            }
            return new Target(cell, index, pathStart);
          }
          System.err.println(cmd.inputType(index) + " not assignable from " + dataType + " old type: " + oldType);
          typeErrors = true;
    //      Log.d("FlowGrid", "Returning null target because of type errors: " + inputSignature[index] + " is not assignable from " + dataType);
        }
  //      Log.d("FlowGrid", "Returning null target; index: " + index + " inputSignature: " + Arrays.toString(inputSignature));
        break;
      }
      Edge target = cell.connection(edge);
      if (target == null) {
//        Log.d("FlowGrid", "Returning null target b/c of no connection");
        break;
      }
      Type oldType = cell.inputTypeCache[edge.ordinal()];
      if (!dataType.equals(oldType)) {
        typeChanged = true;
        if (oldType != null) {
          dataType = Types.commonSuperType(dataType, oldType);
        }
      }
      cell.inputTypeCache[edge.ordinal()] = dataType;
      row += target.row;
      col += target.col;
      edge = target.opposite();
      // Loop detection;
      //Position newPos = new Position(row, col, edge);
  //    Log.d("FlowGrid", "newPos: " + newPos + " seen: " + seen);
    } while (seen.add(new Position(row, col, edge)));
    return new Target(null, -1, pathStart);
  }



  public void removeCell(int row, int col) {
    Cell cell = cell(row, col);
    if (cell == null) {
      return;
    }
    
    int dataIndex = cell.dataOffset;
    int count = Math.max(1, cell.inputCount());
    validated = false;
    
    cell.detach();
    cells.remove(row, cell.col());
  
    inputs.remove(cell.command());
    outputs.remove(cell.command());
  
    if (cell.command() != null) {
      for (Cell shift: this) {
        if (shift.dataOffset >= dataIndex) {
          shift.dataOffset -= count;
          shift.gateOffset--;
        }
      }
      dataSize -= count;
      gateSize--;
    }
  }

  public void removePath(int row, int col, boolean tutorialMode) {
    Cell cell = cell(row, col);
    if (cell == null || cell.connections == null) {
      return;
    }
    validated = false;
    ArrayList<Position> starts = new ArrayList<Position>();
    HashSet<Position> seen = new HashSet<Position>();
    for (int i = 0; i < 4; i++) {
      if (cell.connection(i) != null) {
        findStart(new Position(row, col, Edge.forIndex(i)), false, seen, starts);
      }
    }
    for(Position pos: starts) {
      row = pos.row;
      col = pos.col;
      Edge edge = pos.edge;
      while (true) {
        cell = cell(row, col);
        if (cell == null || cell.connections == null) {
          break;
        }
        Edge[] connections = cell.connections;
        Edge target = connections[edge.ordinal()];
        if (target == null) break;
        if (!tutorialMode || 
            (row >= tutorialData.editableStartRow && row < tutorialData.editableEndRow)) {
          connections[edge.ordinal()] = null;
          boolean removeCell = true;
          for (int i = 0; i < connections.length; i++) {
            if (connections[i] != null) {
              removeCell = false;
              break;
            }
          }
          if (removeCell) {
            removeCell(row, col);
          }
        }
        row += target.row;
        col += target.col;
        edge = target.opposite();
      }
    }
  }

  public void setCommand(int row, int col, Command command) {
    addCell(new Cell(this, row, col, command));
  }
  
  private void addCell(Cell cell) {
    validated = false;
    if (cell.command() instanceof LiteralCommand) {
      Cell target = cell(cell.row() + 1, cell.col());
      if (target != null && target.command() != null) {
        int index = cell.col() - target.col();
        target.setConstant(index, ((LiteralCommand) cell.command()).value);
        return;
      }
    }
    
    put(cells, cell.row(), cell.col(), cell);
    if (cell.command == null) {
      cell.inputTypeCache = new Type[4];
    } else {
      cell.dataOffset = this.dataSize;
      cell.gateOffset = this.gateSize++;
      int inputCount = Math.max(cell.command.inputCount(), 1);
      cell.inputTypeCache = new Type[inputCount];
      this.dataSize += inputCount;
    
      if (cell.command() instanceof PortCommand) {
        PortCommand portCommand = (PortCommand) cell.command();
        portCommand.setCell(cell);
        if (portCommand.input) {
          int i = 0;
          for (i = 0; i < inputs.size(); i++) {
            if (inputs.get(i).cell().compareTo(cell) > 0) {
              break;
            }
          }
          inputs.add(i, portCommand);
        }
        if (portCommand.output) {
          int i = 0;
          for (i = 0; i < outputs.size(); i++) {
            if (outputs.get(i).cell().compareTo(cell) > 0) {
              break;
            }
          }
          outputs.add(i, portCommand);
        }
      }
    }
  }
  
  public String jsonFilename() {
    return qualifiedName() + FILE_EXTENSION;
  }
  
  /**
   * Fillst the array with {startRow, startColumn, endRow, endColumn}
   * @param size
   */
  public void size(int[] size) {
    size[0] = cells.startRow();
    size[1] = cells.startColumn();
    size[2] = cells.endRow();
    size[3] = cells.endColumn();
  }
  
  @Override
  public void toJson(HutnSerializer json, SerializationType serializationType) {
    super.toJson(json, serializationType);
    json.writeString("kind", "operation");
    if (isPublic) {
      json.writeBoolean("public", true);
    }
    if (asyncInput) {
      json.writeBoolean("asyncInput", true);
    }
    if (isTutorial()) {
      json.writeLong("passedWithStars", tutorialData.passedWithStars);
      json.writeDouble("order", tutorialData.order);
    }
    
    // We need to store the signature in FULL mode, too (although it is redundant
    // in this case, so we can do a shallow load from the full data after a metadata reset.
    int startIndex = classifier == null ? 0 : 1;
    json.startArray("input");
    for (int i = startIndex; i < inputCount(); i++) {
      json.writeString(inputType(i).qualifiedName());
    }
    json.endArray();
    json.startArray("output");
    for (int i = startIndex; i < outputCount(); i++) {
      json.writeString(outputType(i, null).qualifiedName());
    }
    json.endArray();
    if (serializationType == SerializationType.FULL) {
      JsonGraphBuilder graph = new JsonGraphBuilder(this,
          cells.startRow(), cells.startColumn(), cells.endRow(), cells.endColumn(), false);
      graph.toJson(json);
      if (tutorialData != null) {
        json.startObject("tutorialData");
        tutorialData.toJson(json);
        json.endObject();
      }
    }
  }

  public String toString(DisplayType type) {
    if (type == DisplayType.LIST && isTutorial()) {
      return name + (" \u2b50\u2b50\u2b50".substring(0, tutorialData.passedWithStars + 1));
    }
    return super.toString(type);
  }
  
  
  public synchronized void validate() {
    if (!validated) {
      ensureLoaded();
      this.validated = true;
      typeErrors = false;
      HashSet<Cell> candidates = new HashSet<Cell>();
 //     HashSet<Cell> validated = new HashSet<Cell>();
      bufferedCache.clear();
      constantCache.clear();
      parameterLessCache.clear();
      for (Cell cell: this) {
        cell.clearCache();
        if (cell.command != null) {
          try {
            for (int i = 0; i < cell.command.inputCount(); i++) {
              Object constant = cell.constant(i);
              if (constant != null) {
                constantCache.add(new ConstantCacheEntry(cell, i, constant));
              }
            }
           
            if (cell.buffered != 0) {
              for (int i = 0; i < cell.inputCount(); i++) {
                bufferedCache.set(i + cell.dataOffset, (cell.buffered & (1 << i)) != 0);
              }
            }
            candidates.add(cell);
          } catch(Exception e) {
            throw new RuntimeException("Validation problem for cell " + cell + " api " + cell.command, e);
          }
        }
      }
      while (candidates.size() > 0) {
        //Log.d("FlowGrid", "validation candidates: " + candidates);
        HashSet<Cell> nextRound = new HashSet<Cell>();
        for (Cell cell: candidates) {
          cell.validate(module.model(), nextRound);
          //  validated.add(cell);
          //}
        }
        candidates = nextRound;
    //    candidates.removeAll(validated);
      }
      for (Cell cell: this) {
        Command cmd = cell.command;
        if (cmd != null) {  // Type cell.inputType(0) check is for trigger-only connections
          if (cmd.inputCount() == 0 && cell.inputType(0) == null) {
            parameterLessCache.add(cell);
          }
        }
      }
    }
  }

  @Override
  public Command createCommand(Action action, boolean implicitInstance) {
    ensureLoaded();
    return implicitInstance && classifier != null ? new LocalCallCommand(this) : this;
  }

  public boolean asyncInput() {
    return asyncInput;
  }
  
  public void setAsyncInput(boolean asyncInput) {
    this.asyncInput = asyncInput;
  }

  @Override
  public int inputCount() {
    return (classifier == null ? 0 : 1) + (loaded ? inputs : inputSignatureCache).size();
  }

  @Override
  public int outputCount() {
    return (classifier == null ? 0 : 1) + (loaded ? outputs : outputSignatureCache).size();
  }

  @Override
  public Type inputType(int index) {
    if (classifier != null) {
      if (index == 0) {
        return classifier;
      }
      index--;
    }
    return loaded ? inputs.get(index).dataType() : inputSignatureCache.get(index);
  }

  @Override
  public Type outputType(int index, Type[] inputSignature) {
    if (classifier != null) {
      if (index == 0) {
        return classifier;
      }
      index--;
    }
    if (!loaded) {
      return outputSignatureCache.get(index);
    }
    PortCommand output = outputs.get(index);
    return output.dataType();
  }

  public boolean isExample() {
    return qualifiedName().startsWith("/examples/");
  }
  
  @Override
  public double order() {
    return (name.equals("main") ? ORDER_MAIN : ORDER_OPERATION) +
        (isTutorial() ? tutorialData.order : 0);
  }

  @Override
  public int hasDynamicType() {
    return 0;
  }

  @Override
  public void process(Cell cell, Environment context, int remainingStackDepth) {
    int portOffset;
    Instance instance = null;
    if (classifier != null) {
      instance = (Instance) context.getData(cell.dataOffset);
      portOffset = 1;
    } else {
      portOffset = 0;
    }
    Environment child = new Environment(
        context.controller, context, this, instance, cell, portOffset);
    
    for (int i = 0; i < inputs.size(); i++) {
      PortCommand port = inputs.get(i);
      port.sendData(child, context.getData(cell.dataOffset + i + portOffset), remainingStackDepth);
    }
    if (classifier != null) {
      context.sendData(cell.target(0), instance, remainingStackDepth);
    }
  }

  @Override 
  public Shape shape() {
    return asyncInput() ? Shape.ASYNC : super.shape();
  }

  @Override
  public void saveData() {
    save();  // Literals may have changed
    savedData = new HutnObject();
    HutnObject portDataJson = new HutnObject();
    for (Map.Entry<String,Object> e: portData.entrySet()) {
      portDataJson.put(e.getKey(), model().valueToJson(e.getValue()));
    }
    savedData.put("port", portDataJson);
    savedData.put("instance", model().valueToJson(instanceData));
    HutnWriter writer = Model.saveJson(model().platform.metadataFileSystem(), qualifiedName() + "fgd");
    Hutn.serialize(writer, savedData);
    writer.close();
  }

  /**
   * Reset data to saved values (overwriting changes from code execution).
   */
  public void resetData() {
    if (savedData != null) {
      portData = new TreeMap<>();
      HutnObject portDataJson = savedData.getJsonObject("port");
      if (portDataJson != null) {
        Iterator<String> iter = portDataJson.keySet().iterator();
        while (iter.hasNext()) {
          String key = iter.next();
          portData.put(key, model().valueFromJson(portDataJson.get(key)));
        }
      }
      if (classifier != null) {
        //instanceData = (Instance) model().valueFromJson(savedData.optJSONObject("instance"));
        if (instanceData == null) {
          instanceData = (Instance) classifier.newInstance();
        }
      }
      structuredData = null;  // because instanceData identity has changed.
    }
  }

  @Override
  public StructuredData structuredData() {
    if (savedData == null) {
      try {
        savedData = Model.loadJson(model().platform.metadataFileSystem(),
            qualifiedName() + ".fgd");
      } catch(Exception e) {
        savedData = new HutnObject();
      }
      resetData();
    }
    if (structuredData == null) {
      structuredData = new StructuredData.Interleaved(
        "instance", classifier, instanceData,
        "port", Type.ANY, new StructuredData() {
            @Override
            public Object get(String name) {
              return portData.get(name);
            }

            @Override
            public Type type(String name) {
              for (PortCommand port: portCommands()) {
                if (port.name().equals(name)) {
                  return port.dataType();
                }
              }
              throw new IllegalArgumentException("Unrecognized port: " + name);
            }

            @Override
            public void set(String name, Object value) {
              portData.put(name, value);
            }
        },
        "literal", Type.ANY, new StructuredData() {
            LiteralCommand command(String location) {
              int row = Position.parseRow(location);
              int col = Position.parseCol(location);
              Type type = Type.ANY;
              Object value = null;
              Cell cell = cell(row, col);
              if (cell != null) {
                LiteralCommand literal = (LiteralCommand) cell.command;
                return literal;
              }
              return null;
            }

            @Override
            public Object get(String name) {
              LiteralCommand cmd = command(name);
              return cmd == null ? null : cmd.value;
            }

            @Override
            public Type type(String name) {
              LiteralCommand cmd = command(name);
              return cmd == null ? Type.ANY : cmd.outputType(0, null);
            }

            @Override
            public void set(String name, Object value) {
              // This value needs to be set via the callback.
            }
        });
    }
    return structuredData;
  }

  public Iterable<PortCommand> portCommands() {
    LinkedHashSet<PortCommand> portCommands = new LinkedHashSet<PortCommand>();
    portCommands.addAll(inputs);
    portCommands.addAll(outputs);
    return portCommands;
  }
}


