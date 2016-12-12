package org.flowgrid.model;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

import org.flowgrid.model.Container.State;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.model.hutn.HutnSerializer;
import org.flowgrid.model.hutn.HutnWriter;
import org.flowgrid.model.io.Files;
import org.flowgrid.model.io.StatusListener;


/**
 * The Module is not a property at this level because we want primitive types
 * to be static (although they are a member of the system module).
 * 
 * This should probably be an interface instead of an abstract class.
 */
public abstract class Artifact implements Comparable<Artifact> {
  public enum SerializationType {
    CACHE,     // Serialize the name and type and the signatures of standalone children
    FULL,      // Serialize everything except standalone children.
    SIGNATURE, // Serialize the name and type. Don't serialize children at all.
  }

  public static final String IDENTIFIER_CONSTRAINT_MESSAGE =
      "Names must neither have leading or trailing whitespace nor contain colons or slashes.";
  
  protected static final double ORDER_MODULE = 1000;
  protected static final double ORDER_TYPE = 2000;
  protected static final double ORDER_MAIN = 3000;
  protected static final double ORDER_OPERATOR = 4000;
  protected static final double ORDER_OPERATION = 5000;
  protected static final double ORDER_PROPERTY = 6000;
  protected static final double ORDER_RESOURCE = 7000;
  
  protected Container owner;
  protected String name;
  protected String documentation;
  protected boolean isPublic;
  protected boolean builtin;

  public static boolean isIdentifier(String s) {
    return s != null && !s.isEmpty() && s.indexOf(':') == -1 && s.indexOf('/') == -1 && s.trim().equals(s);
  }


  protected Artifact(Container owner, String name) {
    this.owner = owner;
    this.name = name;
    if (owner != null && owner.owner != null && owner.builtin) {
      this.builtin = true;
      this.isPublic = true;
    }
  }

  public void delete() {
    rename(null, null, null);
  }

  public void fromJson(HutnObject json, SerializationType serializationType, Map<Artifact, HutnObject> deferred) {
    name = json.getString("name", name);
    documentation = json.getString("documentation",
        json.getBoolean("documentationAvailable", false) ? "(placeholder)" : documentation);
    if (json.containsKey("public") && !(this instanceof Module)) {
      setPublic(json.getBoolean("public", false));
    }
  }
  
  /**
   * Overwritten in Module
   */
  public Model model() {
    return owner.model();
  }
  
  public String name() {
    return name;
  }
  
  public String jsonFilename() {
    return null;
  }

  public void toJson(HutnSerializer json, SerializationType serializationType) {
    json.writeString("name", name);
    if (hasDocumentation()) {
      json.writeString("documentation", serializationType == SerializationType.SIGNATURE ?
          "(placeholder)" : documentation);
    }
    if (isPublic() && !(this instanceof Module)) {
      json.writeBoolean("public", true);
    }
  }

  public final String toString() {
    return toString(DisplayType.LIST);
  }

  public String toString(DisplayType type) {
    switch (type) {
      case TITLE:
        return getClass().getSimpleName() + " '" + name() + "'";
      default:
        return name();
    }
  }

  public final boolean isPublic() {
    return isPublic;
  }

  public final String documentation() {
    ensureLoaded();
    return documentation;
  }
  
  public final boolean hasDocumentation() {
    return (documentation != null && !documentation.isEmpty());
  }
  
  public final boolean setDocumentation(String doc) {
    if (Objects.equals(doc, this.documentation)) {
      return false;
    }
    this.documentation = doc;
    return true;
  }
  
  public abstract double order();
  
  public int compareTo(Artifact other) {
    double order = order();
    double otherOrder = other.order();
    
    if (order < otherOrder) {
      return -1;
    } 
    if (order > otherOrder) {
      return 1;
    }

    if (isPublic() != other.isPublic) {
      return isPublic() ? -1 : 1;
    }

    return name().compareTo(other.name());
  }
  
  public Callable<String> documentationCallable() {
    return new Callable<String>() {
      @Override
      public String call() throws Exception {
        return documentation();
      }
    };
  }
  
  public void ensureLoaded() {
  }

  /**
   * Renames. Called with null from delete() to delete. The name of the root module
   * must be the empty string and the root module cannot be deleted or renamed.
   */
  public void rename(Container newContainer, String newName, StatusListener progress) {
    if (builtin) {
      throw new IllegalStateException("Can't rename builtin " + qualifiedName());
    }

    boolean delete = newName == null && newContainer == null;
    if ((newName == null || newContainer == null) && !delete) {
      throw new IllegalArgumentException("newName and newContainer must be both null (or none)");
    }

    if(!delete) {
      if (progress != null) {
        progress.log("Loading");
      }
      // TODO(haustein): Limit if not public(?)
      model().rootModule.loadAll();
    }

    owner.ensureLoaded();

    try {
      if (!delete) {
        copyData(newContainer.qualifiedName() + "/" + newName, progress);
      }
      owner.artifacts.remove(name);
      deleteData(progress);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    this.name = newName == null ? "<deleted>" : newName;

    if (delete) {
      if (jsonFilename() == null) {
        owner.save();
      } else {
        owner.saveSignatureCache();
      }
      return;
    }

    owner = newContainer;
    owner.artifacts.put(newName, this);

    if (progress != null) {
      progress.log("Saving affected files");
    }
    name = newName;
    model().rootModule.saveAll();
  }

  /**
   * Copies data other than the data contained in the main json file
   */
  public void copyData(String destination, StatusListener statusListener) throws IOException {
  }

  /**
   * Deletes all data for this artifact, including the main json file.
   */
  public void deleteData(StatusListener statusListener) throws IOException {
    if (jsonFilename() != null) {
      Files.delete(model().platform.storageRoot(), jsonFilename());
    }
  }

  public final void save() {
    if (builtin) {
      return;
    }
    ensureLoaded();
    if (jsonFilename() == null) {
      if (owner != null) {
        owner.save();
      } else {
        System.err.println("Tried to save orphan artifact '" + qualifiedName() + "' without json filename.");
      }
    } else {
      HutnWriter json = model().saveJson(model().platform.storageRoot(), jsonFilename());
      json.startObject();
      toJson(json, SerializationType.FULL);
      json.endObject();
      json.close();
      if (owner != null && owner.state == State.LOADED) {
        owner.saveSignatureCache();
      }
    }
  }

  public final void setPublic(boolean b) {
    this.isPublic = b;
  }

  public Container owner() {
    return owner;
  }
  
  public String qualifiedName() {
    return owner == null ? name : (owner.qualifiedName() + "/" + name);
  }

  public boolean isBuiltin() {
    return builtin;
  }

  public String moduleLocalName(Module module) {
    if (owner == module) {
      return name;
    }
    if (owner instanceof Classifier && owner.owner == module) {
      return owner.name() + "/" + name;
    }
    return qualifiedName();
  }
}
