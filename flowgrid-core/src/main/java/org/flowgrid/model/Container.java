package org.flowgrid.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.flowgrid.model.hutn.HutnArray;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.model.hutn.HutnSerializer;
import org.flowgrid.model.hutn.HutnWriter;
import org.flowgrid.model.io.Files;
import org.flowgrid.model.io.Stat;
import org.flowgrid.model.io.StatusListener;


public abstract class Container extends Artifact implements Iterable<Artifact> {
  private static final String[] EMPTY_STRING_ARRAY = {};
  public static final String SIGNATURE_CACHE_FILE_EXTENSION = ".cache";
  public static final String[] ARTIFACTS_ALIASES = 
    {"artifacts", "constants", "operations", "properties"};

  enum State {
    STUB, LOADING, LOADED
  }
  
  protected State state = State.STUB;
  protected TreeMap<String, Artifact> artifacts = new TreeMap<>();

  /**
   * Map of artifact names to their file timestamps. Used to short-circuit sync where the
   * corresponding file is unchanged and thus does not need to be re-loaded.
   */
  protected HashMap<String, Long> artifactFileTimestamps = new HashMap<>();

  protected Container(Container owner, String name) {
    super(owner, name);
  }

  public Artifact artifact(String name) {
    ensureLoaded();
    return artifacts.get(name);
  }
    
  public <T extends Artifact> T artifact(String name, Class<T> type) {
    Artifact result = artifact(name);
    if (result == null || !type.isAssignableFrom(result.getClass())) {
      throw new RuntimeException(type.getSimpleName() + " named '" + name + 
          "' not found in module " + qualifiedName());
    }
    return (T) result;
  }

  abstract boolean hasDirectory();

  @Override
  public void copyData(String targetName, StatusListener statusListener) throws IOException {
    super.copyData(targetName, statusListener);
    if (hasDirectory()) {
      Files.copyAll(model().platform.storageRoot(), qualifiedName() + "/",
          model().platform.storageRoot(), targetName, statusListener);
    }
  }

  @Override
  public void deleteData(StatusListener statusListener) throws IOException {
    super.deleteData(statusListener);
    if (hasDirectory()) {
      Files.deleteAll(model().platform.storageRoot(), qualifiedName() + "/", null);
    }
  }

  @Override
  public void ensureLoaded() {
    if (state != State.STUB) {
      return;
    }
    // Load own data, create stubs for artifacts included in the main JSON
    state = State.LOADING;
    final Map<Artifact,HutnObject> todoFull = new HashMap<Artifact,HutnObject>();
    try {
      System.out.println("LoadJson: " + jsonFilename());
      HutnObject json = Model.loadJson(model().platform.storageRoot(), jsonFilename());
      model().platform.log("Processing " + qualifiedName());
      fromJson(json, SerializationType.FULL, todoFull);
    } catch (Exception e) {
      model().platform.info("Loading '" + jsonFilename() + "' failed.", e);
    }
    
    HutnObject signaturesJson = null;
    boolean needsSave = false;

    if (!jsonFilename().endsWith(".fgi")) {
      try {
        if (Files.exists(model().platform.cacheRoot(), jsonFilename() + SIGNATURE_CACHE_FILE_EXTENSION)) {
          signaturesJson = Model.loadJson(model().platform.cacheRoot(), jsonFilename() +
              SIGNATURE_CACHE_FILE_EXTENSION);
        }
      } catch (Exception e) {
        model().platform.log("Signature cache error for " + qualifiedName(), e);
      }
      if (signaturesJson != null) {
        try {
          final Map<Artifact, HutnObject> todoShallow = new HashMap<Artifact, HutnObject>();
          fromJson(signaturesJson, SerializationType.SIGNATURE, todoShallow);
          HutnObject timestampsJson = signaturesJson.getJsonObject("timestamps");
          if (timestampsJson != null) {
            Iterator<String> it = timestampsJson.keySet().iterator();
            while (it.hasNext()) {
              String key = it.next();
              artifactFileTimestamps.put(key, timestampsJson.getLong(key));
            }
          }
          processDeferred(todoShallow, SerializationType.SIGNATURE);
        } catch (Exception e) {
          model().platform.log("Error parsing module JSON", e);
          signaturesJson = null;
        }
      }
      // If we were not able to read the signatures from metadata,
      // we need to sync with the file system immediately.
      if (signaturesJson == null) {
        needsSave = sync();
      }
    }

    processDeferred(todoFull, SerializationType.FULL);
    state = State.LOADED;
    
    if (needsSave) {
      save();  // We may detect new resources
      saveSignatureCache();
    }
  }

  public void loadAll() {
    model().platform.log("loadAll: " + this.qualifiedName());
    for (Artifact artifact: this) {
      model().platform.log("loadAll loop: " + artifact);
      artifact.ensureLoaded();
      if (artifact instanceof Container) {
        ((Container) artifact).loadAll();
      }
    }
  }

  public void saveAll() {
    for (Artifact artifact: this) {
      if (!artifact.isBuiltin()) {
        artifact.save();
        if (artifact instanceof Container) {
          ((Container) artifact).saveAll();
        }
      }
    }
  }

  private void processDeferred(Map<Artifact,HutnObject> deferred,
      SerializationType serializationType) {
    for (Map.Entry<Artifact, HutnObject> entry: deferred.entrySet()) {
      Artifact artifact = entry.getKey();
      HutnObject json = entry.getValue();
      try {
        artifact.fromJson(json, serializationType, null);
      } catch(Exception e) {
        model().platform.log(
            "Deferred dezerializaion failed. JSON: " + json.toString(), e);
      }
    }
  }

  private final Artifact stubFromJson(HutnObject json) {
    String name = json.getString("name");
    String kind = json.getString("kind", json.getString("type", ""));
    return createStub(kind, name);
  }

  private final Artifact createStub(String kind, String name) {
    if ("class".equals(kind)) {
      return new Classifier((Module) this, name, Classifier.Kind.CLASS);
    }
    if ("interface".equals(kind)) {
      return new Classifier((Module) this, name, Classifier.Kind.INTERFACE);
    }
    if ("operation".equals(kind)) {
      if (this instanceof  Classifier && ((Classifier) this).isInterface()) {
        return new VirtualOperation(name, ((Classifier) this));
      }
      return new CustomOperation(this, name, false);
    }
    if ("module".equals(kind)) {
      return new Module(model(), (Module) this, name, false);
    }
    if ("resource".equals(kind)) {
      return new ResourceFile((Module) this, name);
    }
    return new Property(this, name, Type.ANY, null);
  }
  
  
  @Override
  public void fromJson(HutnObject json, SerializationType serializationType, Map<Artifact,HutnObject> deferred) {
    super.fromJson(json, serializationType, deferred);

    if (deferred != null) {
      for (String alias: ARTIFACTS_ALIASES) {
        HutnArray artifactsArray = json.getJsonArray(alias);
        if (artifactsArray == null) {
          HutnObject artifactsObject = json.getJsonObject(alias);
          if (artifactsObject == null) {
            continue;
          }
          artifactsArray = artifactsObject.toJsonArray(artifactsObject.keySet());
          if (artifactsArray == null) {
            continue;
          }     
        }
        for (int i = 0; i < artifactsArray.size(); i++) {
          HutnObject artifactJson = artifactsArray.getJsonObject(i);
          String name = artifactJson.getString("name", "");
          Artifact artifact = artifacts.get(name);
          if (artifact == null) {
            artifact = stubFromJson(artifactJson);
            artifacts.put(name, artifact);
          }
          deferred.put(artifact, artifactJson);
        }
      }
    }
  }

  /**
   * Synchronizes a single file inside this container with the existing contents.
   *
   * @param fileName Name of the file to be synchronized.
   * @return true if a change was detected and the metadata needs to be updated.
   */
  private boolean syncChild(String fileName, HashSet<String> deleted,
                            HashMap<Artifact, HutnObject> todo) throws IOException {
    // Determine the JSON file name, kind and artifact name

    model().platform.log("sync child: " + fileName + " of " + qualifiedName());

    if (fileName.equals("OnAttach.fgi")) {
      System.out.println("oh");
    }

    // final Filesystem storeageFs = model().platform.storageFileSystem();
    final File storageRoot = model().platform.storageRoot();
    int len = fileName.length();
    String fullFileName = qualifiedName() + "/" + fileName;
    String kind;
    String artifactName;
    if (fileName.endsWith("/")) {
      artifactName = fileName.substring(0, len - 1);
      if (Files.exists(storageRoot, fullFileName + Module.MODULE_FILENAME)) {
        fullFileName += Module.MODULE_FILENAME;
        kind = "module";
      } else if (Files.exists(storageRoot, fullFileName + "class.fgc")) {
        fullFileName += "class.fgc";
        kind = "class";
      } else {
        return false;
      }
    } else {
      int cut = fileName.lastIndexOf(".");
      if (fileName.endsWith(CustomOperation.FILE_EXTENSION)) {
        artifactName = fileName.substring(0, cut);
        kind = "operation";
      } else if (fileName.endsWith(Classifier.INTERFACE_FILE_EXTENSION)) {
        artifactName = fileName.substring(0, cut);
        kind = "interface";
      } else if (ResourceFile.kind(fileName) != null) {
        kind = "resource";
        artifactName = fileName;
      } else {
        return false;
      }
    }

    deleted.remove(artifactName);
    Stat stat = Files.stat(storageRoot, fullFileName);

    long timestamp = stat == null ? 0 : stat.lastModified();
    Long oldTimestamp = artifactFileTimestamps.get(fileName);
    if (oldTimestamp != null && timestamp <= oldTimestamp.longValue()) {
      return false;
    }
    artifactFileTimestamps.put(fileName, timestamp);

    Artifact artifact = artifacts.get(artifactName);
    if (artifact == null) {
      artifact = createStub(kind, artifactName);
      artifacts.put(artifactName, artifact);
    }
    if (!kind.equals("resource")) {
      todo.put(artifact, Model.loadJson(storageRoot, fullFileName));
    }
    return true;
  }

  public final void syncAll() {
    sync();
    for (Artifact a: this) {
      if (a instanceof Container) {
        ((Container) a).syncAll();
      }
    }
  }

  public boolean sync() {
    model().platform.log("sync: " + this.qualifiedName());
    HashSet<String> deleted = new HashSet<String>();
    deleted.addAll(artifactFileTimestamps.keySet());
    boolean needsSave = false;
    HashMap<Artifact,HutnObject> todo = new HashMap<Artifact,HutnObject>();
    String[] fileNameArray = Files.list(model().platform.storageRoot(), qualifiedName());

    for (String fileName: fileNameArray) {
      try {
        if (syncChild(fileName, deleted, todo)) {
          needsSave = true;
        }
      } catch (IOException e) {
        model().platform.log("IO error syncing file", e);
      }
    }

    for (String name: deleted) {
      artifactFileTimestamps.remove(name);
      artifacts.remove(name);
      needsSave = true;
    }

    processDeferred(todo, SerializationType.SIGNATURE);

    return needsSave;
  }
  
  @Override
  public void toJson(HutnSerializer json, SerializationType serializationType) {
    if (serializationType != SerializationType.CACHE) {
      super.toJson(json, serializationType);
    }
    if (serializationType != SerializationType.SIGNATURE) {
      json.startArray("artifacts");
      for (Artifact artifact : this) {
        boolean standalone = artifact.jsonFilename() != null;
        if (standalone == (serializationType == SerializationType.CACHE)) {
          json.startObject();
          artifact.toJson(json,
              standalone ? SerializationType.SIGNATURE : SerializationType.FULL);
          json.endObject();
        }
      }
      json.endArray();
    }
  }
 
  public void saveSignatureCache() {
    if (this instanceof Module && ((Module) this).builtin)  {
      System.err.println("Can't save system module: " + qualifiedName());
      return;
    }
    ensureLoaded();
    if (state == State.LOADING) {
      model().platform.info("Trying to save signatures of '" + qualifiedName() + 
          "' while in loading state", new RuntimeException());
    }

    HutnWriter writer = Model.saveJson(model().platform.cacheRoot(), jsonFilename() +
        SIGNATURE_CACHE_FILE_EXTENSION);
    writer.startObject();
    toJson(writer, SerializationType.CACHE);
    writer.startObject("timestamps");
    for (Entry<String,Long> e : artifactFileTimestamps.entrySet()) {
      writer.writeLong(e.getKey(), e.getValue());
    }
    writer.endObject();
    writer.endObject();
    writer.close();
  }

  @Override
  public Iterator<Artifact> iterator() {
    ensureLoaded();
    TreeSet<Artifact> sorted = new TreeSet<Artifact>();
    sorted.addAll(artifacts.values());
    return sorted.iterator();
  }
}
